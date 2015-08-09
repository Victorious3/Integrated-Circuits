package moe.nightfall.vic.integratedcircuits;

import moe.nightfall.vic.integratedcircuits.tile.TileEntityCAD;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerCAD extends Container {
	public TileEntityCAD tileentity;

	public ContainerCAD(TileEntityCAD tileentity) {
		this.tileentity = tileentity;
		this.tileentity.openInventory();

		this.addSlotToContainer(new Slot(tileentity, 0, 0, 0) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return false;
			}

			@Override
			public boolean canTakeStack(EntityPlayer player) {
				return false;
			}
		});
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
		return false;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
		return null;
	}
}
