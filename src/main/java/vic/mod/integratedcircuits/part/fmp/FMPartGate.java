package vic.mod.integratedcircuits.part.fmp;

import java.util.Arrays;

import mrtjp.projectred.api.IBundledEmitter;
import mrtjp.projectred.api.IConnectable;
import mrtjp.projectred.transmission.APIImpl_Transmission;
import mrtjp.projectred.transmission.IRedwireEmitter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.part.GateProvider.IGateProvider;
import vic.mod.integratedcircuits.part.PartGate;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.IFaceRedstonePart;
import codechicken.multipart.IRedstonePart;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.RedstoneInteractions;
import codechicken.multipart.TFacePart;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@InterfaceList(value = {
	@Interface(iface = "mrtjp.projectred.api.IBundledEmitter", modid = "ProjRed|Core"),
	@Interface(iface = "mrtjp.projectred.api.IConnectable", modid = "ProjRed|Core")
})
public class FMPartGate extends JCuboidPart implements JNormalOcclusion, TFacePart, IConnectable, IFaceRedstonePart, IBundledEmitter, IGateProvider
{
	private PartGate gate;
	private Cuboid6 box = new Cuboid6(0, 0, 0, 1, 2 / 16D, 1);

	public FMPartGate(PartGate gate)
	{
		this.gate = gate;
		PartFactory.register(this);
	}
	
	@Override
	public String getType() 
	{
		return gate.getType();
	}
	
	@Override
	public void load(NBTTagCompound tag)
	{
		gate.load(tag);
	}
	
	@Override
	public void save(NBTTagCompound tag)
	{
		gate.save(tag);
	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		gate.readDesc(packet);
	}
	
	@Override
	public void writeDesc(MCDataOutput packet)
	{
		gate.writeDesc(packet);
	}

	@Override
	public void read(MCDataInput packet) 
	{
		gate.read(packet.readByte(), packet);
	}
	
	@Override
	public MCDataOutput getWriteStream(int disc)
	{
		return getWriteStream().writeByte(disc);
	}

	@Override
	public Cuboid6 getBounds()
	{
		return box.copy().apply(gate.getRotationTransformation());
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
		return 1 << gate.getSide();
	}

	@Override
	public int redstoneConductionMap() 
	{
		return 0;
	}
	
	@Override
	public void update() 
	{
		gate.update();
	}

	@Override
	public void scheduledTick() 
	{
		gate.scheduledTick();
	}

	@Override
	public boolean solid(int arg0) 
	{
		return false;
	}
	
