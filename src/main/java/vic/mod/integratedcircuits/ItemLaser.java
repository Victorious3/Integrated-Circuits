package vic.mod.integratedcircuits;

import net.minecraft.item.Item;

public class ItemLaser extends Item
{
	public ItemLaser()
	{
		setCreativeTab(IntegratedCircuits.creativeTab);
		setUnlocalizedName(IntegratedCircuits.modID + ".laser");
		setMaxStackSize(1);
	}
}
