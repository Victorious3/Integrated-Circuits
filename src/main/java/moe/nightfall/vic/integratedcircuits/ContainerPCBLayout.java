package moe.nightfall.vic.integratedcircuits;

import moe.nightfall.vic.integratedcircuits.tile.TileEntityPCBLayout;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerPCBLayout extends Container
{
	public TileEntityPCBLayout tileentity;
	
	public ContainerPCBLayout(TileEntityPCBLayout tileentity)
	{
		this.tileentity = tileentity;
		this.tileentity.openInventory();
		
		this.addSlotToContainer(new Slot(tileentity, 0, 224, 8)
		{
			@Override
			public boolean isItemValid(ItemStack stack) 
			{
				return false;
			}

			@Override
			public boolean canTakeStack(EntityPlayer player) 
			{
				return false;
			}
		});
	}

	@Override
	public void onContainerClosed(EntityPlayer player) 
	{
		this.tileentity.closeInventory();
		super.onContainerClosed(player);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) 
	{
		return this.tileentity.isUseableByPlayer(player);
	}
	
	@Override
	public boolean canDragIntoSlot(Slot slot) 
	{
		return false;
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) 
	{
		return null;
	}
}
