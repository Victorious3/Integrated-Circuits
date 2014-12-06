package vic.mod.integratedcircuits.misc;

import net.minecraft.item.ItemStack;
import vic.mod.integratedcircuits.TileEntityBase;

public class InventoryUtils 
{
	public static ItemStack tryFetchItem(TileEntityBase te, ItemStack stack, int from, int to)
	{
		for(int i = from; i <= to; i++)
		{
			ItemStack stack2 = te.getStackInSlot(i);
			if(stack2 == null) continue; 
			if(stack.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack, stack2))
			{
				if(stack2.stackSize >= stack.stackSize)
				{
					te.decrStackSize(i, stack.stackSize);
					return stack;
				}
			}
		}
		return null;
	}
	
	public static boolean tryPutItem(TileEntityBase te, ItemStack stack, int from, int to)
	{
		if(stack == null) return true;
		for(int i = from; i <= to; i++)
		{
			ItemStack stack2 = te.getStackInSlot(i);
			if(stack2 != null && stack.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack, stack2))
			{
				if(stack2.getMaxStackSize() >= stack2.stackSize + stack.stackSize)
				{
					stack2.stackSize += stack.stackSize;
					te.onSlotChange(i);
					return true;
				}
			}
		}
		for(int i = from; i <= to; i++)
		{
			ItemStack stack2 = te.getStackInSlot(i);
			if(stack2 == null)
			{
				te.setInventorySlotContents(i, stack);
				return true;
			}
		}
		return false;
	}
}
