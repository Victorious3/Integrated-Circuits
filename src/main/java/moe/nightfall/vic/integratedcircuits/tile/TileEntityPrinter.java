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
		return inkLevel() > 0F;
	}

	public float inkLevel() {
		return inkLevel;
	}

	public boolean hasPaper() {
		return paperCount() > 0F;
	}

	public int paperCount() {
		return paperStack != null ? paperStack.stackSize : 0;
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

	public boolean addInk(ItemStack stack) {
		// TODO If you ever need this again, move it to the utility functions
		if (inkLevel < 1F && stack != null && stack.getItem() == Items.dye && stack.getItemDamage() == 0) {
			inkLevel += 0.2F;
			inkLevel = Math.min(inkLevel, 1F);
			stack.stackSize--;

			markDirty();
			return true;
		}
		return false;
	}

	public boolean addPaper(ItemStack stack) {
		// TODO Same as above
		if (stack != null && stack.getItem() == Items.paper && paperCount() < 16) {
			if (paperStack == null) {
				paperStack = new ItemStack(Items.paper);
				paperStack.stackSize = stack.stackSize;
			} else {
				paperStack.stackSize += stack.stackSize;
			}

			markDirty();

			stack.stackSize = 0;
			int over = paperStack.stackSize - 16;
			paperStack.stackSize = Math.min(paperStack.stackSize, 16);
			if (over > 0) {
				stack.stackSize = over;
			}
			return true;
		}
		return false;
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
