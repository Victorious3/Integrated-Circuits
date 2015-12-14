package moe.nightfall.vic.integratedcircuits.compat.gateio;

import moe.nightfall.vic.integratedcircuits.api.gate.GateIOProvider;
import moe.nightfall.vic.integratedcircuits.compat.BPDevice;

import codechicken.lib.vec.BlockCoord;

import com.bluepowermod.api.BPApi;
import com.bluepowermod.api.wire.redstone.IBundledDevice;
import com.bluepowermod.api.wire.redstone.IBundledDeviceWrapper;
import com.bluepowermod.api.wire.redstone.IRedstoneApi;

import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.Method;

@Interface(iface = "com.bluepowermod.api.wire.redstone.IBundledDeviceWrapper", modid = "bluepower")
public class GPBluePower extends GateIOProvider implements IBundledDeviceWrapper {

	@Override
	@Method(modid = "bluepower")
	public byte[] calculateBundledInput(int side, int rotation, int abs, BlockCoord offset) {
		IRedstoneApi redstoneAPI = BPApi.getInstance().getRedstoneApi();
		IBundledDevice device = redstoneAPI.getBundledDevice(socket.getWorld(), offset.x, offset.y, offset.z,
				EnumFacing.getOrientation(socket.getSide()), EnumFacing.UNKNOWN);

		if (device != null)
			return device.getBundledOutput(EnumFacing.getOrientation(abs ^ 1));

		return null;
	}

	@Override
	@Method(modid = "bluepower")
	public IBundledDevice getBundledDeviceOnSide(EnumFacing side) {
		IBundledDevice device = socket.get("bpDevice");
		if (device == null) {
			device = new BPDevice(socket);
			socket.put("bpDevice", device);
		}
		return device;
	}
}