package moe.nightfall.vic.integratedcircuits.tile;

import moe.nightfall.vic.integratedcircuits.Content;
import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketWrapper;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;

public class TileEntitySocket extends TileEntity implements ISocketWrapper {
	public ISocket socket = IntegratedCircuitsAPI.getGateRegistry().createSocketInstance(this);

	public boolean isDestroyed;

	@Override
	public void markRender() {
		worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
	}

	@Override
	public void updateEntity() {
		socket.update();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		socket.readFromNBT(compound);
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		socket.writeToNBT(compound);
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound comp = new NBTTagCompound();
		socket.writeDesc(comp);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, blockMetadata, comp);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		NBTTagCompound comp = pkt.func_148857_g();
		socket.readDesc(comp);
	}

	@Override
	public MCDataOutput getWriteStream(int disc) {
		return IntegratedCircuitsAPI.getWriteStream(getWorld(), getPos(), socket.getSide()).writeByte(disc);
	}

	@Override
	public World getWorld() {
		return worldObj;
	}

	@Override
	public void notifyBlocksAndChanges() {
		markDirty();
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
	}

	@Override
	public void notifyPartChange() {
		markDirty();
		worldObj.notifyBlockChange(xCoord, yCoord, zCoord, getBlockType());
	}

	@Override
	public BlockCoord getPos() {
		return new BlockCoord(xCoord, yCoord, zCoord);
	}

	@Override
	public void destroy() {
		MiscUtils.dropItem(worldObj, new ItemStack(Content.itemSocket), xCoord, yCoord, zCoord);
		isDestroyed = true;
		worldObj.setBlockToAir(xCoord, yCoord, zCoord);
	}

	@Override
	public byte[] updateBundledInput(int side) {
		return IntegratedCircuitsAPI.updateBundledInput(getSocket(), side);
	}

	@Override
	public int updateRedstoneInput(int side) {
		return IntegratedCircuitsAPI.updateRedstoneInput(getSocket(), side);
	}

	@Override
	public void scheduleTick(int delay) {
		worldObj.scheduleBlockUpdate(xCoord, yCoord, zCoord, getBlockType(), delay);
	}

	@Override
	public ISocket getSocket() {
		return socket;
	}

	@Override
	public void sendDescription() {
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public void updateInput() {
		socket.updateInput();
	}
}