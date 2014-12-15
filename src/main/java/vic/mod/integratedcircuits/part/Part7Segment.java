package vic.mod.integratedcircuits.part;

import net.minecraft.item.ItemStack;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.client.Part7SegmentRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Part7Segment extends GatePart
{
	@SideOnly(Side.CLIENT)
	private static Part7SegmentRenderer renderer = new Part7SegmentRenderer();
	
	public Part7Segment() 
	{
		super("7segment");
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
	GatePart newInstance() 
	{
		return new Part7Segment();
	}
}
