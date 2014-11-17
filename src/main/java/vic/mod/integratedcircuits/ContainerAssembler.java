package vic.mod.integratedcircuits;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerAssembler extends Container
{
	public TileEntityAssembler tileentity;
	
	public ContainerAssembler(IInventory playerInventory, final TileEntityAssembler tileentity)
	{
		this.tileentity = tileentity;
		this.tileentity.openInventory();
		
		this.addSlotToContainer(new Slot(this.tileentity, 0, 8, 8)
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
		this.addSlotToContainer(new Slot(this.tileentity, 1, 8, 113)
		{
			@Override
			public boolean isItemValid(ItemStack stack) 
			{
				return stack.getItem() == IntegratedCircuits.itemPCB && stack.getItemDamage() == 0;
			}
			
			@Override
			public int getSlotStackLimit() 
			{
				return 1;
			}
		});
		
		for(int i = 0; i < 7; i++)
			this.addSlotToContainer(new Slot(this.tileentity, i + 2, 40 + i * 18, 113));
		
		for(int i = 0; i < 4; i++)
			this.addSlotToContainer(new Slot(this.tileentity, i + 9, 148, 12 + i * 18)
			{
				@Override
				public boolean isItemValid(ItemStack stack) 
				{
					return stack.getItem() == IntegratedCircuits.itemLaser;
				}

				@Override
				public int getSlotStackLimit() 
				{
					return 1;
				}
			});
		
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 140 + i * 18));	
		for(int i = 0; i < 9; i++)
			this.addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 198));
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
		return slot.slotNumber > 1;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int id)
	{
		Slot slot = getSlot(id);
		ItemStack stack = slot.getStack();
		
		if(slot != null && slot.getHasStack())
		{
			ItemStack stack1 = stack.copy();
			if((id < 9 || id > 13) && stack.getItem() == IntegratedCircuits.itemLaser)
			{
				//Good that mergeItemStack doesn't care about the slot properties...
				for(int i = 9; i < 13; i++)
				{
					Slot slot2 = getSlot(i);
					if(!slot2.getHasStack())
					{
						stack.stackSize--;
						stack1.stackSize = 1;
						slot2.putStack(stack1);
						slot2.onSlotChanged();
						break;
					}
				}
			}
			else if(id < 13)
			{
				if(!mergeItemStack(stack, 13, getInventory().size() - 1, false)) return null;
			}
			else if(id >= 13)
			{
				if(!mergeItemStack(stack, 2, 8, false)) return null;
			}
			
			if(stack.stackSize == 0)
				slot.putStack(null);
			else slot.onSlotChanged();
			
			if(stack.stackSize == stack1.stackSize) return null;
			slot.onPickupFromSlot(player, stack);
		}
		return stack;
	}
}
