package vic.mod.integratedcircuits.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import vic.mod.integratedcircuits.misc.MiscUtils;

public class Item7Segment extends ItemBase implements IDyeable
{
	public Item7Segment() 
	{
		super("7segment");
	}

	@Override
	public boolean canDye(int color, ItemStack stack) 
	{
		return true;
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List list) 
	{
		for(int i = 0; i < 16; i++)
			list.add(new ItemStack(this, 1, i));
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) 
	{
		return MiscUtils.getLocalizedColor(stack.getItemDamage()) + " " + StatCollector.translateToLocal(stack.getUnlocalizedName() + ".name");
	}
}