	public void preparePlacement(EntityPlayer player, BlockCoord pos, int side, int meta)
	{
		gate.preparePlacement(player, pos, side, meta);
	}
	
	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item) 
	{
		return gate.activate(player, hit, item);
	}
	
	@Override
	public void onAdded() 
	{
		gate.onAdded();
	}

	@Override
	public void onWorldJoin() 
	{
		gate.onWorldJoin();
	}

	@Override
	public void onRemoved()
	{
		gate.onRemoved();
	}

	@Override
	public void onMoved() 
	{
		gate.onMoved();
	}
	
	@Override
	public Iterable<ItemStack> getDrops() 
	{
		return Arrays.asList(gate.getItem());
	}
	
	@Override
	public ItemStack pickItem(MovingObjectPosition hit) 
	{
		return gate.pickItem(hit);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderStatic(Vector3 pos, int pass) 
	{
		return gate.renderStatic(pos, pass);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float frame, int pass) 
	{
		gate.renderDynamic(pos, frame, pass);
	}
	
	@Override
	public void onNeighborChanged() 
	{
		gate.onNeighborChanged();
	}

	@Override
	public void onPartChanged(TMultiPart part) 
	{
		if(!world().isRemote) gate.updateInput();
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
		int side = gate.getRotationRel(arg1);
		if(arg0 instanceof IRedwireEmitter && gate.canConnectRedstoneImpl(side)) return true;
		else if(arg0 instanceof IBundledEmitter && gate.canConnectBundledImpl(side)) return true;
		return false;
	}
	
	@Override
	public byte[] getBundledSignal(int arg0) 
	{
		int rot = gate.getRotationRel(arg0);
		if(!gate.canConnectBundledImpl(rot)) return null;
		return gate.output[rot];
	}
	
	//---

	@Override
	public final boolean canConnectRedstone(int arg0) 
	{
		if((arg0 & 6) == (gate.getSide() & 6)) return false;
		return gate.canConnectRedstoneImpl(gate.getSideRel(arg0));
	}
	
	@Override
	public int strongPowerLevel(int arg0) 
	{
		if((arg0 & 6) == (gate.getSide() & 6)) return 0;
		int rot = gate.getSideRel(arg0);
		if(!gate.canConnectRedstoneImpl(rot)) return 0;
		return gate.getRedstoneOutput(rot);
	}

	@Override
	public int weakPowerLevel(int arg0) 
	{
		return strongPowerLevel(arg0);
	}

	@Override
	public int getFace() 
	{
		return gate.getSide();
	}

	public FMPartGate newInstance()
	{
		FMPartGate fmpgate = new FMPartGate(gate.newInstance());
		fmpgate.gate.setProvider(fmpgate);
		return fmpgate;
	}

	@Override
	public void markRender() 
	{
		tile().markRender();
	}

	@Override
	public World getWorld() 
	{
		return world();
	}
	
	@Override
	public void notifyPartChange() 
	{
		tile().notifyPartChange(this);
	}

	@Override
	public void notifyBlocksAndChanges() 
	{
		tile().markDirty();
		tile().notifyPartChange(this);
		tile().notifyNeighborChange(gate.getSide());
	}

	@Override
	public BlockCoord getPos() 
	{
		return new BlockCoord(x(), y(), z());
	}

	@Override
	public TileEntity getTileEntity() 
	{
		return tile();
	}

	@Override
	public void destroy() 
	{
		tile().remPart(this);
	}
	
	@Override
	public byte[] updateBundledInput(int side)
	{
		int r = gate.getRotationAbs(side);
		int abs = Rotation.rotateSide(gate.getSide(), r);
		byte[] power = null;

		if(((abs ^ 1) & 6) != ((gate.getSide() ^ 1) & 6))
		{
			BlockCoord pos = new BlockCoord(tile()).offset(abs).offset(gate.getSide());
			TileEntity t = world().getTileEntity(pos.x, pos.y, pos.z);
			if(t != null && t instanceof TileMultipart) 
				power = updateBundledPartSignal(0, ((TileMultipart)t).partMap(abs ^ 1), Rotation.rotationTo(abs ^ 1, gate.getSide() ^ 1));
			if(power != null) return power;
		}
		
		BlockCoord pos = new BlockCoord(tile()).offset(abs);
		TileEntity t = world().getTileEntity(pos.x, pos.y, pos.z);
		if (t instanceof IBundledEmitter)
			power = updateBundledPartSignal(1, t, abs ^ 1);
		else if (t instanceof TileMultipart)
			power = updateBundledPartSignal(1, ((TileMultipart)t).partMap(gate.getSide()), (r + 2) % 4);
		else power = APIImpl_Transmission.getBundledSignal(world(), pos, abs ^ 1);
		if(power != null) return power;
		
		if((abs & 6) != (gate.getSide() & 6))
		{
			TMultiPart tp = tile().partMap(abs);
			power = updateBundledPartSignal(2, tp, Rotation.rotationTo(abs, gate.getSide()));
			if(power != null) return power;
		}
		
		return new byte[16];
	}
	
	private byte[] updateBundledPartSignal(int mode, Object part, int r)
	{
		if(part instanceof IBundledEmitter) return ((IBundledEmitter)part).getBundledSignal(r);
		return null;
	}
	
	@Override
	public int updateRedstoneInput(int side)
	{
		int r = gate.getRotationAbs(side);
		int abs = Rotation.rotateSide(gate.getSide(), r);
		if(strongPowerLevel(abs) != 0) return 0;
		int power = 0;
		
		if(((abs ^ 1) & 6) != ((gate.getSide() ^ 1) & 6))
		{
			BlockCoord pos = getPos().offset(abs).offset(gate.getSide());
			TileEntity t = world().getTileEntity(pos.x, pos.y, pos.z);
			if(t != null && t instanceof TileMultipart) 
				power = updatePartSignal(((TileMultipart)t).partMap(abs ^ 1), Rotation.rotationTo(abs ^ 1, gate.getSide() ^ 1));
			if(power > 0) return power / 17;
		}
		
		power = RedstoneInteractions.getPowerTo(this, abs);
		if(power > 0) return power;
		
		TMultiPart tp = tile().partMap(abs);
		if((abs & 6) != (gate.getSide() & 6))
		{
			power = updatePartSignal(tp, Rotation.rotationTo(abs, gate.getSide()));
			if(power > 0) return power / 17;
		}	
		if(tp instanceof IRedstonePart)
		{
			IRedstonePart rp = (IRedstonePart)tp;
			power = Math.max(rp.strongPowerLevel(gate.getSide()), rp.weakPowerLevel(gate.getSide())) << 4;
			if(power > 0) return power;
		}
		
		return power;
	}
	
	private int updatePartSignal(TMultiPart part, int r)
	{
		if(!IntegratedCircuits.isPRLoaded) return 0;
		if(part instanceof IRedwireEmitter) 
			return ((IRedwireEmitter)part).getRedwireSignal(r);
		return 0;
	}

	@Override
	public PartGate getGate() 
	{
		return gate;
	}
}
