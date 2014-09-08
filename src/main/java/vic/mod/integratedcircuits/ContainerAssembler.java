package vic.mod.integratedcircuits;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerAssembler extends Container
{
	public TileEntityAssembler tileentity;
	
	public ContainerAssembler(TileEntityAssembler tileentity)
	{
		this.tileentity = tileentity;
		this.tileentity.openInventory();
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
		//TODO
		return null;
	}

	@Override
	public boolean canDragIntoSlot(Slot slot) 
	{
		//TODO
		return false;
	}
}
