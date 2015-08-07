package moe.nightfall.vic.integratedcircuits.tile;

import moe.nightfall.vic.integratedcircuits.DiskDrive.IDiskDrive;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket.EnumConnectionType;
import moe.nightfall.vic.integratedcircuits.cp.CircuitCache;
import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.net.PacketFloppyDisk;
import moe.nightfall.vic.integratedcircuits.net.pcb.PacketPCBChangeInput;
import moe.nightfall.vic.integratedcircuits.net.pcb.PacketPCBSimulation;
import moe.nightfall.vic.integratedcircuits.net.pcb.PacketPCBUpdate;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityCAD extends TileEntityContainer implements ICircuit, IDiskDrive {
	private ItemStack floppyStack;
	private CircuitData circuitData;
	public CircuitCache cache = new CircuitCache(this);

	// Used for the GUI.
	public float scale = 0.33F;
	public double offX = 0;
	public double offY = 0;

	public int[] in = new int[4];
	public int[] out = new int[4];
	private boolean updateIO;

	// Simulation settings
	private boolean pausing = false;
	private boolean step = false;

	public boolean isPausing() {
		return pausing;
	}

	public void setPausing(boolean pausing) {
		this.pausing = pausing;
		this.step = false;
	}

	public void step() {
		this.step = true;
	}

	public void sendSimulationState() {
		CommonProxy.networkWrapper.sendToServer(new PacketPCBSimulation(step, pausing, xCoord, yCoord, zCoord));
	}

	public void setup(int size) {
		circuitData = new CircuitData(size, this);
		circuitData.clear(size);
	}

	@Override
	public void updateEntity() {
		// Update the matrix in case there is at least one player watching.
		if (!worldObj.isRemote && playersUsing > 0) {
			if (step || !pausing) {
				getCircuitData().updateMatrix();
				step = false;
			}

			if (getCircuitData().checkUpdate()) {
				CommonProxy.networkWrapper.sendToAllAround(
						new PacketPCBUpdate(getCircuitData(), xCoord, yCoord, zCoord), new TargetPoint(
								worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 8));
			}
			if (updateIO) {
				updateIO = false;
				CommonProxy.networkWrapper.sendToAllAround(new PacketPCBChangeInput(false, out, circuitData
					.getProperties().getCon(), xCoord, yCoord, zCoord), new TargetPoint(
						worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 8));
			}
			markDirty();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		circuitData = CircuitData.readFromNBT(compound.getCompoundTag("circuit"), this);
		in = compound.getIntArray("in");
		out = compound.getIntArray("out");
		pausing = compound.getBoolean("pausing");
		NBTTagCompound stackCompound = compound.getCompoundTag("floppyStack");
		floppyStack = ItemStack.loadItemStackFromNBT(stackCompound);
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setTag("circuit", circuitData.writeToNBT(new NBTTagCompound()));
		compound.setIntArray("in", in);
		compound.setIntArray("out", out);
		compound.setBoolean("pausing", pausing);
		NBTTagCompound stackCompound = new NBTTagCompound();
		if (floppyStack != null)
			floppyStack.writeToNBT(stackCompound);
		compound.setTag("floppyStack", stackCompound);
	}

	@Override
	public boolean getInputFromSide(ForgeDirection dir, int frequency) {
		return (in[MiscUtils.getSide(dir)] & 1 << frequency) != 0;
	}

	public boolean getOutputToSide(ForgeDirection dir, int frequency) {
		return (out[MiscUtils.getSide(dir)] & 1 << frequency) != 0;
	}

	@SideOnly(Side.CLIENT)
	public void setInputFromSide(ForgeDirection dir, int frequency, boolean output) {
		EnumConnectionType mode = circuitData.getProperties().getModeAtSide(MiscUtils.getSide(dir));
		if (mode != EnumConnectionType.SIMPLE || frequency == 0) {
			int[] i = this.in.clone();
			if (mode == EnumConnectionType.ANALOG)
				i[MiscUtils.getSide(dir)] = 0;
			if (output)
				i[MiscUtils.getSide(dir)] |= 1 << frequency;
			else {
				if (mode == EnumConnectionType.ANALOG)
					i[MiscUtils.getSide(dir)] = 1;
				else
					i[MiscUtils.getSide(dir)] &= ~(1 << frequency);
			}
			CommonProxy.networkWrapper.sendToServer(new PacketPCBChangeInput(true, i, circuitData.getProperties()
				.getCon(), xCoord, yCoord, zCoord));
		}
	}

	@SideOnly(Side.CLIENT)
	public void setInputMode(int side, EnumConnectionType mode) {
		int con = circuitData.getProperties().setModeAtSide(side, mode);
		int i[] = this.in.clone();
		i[side] = mode == EnumConnectionType.ANALOG ? 1 : 0;
		CommonProxy.networkWrapper.sendToServer(new PacketPCBChangeInput(true, i, con, xCoord, yCoord, zCoord));
	}

	@Override
	public void setOutputToSide(ForgeDirection dir, int frequency, boolean output) {
		if (output)
			out[MiscUtils.getSide(dir)] |= 1 << frequency;
		else
			out[MiscUtils.getSide(dir)] &= ~(1 << frequency);
		updateIO = true;
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int id) {
		return id == 0 ? floppyStack : null;
	}

	@Override
	public ItemStack decrStackSize(int id, int amount) {
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int id) {
		return getStackInSlot(id);
	}

	@Override
	public void setInventorySlotContents(int id, ItemStack stack) {
		if (id == 0)
			floppyStack = stack;
		markDirty();
	}

	@Override
	public String getInventoryName() {
		return null;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isItemValidForSlot(int id, ItemStack stack) {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox() {
		return MiscUtils.getRotatedInstance(
				AxisAlignedBB.getBoundingBox(1 / 16F, 1 / 16F, -1 / 16F, 13 / 16F, 3 / 16F, 1 / 16F), rotation);
	}

	@Override
	public ItemStack getDisk() {
		return getStackInSlot(0);
	}

	@Override
	public void setDisk(ItemStack stack) {
		setInventorySlotContents(0, stack);
		if (!worldObj.isRemote)
			CommonProxy.networkWrapper.sendToDimension(new PacketFloppyDisk(xCoord, yCoord, zCoord, stack),
					worldObj.provider.dimensionId);
	}

	@Override
	public CircuitData getCircuitData() {
		return circuitData;
	}

	@Override
	public void setCircuitData(CircuitData data) {
		this.circuitData = data;
	}

	public boolean rotate() {
		this.rotation = rotation + 1 & 3;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		return true;
	}
}
