package vic.mod.integratedcircuits.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

public class ItemCircuit extends ItemBase
{
	public ItemCircuit()
	{
		super("circuit");
		setMaxStackSize(1);
		setCreativeTab(null);
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack) 
	{
		if(stack.getTagCompound() == null) return StatCollector.translateToLocalFormatted(getUnlocalizedName() + ".name", "INVALID!");
		String name = stack.getTagCompound().getCompoundTag("circuit").getCompoundTag("properties").getString("name");
		return StatCollector.translateToLocalFormatted(getUnlocalizedName() + ".name", name);
	}
}
