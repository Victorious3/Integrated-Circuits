package moe.nightfall.vic.integratedcircuits;

import moe.nightfall.vic.integratedcircuits.tile.TileEntityAssembler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ContainerAssembler extends ContainerBase {
	public TileEntityAssembler tileentity;

	public ContainerAssembler(IInventory playerInventory, final TileEntityAssembler tileentity) {
		this.tileentity = tileentity;
		this.tileentity.openInventory();

		//Disk slot: 0
		this.addSlotToContainer(new Slot(this.tileentity, 0, 8, 8) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return false;
			}

			@Override
			public boolean canTakeStack(EntityPlayer player) {
				return false;
			}
		});
		
		//PCB slot: 1
		this.addSlotToContainer(new Slot(this.tileentity, 1, 8, 113) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return stack.getItem() == IntegratedCircuits.itemPCB && stack.getItemDamage() == 0;
			}

			@Override
			public int getSlotStackLimit() {
				return 1;
			}
		});

		//Material input slots: 2-8
		for (int i = 0; i < 7; i++)
			this.addSlotToContainer(new Slot(this.tileentity, i + 2, 40 + i * 18, 113));

		//Laser slots: 9-12
		for (int i = 0; i < 4; i++)
			this.addSlotToContainer(new Slot(this.tileentity, i + 9, 148, 12 + i * 18) {
				@Override
				public boolean isItemValid(ItemStack stack) {
					return stack.getItem() == IntegratedCircuits.itemLaser;
				}

				@Override
				public int getSlotStackLimit() {
					return 1;
				}

				@Override
				public boolean canTakeStack(EntityPlayer player) {
					return tileentity.getStatus() != TileEntityAssembler.RUNNING;
				}
			});

		//Player inventory slots: 13-39
		//Player hotbar slots: 40-48
		addPlayerInv(playerInventory, 8, 140);
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		this.tileentity.closeInventory();
		super.onContainerClosed(player);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return this.tileentity.isUseableByPlayer(player);
	}

	@Override
	public boolean canDragIntoSlot(Slot slot) {
		return slot.slotNumber > 1;
	}
	
	@Override
	protected boolean doTransferStack(ItemStack stack, int slot) {
		if(slot >= 13 && stack.getItem() == IntegratedCircuits.itemLaser) {
			//Transfer lasers to laser slots
			if(!mergeItemStack(stack, 9, 13, false))
				return false;
		} else if(slot < 13) {
			//Transfer from machine to player
			if(!mergeItemStack(stack, 13, inventorySlots.size(), false))
				return false;
		} else {
			//Transfer materials from player to machine
			if(!mergeItemStack(stack, 1, 9, false)) {
				if(slot < 40) {
					//Transfer from inventoy into hotbar
					if(!mergeItemStack(stack, 40, 49, false))
						return false;
					//Transfer from hotbar into inventory
				} else if(!mergeItemStack(stack, 13, 40, false))
					return false;
			}
		}
		return true;
	}
}
