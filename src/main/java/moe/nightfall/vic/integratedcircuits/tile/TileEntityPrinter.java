package moe.nightfall.vic.integratedcircuits.tile;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityPrinter extends TileEntityContainer {

	private ItemStack paperStack;
	private float inkLevel = 0F;

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int id) {
		return id == 0 ? paperStack : null;
	}

	public boolean hasInk() {
		return inkLevel > 0F;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		paperStack = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("paperStack"));
		inkLevel = compound.getFloat("inkLevel");
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		if (paperStack != null) {
			compound.setTag("paperStack", paperStack.writeToNBT(new NBTTagCompound()));
		}
		compound.setFloat("inkLevel", inkLevel);
	}

	@Override
	public boolean receiveClientEvent(int id, int par) {
		return super.receiveClientEvent(id, par);

	}

	public ItemStack addInk(ItemStack stack) {
		// TODO If you ever need this again, move it to the utility functions
		if (inkLevel < 1F && stack != null && stack.getItem() == Items.dye && stack.getItemDamage() == 1) {
			inkLevel += 0.2F;
			inkLevel = Math.min(inkLevel, 1F);
			stack.stackSize--;
			if (stack.stackSize < 1) {
				return null;
			}
			markDirty();
		}
		return stack;
	}

	@Override
	public void setInventorySlotContents(int id, ItemStack stack) {
		if (id == 0)
			paperStack = stack;
		markDirty();
	}

	@Override
	public boolean isItemValidForSlot(int id, ItemStack stack) {
		return id == 0 && stack != null && stack.getItem() == Items.paper ? true : false;
	}
}
