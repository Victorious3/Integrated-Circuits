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
		
		this.addSlotToContainer(new Slot(this.tileentity, 0, 6, 6)
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
		this.addSlotToContainer(new Slot(this.tileentity, 1, 6, 52)
		{
			@Override
			public boolean isItemValid(ItemStack stack) 
			{
				return stack.getItem() == IntegratedCircuits.itemPCB && stack.getItemDamage() == 0;
			}
		});
		
		for(int i = 0; i < 9; i++)
			this.addSlotToContainer(new Slot(this.tileentity, i + 2, 8 + i * 18, 108));
		
		for(int i = 0; i < 4; i++)
			this.addSlotToContainer(new Slot(this.tileentity, i + 11, 154, 6 + i * 18)
			{
				@Override
				public boolean isItemValid(ItemStack stack) 
				{
					return stack.getItem() == IntegratedCircuits.itemLaser;
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
		return false;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) 
	{
		return null;
	}
}
