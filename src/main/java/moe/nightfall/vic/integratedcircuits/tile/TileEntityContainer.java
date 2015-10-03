package moe.nightfall.vic.integratedcircuits.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public abstract class TileEntityContainer extends TileEntity implements IInventory {
	public int rotation;
	public int playersUsing;

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		rotation = compound.getInteger("rotation");
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("rotation", rotation);
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound compound = new NBTTagCompound();
		writeToNBT(compound);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, compound);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		NBTTagCompound compound = pkt.func_148857_g();
		readFromNBT(compound);
	}

	@Override
	public boolean receiveClientEvent(int id, int par) {
		if (id == 0) {
			playersUsing = par;
			worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
			return true;
		}
		return false;
	}

	@Override
	public void openInventory() {
		if (!worldObj.isRemote)
			worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 0, ++playersUsing);
	}

	@Override
	public void closeInventory() {
		if (!worldObj.isRemote)
			worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 0, --playersUsing);
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
	}

	public void onSlotChange(int id) {

	}

	// IInventory defaults

	@Override
	public ItemStack getStackInSlotOnClosing(int id) {
		return getStackInSlot(id);
	}

	@Override
	public ItemStack decrStackSize(int id, int size) {
		return null;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isItemValidForSlot(int id, ItemStack stack) {
		return true;
	}

	@Override
	public String getInventoryName() {
		return null;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	public boolean rotate() {
		this.rotation = rotation + 1 & 3;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		return true;
	}
}
