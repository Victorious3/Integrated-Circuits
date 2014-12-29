package vic.mod.integratedcircuits.item;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.part.PartGate;

public class Item7Segment extends ItemGatePart implements IDyeable
{
	public Item7Segment(PartGate part) 
	{
		super("7segment", part);
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
		return I18n.format(MiscUtils.getLocalizedColor(stack.getItemDamage()) + " " + I18n.format(stack.getUnlocalizedName() + ".name"));
	}
}
