package vic.mod.integratedcircuits;

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
		
		this.addSlotToContainer(new Slot(tileentity, 0, 224, 8));
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
	public ItemStack slotClick(int par1, int par2, int par3, EntityPlayer player) 
	{
		//You can't interact with the one and only slot.
		return null;
	}

	@Override
	public boolean canDragIntoSlot(Slot slot) 
	{
		return false;
	}	
}
