package moe.nightfall.vic.integratedcircuits;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Base class for GUI containers
 * 
 * @author ljfa
 */
public abstract class ContainerBase extends Container {

	/**
	 * Called when a player shift-clicks on a slot. It is called repeatedly
	 * until it returns null.
	 * 
	 * @return null if nothing has happened, e.g. the stack could not be transferred.
	 * Else, a copy of the stack that was originally in the slot.
	 */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotInd) {
		ItemStack copyStack = null;
		Slot slot = (Slot) inventorySlots.get(slotInd);

		if (slot != null && slot.getHasStack()) {
			ItemStack stackInSlot = slot.getStack();
			copyStack = stackInSlot.copy();

			if (!doTransferStack(stackInSlot, slotInd))
				return null;

			if (stackInSlot.stackSize == 0)
				slot.putStack(null);
			else
				slot.onSlotChanged();

			if (copyStack.stackSize == stackInSlot.stackSize)
				return null;

			slot.onPickupFromSlot(player, stackInSlot);
		}
		return copyStack;
	}
	
	/**
	 * Adds the slots for the player inventory to the container at the given coordinates.
	 */
	protected void addPlayerInv(IInventory invPlayer, int x, int y) {
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(invPlayer, 9 + j + 9 * i, x + 18 * j, y + 18 * i));

		for (int j = 0; j < 9; j++)
			addSlotToContainer(new Slot(invPlayer, j, x + 18 * j, y + 58));
	}
	
	/**
	 * Does the actual transferring of the stack in the slot to somewhere else.
	 * @return true if the stack could be transferred at least partially
	 */
	protected abstract boolean doTransferStack(ItemStack stack, int slot);
	
	/**
	 * Gets the current amount that is available of an item type, crawls the
	 * custom inventory
	 **/
	public int getAmountOf(Item item) {
		int amount = 0;
		for (int i = 0; i < 7; i++) {
			Slot s = getSlot(i + 2);
			if (s.getHasStack() && s.getStack().getItem() == item)
				amount += s.getStack().stackSize;
		}
		return amount;
	}
}
