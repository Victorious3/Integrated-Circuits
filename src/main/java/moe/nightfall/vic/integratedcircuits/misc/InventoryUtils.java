package moe.nightfall.vic.integratedcircuits.misc;

import moe.nightfall.vic.integratedcircuits.tile.TileEntityContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class InventoryUtils {
	public static ItemStack tryFetchItem(TileEntityContainer te, ItemStack stack, int from, int to) {
		for (int i = from; i <= to; i++) {
			ItemStack stack2 = te.getStackInSlot(i);
			if (stack2 == null)
				continue;
			if (stack.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack, stack2)) {
				if (stack2.stackSize >= stack.stackSize) {
					te.decrStackSize(i, stack.stackSize);
					return stack;
				}
			}
		}
		return null;
	}

	public static boolean tryPutItem(TileEntityContainer te, ItemStack stack, int from, int to) {
		if (stack == null)
			return true;
		for (int i = from; i <= to; i++) {
			ItemStack stack2 = te.getStackInSlot(i);
			if (stack2 != null && stack.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack, stack2)) {
				if (stack2.getMaxStackSize() >= stack2.stackSize + stack.stackSize) {
					stack2.stackSize += stack.stackSize;
					te.onSlotChange(i);
					return true;
				}
			}
		}
		for (int i = from; i <= to; i++) {
			ItemStack stack2 = te.getStackInSlot(i);
			if (stack2 == null) {
				te.setInventorySlotContents(i, stack);
				return true;
			}
		}
		return false;
	}

	public static ItemStack getFirstItem(Item item, IInventory inventory) {
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack != null && stack.getItem() == item)
				return stack;
		}
		return null;
	}

	public static int getSlotIndex(ItemStack stack, IInventory inventory) {
		for (int i = 0; i < inventory.getSizeInventory(); i++)
			if (inventory.getStackInSlot(i) == stack)
				return i;
		return -1;
	}
}
