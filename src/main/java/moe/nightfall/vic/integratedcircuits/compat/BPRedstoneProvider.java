package moe.nightfall.vic.integratedcircuits.compat;

import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import net.minecraft.world.World;

import codechicken.lib.vec.BlockCoord;

import com.bluepowermod.api.BPApi;
import com.bluepowermod.api.wire.redstone.IBundledDevice;
import com.bluepowermod.api.wire.redstone.IBundledDeviceWrapper;
import com.bluepowermod.api.wire.redstone.IRedstoneDevice;
import com.bluepowermod.api.wire.redstone.IRedstoneProvider;

public class BPRedstoneProvider implements IRedstoneProvider {
	public BPRedstoneProvider() {
		BPApi.getInstance().getRedstoneApi().registerRedstoneProvider(this);
	}

	@Override
	public IRedstoneDevice getRedstoneDeviceAt(World world, int x, int y, int z, EnumFacing side,
			EnumFacing face) {
		return (IRedstoneDevice) getBundledDeviceAt(world, x, y, z, side, face);
	}

	@Override
	public IBundledDevice getBundledDeviceAt(World world, int x, int y, int z, EnumFacing side, EnumFacing face) {
		ISocket socket = IntegratedCircuitsAPI.getSocketAt(world, new BlockCoord(x, y, z), side.ordinal());
		if (socket != null && socket.getWrapper() instanceof IBundledDeviceWrapper)
			return ((IBundledDeviceWrapper) socket.getWrapper()).getBundledDeviceOnSide(face);
		return null;
	}
}
