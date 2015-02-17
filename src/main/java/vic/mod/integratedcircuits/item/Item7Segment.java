package vic.mod.integratedcircuits.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import vic.mod.integratedcircuits.gate.GateRegistry.ItemGatePair;
import vic.mod.integratedcircuits.gate.PartGate;
import vic.mod.integratedcircuits.misc.MiscUtils;

public class Item7Segment extends ItemPartGate implements IDyeable
{
	public Item7Segment(String name, PartGate gate, ItemGatePair parent, boolean isMultiPart) 
	{
		super(name, gate, parent, isMultiPart);
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
