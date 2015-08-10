package moe.nightfall.vic.integratedcircuits.compat.buildcraft;

import io.netty.buffer.ByteBuf;

import java.util.List;

import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.IGate;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.transport.IPipeTile;
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

import cpw.mods.fml.common.network.ByteBufUtils;

public class GatePipePluggable extends PipePluggable implements ISocketWrapper {

	private ISocket socket = IntegratedCircuitsAPI.getGateRegistry().createSocketInstance(this);
	private IPipeTile pipe;
	private BlockCoord pos;
	private World world;
	private ForgeDirection dir;
	private int scheduledTick = -1;
	private AxisAlignedBB boundingBox;

	public GatePipePluggable() {
	}

	public GatePipePluggable(ItemStack stack, BlockCoord pos, World world, ForgeDirection dir) {
		this.pos = pos;
		this.world = world;
		this.dir = dir;
		socket.setGate(stack, null);
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
			onAttachedPipe(pipe, direction);
		}
		socket.update();
		if (scheduledTick > -1) {
			scheduledTick--;
			if (scheduledTick <= -1) {
				socket.scheduledTick();
				scheduledTick = -1;
			}
		}
	}

	@Override
	public void onAttachedPipe(IPipeTile pipe, ForgeDirection direction) {
		IGate gate = socket.getGate();
		this.pipe = pipe;
		this.dir = direction;
		this.world = pipe.getWorld();
		this.pos = new BlockCoord(pipe.x(), pipe.y(), pipe.z());
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
				.with(Rotation.sideOrientation(side.getOpposite().ordinal(), 0).at(Vector3.center))).toAABB();
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
		return IntegratedCircuitsAPI.getWriteStream(getWorld(), getPos(), dir.ordinal());
	}

	@Override
	public World getWorld() {
		return world;
	}

	@Override
	public void notifyBlocksAndChanges() {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyPartChange() {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

	}

	@Override
	public int updateRedstoneInput(int side) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte[] updateBundledInput(int side) {
		// TODO Auto-generated method stub
		return new byte[16];
	}

	@Override
	public void scheduleTick(int delay) {
		scheduledTick = delay;
	}

	@Override
	public int strongPowerLevel(int side) {
		// TODO Auto-generated method stub
		return 0;
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
