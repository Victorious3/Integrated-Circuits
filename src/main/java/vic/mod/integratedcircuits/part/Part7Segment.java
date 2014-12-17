package vic.mod.integratedcircuits.part;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.client.Part7SegmentRenderer;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Part7Segment extends PartGate
{
	@SideOnly(Side.CLIENT)
	private static Part7SegmentRenderer renderer = new Part7SegmentRenderer();
	public int display;
	
	public Part7Segment() 
	{
		super("7segment");
	}
	
	@Override
	public void load(NBTTagCompound tag)
	{
		super.load(tag);
		display = tag.getInteger("display");	
	}
	
	@Override
	public void save(NBTTagCompound tag)
	{
		super.save(tag);
		tag.setInteger("display", orientation);
	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		super.readDesc(packet);
		display = packet.readInt();
	}
	
	@Override
	public void writeDesc(MCDataOutput packet)
	{
		super.writeDesc(packet);
		packet.writeInt(display);
	}

	@Override
	public void read(byte discr, MCDataInput packet) 
	{
		if(discr == 10)
			display = packet.readInt();
		else super.read(discr, packet);
	}

	@Override
	public ItemStack getItem() 
	{
		return new ItemStack(IntegratedCircuits.item7Segment);
	}

	@Override
	public Part7SegmentRenderer getRenderer() 
	{
		return renderer;
	}

	@Override
	public boolean canConnectRedstoneImpl(int arg0) 
	{
		return false;
	}

	@Override
	public boolean canConnectBundledImpl(int arg0) 
	{
		return true;
	}

	@Override
	PartGate newInstance() 
	{
		return new Part7Segment();
	}
}
