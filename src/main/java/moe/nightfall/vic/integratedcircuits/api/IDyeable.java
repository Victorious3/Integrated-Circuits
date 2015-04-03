package moe.nightfall.vic.integratedcircuits.api;

import net.minecraft.item.ItemStack;

public interface IDyeable 
{
	public boolean canDye(int color, ItemStack stack);
}
