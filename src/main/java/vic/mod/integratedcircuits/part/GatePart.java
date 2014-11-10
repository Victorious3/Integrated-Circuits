package vic.mod.integratedcircuits.part;

import java.util.Arrays;

import mrtjp.projectred.api.IBundledEmitter;
import mrtjp.projectred.api.IConnectable;
import mrtjp.projectred.api.IScrewdriver;
import mrtjp.projectred.transmission.IRedwireEmitter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.IFaceRedstonePart;
import codechicken.multipart.IRedstonePart;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.TFacePart;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;

@InterfaceList(value = {
	@Interface(iface = "mrtjp.projectred.api.IBundledEmitter", modid = "ProjRed|Core"),
	@Interface(iface = "mrtjp.projectred.api.IConnectable", modid = "ProjRed|Core"),
})
public abstract class GatePart extends JCuboidPart implements JNormalOcclusion, TFacePart, IConnectable, IFaceRedstonePart, IBundledEmitter
{
	public byte orientation;
	private Cuboid6 box = new Cuboid6(0, 0, 0, 1, 2 / 16D, 1);
	public byte[][] output = new byte[4][16];
	public byte[][] input = new byte[4][16];
	
	public void preparePlacement(EntityPlayer player, BlockCoord pos, int side, int meta)
    {
		setSide(side ^ 1);
		setRotation(Rotation.getSidedRotation(player, side));
    }

	@Override
	public void load(NBTTagCompound tag)
	{
		orientation = tag.getByte("orientation");
	}
	
	@Override
	public void save(NBTTagCompound tag)
	{
		tag.setByte("orientation", orientation);
	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		orientation = packet.readByte();
	}
	
	@Override
	public void writeDesc(MCDataOutput packet)
	{
		packet.writeByte(orientation);
	}

	@Override
	public void read(MCDataInput packet) 
	{
		switch (packet.readByte()) {
		case 0 :
			orientation = packet.readByte();
			tile().markRender();
			break;
		}
	}
	
	public MCDataOutput getWriteStream(int disc)
	{
		return getWriteStream().writeByte(disc);
	}

	public Transformation getRotationTransformation()
	{
		return Rotation.sideOrientation(getSide(), getRotation()).at(Vector3.center);
	}

	public int getSide()
	{
		return orientation >> 2;
	}
	
	public int getSideRel(int side)
	{
		return getRotationRel(Rotation.rotationTo(getSide(), side));
	}
	
	public void setSide(int s)
	{
		orientation = (byte)(orientation & 3 | s << 2);
	}
	
	public int getRotation()
	{
		return orientation & 3;
	}
	
	public int getRotationAbs(int rel)
	{
		return (rel + getRotation() + 2) % 4;
	}
	
	public int getRotationRel(int abs)
	{
		return (abs + 6 - getRotation()) % 4;
	}
	
	public void setRotation(int r)
	{
		orientation = (byte)(orientation & 252| r);
	}
	
	@Override
	public Cuboid6 getBounds()
	{
		return box.copy().apply(getRotationTransformation());
	}
	
	@Override
	public Iterable<Cuboid6> getOcclusionBoxes() 
	{
		return Arrays.asList(getBounds());
	}
	
	@Override
    public boolean occlusionTest(TMultiPart npart)
    {
        return NormalOcclusionTest.apply(this, npart);
    }
	
	@Override
	public int getSlotMask() 
	{
		return 1 << getSide();
	}

	@Override
	public int redstoneConductionMap() 
	{
		return 0;
	}

	@Override
	public boolean solid(int arg0) 
	{
		return false;
	}
	
	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item) 
	{
		if(item != null && item.getItem() instanceof IScrewdriver)
		{
			if(!world().isRemote)
			{
				((IScrewdriver)item.getItem()).damageScrewdriver(world(), player);
				setRotation((getRotation() + 1) % 4);
				getWriteStream(0).writeByte(orientation);
				tile().markDirty();
				tile().notifyPartChange(this);
				tile().notifyNeighborChange(getSide());
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void onNeighborChanged() 
	{
		if(!world().isRemote) updateRedstoneInput();
	}
	
	public void updateRedstoneInput()
	{
		System.out.print(new BlockCoord(tile()) + " - ");
		for(int i = 0; i < 4; i++)
		{
			int r = getRotationAbs(i);
			BlockCoord crd = new BlockCoord(tile());
			crd.offset(Rotation.rotateSide(getSide(), r));
			TileEntity te = world().getTileEntity(crd.x, crd.y, crd.z);
			if(te == null) continue;
			if(te instanceof TileMultipart)
			{
				TileMultipart tm = (TileMultipart)te;
				TMultiPart tp = tm.partMap(getSide());
				if(tp instanceof IRedstonePart)
					System.out.print(((IRedstonePart)tp).weakPowerLevel(Rotation.rotateSide(getSide(), (r + 2) % 4)));
			}
//			else if(world().isBlockProvidingPowerTo(arg0, arg1, arg2, arg3))
//				System.out.print(world().getBlockPowerInput(crd.x, crd.y, crd.z));
		}
		System.out.println();
	}

	//ProjectRed
	
	@Override
	public boolean canConnectCorner(int arg0) 
	{
		return false;
	}

	@Override
	public boolean connectCorner(IConnectable arg0, int arg1, int arg2) 
	{
		return connectStraight(arg0, arg1, arg2);
	}

	@Override
	public boolean connectInternal(IConnectable arg0, int arg1) 
	{
		return connectStraight(arg0, arg1, 0);
	}

	@Override
	public boolean connectStraight(IConnectable arg0, int arg1, int arg2) 
	{
		int side = getRotationRel(arg1);
		if(arg0 instanceof IRedwireEmitter && canConnectRedstoneImpl(side)) return true;
		if(arg0 instanceof IBundledEmitter) return canConnectBundledImpl(side);
		return false;
	}
	
	@Override
	public byte[] getBundledSignal(int arg0) 
	{
		return null;
	}
	
	//---

	@Override
	public final boolean canConnectRedstone(int arg0) 
	{
		if((arg0 & 6) == (getSide() & 6)) return false;
		return canConnectRedstoneImpl(getRotationRel(getSideRel(arg0)));
	}

	public abstract boolean canConnectRedstoneImpl(int arg0);
	public abstract boolean canConnectBundledImpl(int arg0);
	
	@Override
	public int strongPowerLevel(int arg0) 
	{
		return 0;
	}

	@Override
	public int weakPowerLevel(int arg0) 
	{
		return strongPowerLevel(arg0);
	}

	@Override
	public int getFace() 
	{
		return getSide();
	}
}
