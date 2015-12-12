package moe.nightfall.vic.integratedcircuits.compat.buildcraft;

import io.netty.buffer.ByteBuf;

import java.util.List;

import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.PipeWire;
import buildcraft.api.transport.pluggable.IPipePluggableDynamicRenderer;
import buildcraft.api.transport.pluggable.IPipePluggableRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;

import com.google.common.collect.Lists;

import net.minecraftforge.fml.common.network.ByteBufUtils;

public class GatePipePluggable extends PipePluggable implements ISocketWrapper {

	private ISocket socket = IntegratedCircuitsAPI.getGateRegistry().createSocketInstance(this);
	private IPipeTile pipe;
	private BlockCoord pos;
	private World world;
	private int scheduledTick = -1;
	private AxisAlignedBB boundingBox;

	public GatePipePluggable() {
		System.out.println("Creating new instance");
	}

	public GatePipePluggable(ItemStack stack, BlockCoord pos, World world, ForgeDirection dir) {
		this.pos = pos;
		this.world = world;
		socket.setGate(stack, null);
		socket.setSide(dir.ordinal());
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		socket.readFromNBT(tag);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		socket.writeToNBT(tag);
	}

	@Override
	public void readData(ByteBuf data) {
		socket.readDesc(ByteBufUtils.readTag(data));
	}

	@Override
	public void writeData(ByteBuf data) {
		NBTTagCompound compound = new NBTTagCompound();
		socket.writeDesc(compound);
		ByteBufUtils.writeTag(data, compound);
	}

	@Override
	public void update(IPipeTile pipe, ForgeDirection direction) {
		if (this.pipe == null) {
			this.pipe = pipe;
			this.world = pipe.getWorld();
			this.pos = new BlockCoord(pipe.x(), pipe.y(), pipe.z());
			socket.setSide(direction.ordinal());
		}
		socket.update();
		if (scheduledTick > -1) {
			scheduledTick--;
			if (scheduledTick <= -1) {
				socket.scheduledTick();
				scheduledTick = -1;
			}
		}
		updateInput();
	}

	@Override
	public ItemStack[] getDropItems(IPipeTile pipe) {
		List<ItemStack> list = Lists.newArrayList();
		socket.addDrops(list);
		return list.toArray(new ItemStack[list.size()]);
	}

	@Override
	public boolean isBlocking(IPipeTile pipe, ForgeDirection direction) {
		return true;
	}

	@Override
	public AxisAlignedBB getBoundingBox(ForgeDirection side) {
		if (boundingBox == null) {
			boundingBox = socket.getGate().getDimension().copy().apply(new Scale(1 / 16D)
				.with(new Translation(0, 0.85, 0))
				.with(new Scale(0.75).at(Vector3.center))
				.with(Rotation.sideOrientation(side.getOpposite().ordinal(), socket.getRotation()).at(Vector3.center))).toAABB();
		}
		return boundingBox;
	}

	@Override
	public IPipePluggableRenderer getRenderer() {
		return BCGateRenderer.instance;
	}

	@Override
	public IPipePluggableDynamicRenderer getDynamicRenderer() {
		return BCGateRenderer.instance;
	}

	@Override
	public void markRender() {
		if (pipe == null)
			return;
		pipe.scheduleRenderUpdate();
	}

	@Override
	public MCDataOutput getWriteStream(int disc) {
		return IntegratedCircuitsAPI.getWriteStream(getWorld(), getPos(), socket.getSide());
	}

	@Override
	public World getWorld() {
		return world;
	}

	@Override
	public void notifyBlocksAndChanges() {
		// Not implemented since we don't need to notify neighbor gates
	}

	@Override
	public void notifyPartChange() {
		// Called to update the output
		byte[][] output = socket.getOutput();
	}

	@Override
	public BlockCoord getPos() {
		return pos;
	}

	@Override
	public void destroy() {
		// Not implemented since it can't be destroyed by a neighbor update
	}

	@Override
	public void updateInput() {
		socket.updateInput();
	}

	@Override
	public int updateRedstoneInput(int side) {
		return getInput(side, PipeWire.RED);
	}

	private int getInput(int side, PipeWire wire) {
		if (pipe == null)
			return 0;
		return pipe.getPipe().isWireActive(wire) ? 15 : 0;
	}

	@Override
	public byte[] updateBundledInput(int side) {
		// TODO Not supported yet
		return new byte[16];
	}

	@Override
	public void scheduleTick(int delay) {
		scheduledTick = delay;
	}

	@Override
	public void sendDescription() {
		BlockCoord pos = getPos();
		// TODO Not sure if this is a valid action for pipes
		getWorld().markBlockForUpdate(pos.x, pos.y, pos.z);
	}

	@Override
	public ISocket getSocket() {
		return socket;
	}
}
