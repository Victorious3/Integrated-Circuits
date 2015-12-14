package moe.nightfall.vic.integratedcircuits.compat;

import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import net.minecraft.world.World;


import com.bluepowermod.api.BPApi;
import com.bluepowermod.api.misc.MinecraftColor;
import com.bluepowermod.api.connect.ConnectionType;
import com.bluepowermod.api.connect.IConnectionCache;
import com.bluepowermod.api.wire.redstone.IBundledDevice;
import com.bluepowermod.api.wire.redstone.IRedstoneDevice;

public class BPDevice implements IBundledDevice, IRedstoneDevice {
	private final ISocket socket;

	private final IConnectionCache<IBundledDevice> cacheBundled;
	private final IConnectionCache<IRedstoneDevice> cacheSimple;

	public BPDevice(ISocket socket) {
		this.socket = socket;
		this.cacheBundled = BPApi.getInstance().getRedstoneApi().createBundledConnectionCache(this);
		this.cacheSimple = BPApi.getInstance().getRedstoneApi().createRedstoneConnectionCache(this);
	}

	@Override
	public int getX() {
		return socket.getPos().x;
	}

	@Override
	public int getY() {
		return socket.getPos().y;
	}

	@Override
	public int getZ() {
		return socket.getPos().z;
	}

	@Override
	public World getWorld() {
		return socket.getWorld();
	}

	@Override
	public boolean canConnect(EnumFacing side, IBundledDevice dev, ConnectionType type) {
		return socket.getConnectionTypeAtSide(socket.getSideRel(side.ordinal())).isBundled();
	}

	@Override
	public IConnectionCache<? extends IBundledDevice> getBundledConnectionCache() {
		return cacheBundled;
	}

	@Override
	public byte[] getBundledOutput(EnumFacing side) {
		return socket.getOutput()[(socket.getSideRel(side.ordinal()))];
	}

	@Override
	public void setBundledPower(EnumFacing side, byte[] power) {
		socket.updateInputPre();
		socket.getInput()[socket.getSideRel(side.ordinal())] = power;
	}

	@Override
	public byte[] getBundledPower(EnumFacing side) {
		return socket.getInput()[socket.getSideRel(side.ordinal())];
	}

	@Override
	public void onBundledUpdate() {
		socket.updateInputPost();
	}

	@Override
	public MinecraftColor getBundledColor(EnumFacing side) {
		return MinecraftColor.ANY;
	}

	@Override
	public boolean isNormalFace(EnumFacing side) {
		return true;
	}

	@Override
	public boolean canConnect(EnumFacing side, IRedstoneDevice dev, ConnectionType type) {
		return socket.getConnectionTypeAtSide(socket.getSideRel(side.ordinal())).isRedstone();
	}

	@Override
	public IConnectionCache<? extends IRedstoneDevice> getRedstoneConnectionCache() {
		return cacheSimple;
	}

	@Override
	public byte getRedstonePower(EnumFacing side) {
		int out = socket.getRedstoneOutput(socket.getSideRel(side.ordinal()));
		return (byte) (17 * out); //BluePower uses unsigned byte values from 0 to 255
	}

	@Override
	public void setRedstonePower(EnumFacing side, byte power) {
		socket.updateInputPre();
		power = (byte) ((power & 0xFF) / 17); //Convert signed byte to unsigned int
		socket.setInput(socket.getSideRel(side.ordinal()), 0, power);
	}

	@Override
	public void onRedstoneUpdate() {
		socket.updateInputPost();
	}
}
