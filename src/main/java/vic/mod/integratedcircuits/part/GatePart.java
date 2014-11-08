package vic.mod.integratedcircuits.part;

import java.util.Arrays;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.TFacePart;

public abstract class GatePart extends JCuboidPart implements JNormalOcclusion, TFacePart
{
	private byte orientation;
	private Cuboid6 box = new Cuboid6(0, 0, 0, 1, 2 / 16D, 1);
	
	public void preparePlacement(EntityPlayer player, BlockCoord pos, int side, int meta)
    {
		setSide(side ^ 1);
		setRotation(Rotation.getSidedRotation(player, side));
    }

	@Override
	public void load(NBTTagCompound tag)
	{
		super.load(tag);
		orientation = tag.getByte("orientation");
	}
	
	@Override
	public void save(NBTTagCompound tag)
	{
		super.save(tag);
		tag.setByte("orientation", orientation);
	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		super.readDesc(packet);
		orientation = packet.readByte();
	}
	
	@Override
	public void writeDesc(MCDataOutput packet)
	{
		super.writeDesc(packet);
		packet.writeByte(orientation);
	}

	public int getSide()
	{
		return orientation >> 2;
	}
	
	public void setSide(int s)
	{
		orientation = (byte)(orientation & 3 | s << 2);
	}
	
	public int getRotation()
	{
		return orientation & 3;
	}
	
	public void setRotation(int r)
	{
		orientation = (byte)(orientation & 252| r);
	}
	
	@Override
	public Cuboid6 getBounds()
	{
		return box;
	}
	
	@Override
	public Iterable<Cuboid6> getOcclusionBoxes() 
	{
		return Arrays.asList(box);
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
}
