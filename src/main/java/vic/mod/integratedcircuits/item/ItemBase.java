package vic.mod.integratedcircuits.item;

import net.minecraft.item.Item;
import vic.mod.integratedcircuits.IntegratedCircuits;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemBase extends Item
{
	public ItemBase(String name)
	{
		setCreativeTab(IntegratedCircuits.creativeTab);
		setUnlocalizedName(IntegratedCircuits.modID + "." + name);
		setTextureName(IntegratedCircuits.modID + ":" + name);
		GameRegistry.registerItem(this, IntegratedCircuits.modID + "_" + name, IntegratedCircuits.modID);
	}
}
