package moe.nightfall.vic.integratedcircuits.compat.gateio;

import moe.nightfall.vic.integratedcircuits.api.gate.GateIOProvider;
import moe.nightfall.vic.integratedcircuits.api.gate.IGatePeripheralProvider;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.tile.TileEntitySocket;
import net.minecraft.world.World;
import codechicken.lib.vec.BlockCoord;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.common.Optional.Method;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.api.redstone.IBundledRedstoneProvider;

@InterfaceList({
		@Interface(iface = "dan200.computercraft.api.redstone.IBundledRedstoneProvider", modid = "ComputerCraft"),
		@Interface(iface = "dan200.computercraft.api.peripheral.IPeripheralProvider", modid = "ComputerCraft")
})
public class GPComputerCraft extends GateIOProvider implements IBundledRedstoneProvider, IPeripheralProvider {

	@Override
	@Method(modid = "ComputerCraft")
	public byte[] calculateBundledInput(int side, int rotation, int abs, BlockCoord offset) {
		int input = ComputerCraftAPI.getBundledRedstoneOutput(socket.getWorld(), offset.x, offset.y, offset.z, abs ^ 1);
		if (input > 0) {
			// digital to analog
			byte[] convInput = new byte[16];
			for (int i = 0; i < 16; i++) {
				convInput[i] = (byte) (input & 1);
				input >>= 1;
			}
			return convInput;
		}
		return null;
	}

	@Override
	@Method(modid = "ComputerCraft")
	public int getBundledRedstoneOutput(World world, int x, int y, int z, int side) {
		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(x, y, z);
		ISocket socket = te.getSocket();

		if ((side & 6) == (socket.getSide() & 6))
			return -1;
		int rel = socket.getSideRel(side);

		// convert analog to digital
		int out = 0;
		for (int i = 0; i < 16; i++)
			out |= (socket.getBundledOutput(side, i) != 0 ? 1 : 0) << i;
		return out;
	}

	@Override
	@Method(modid = "ComputerCraft")
	public IPeripheral getPeripheral(World world, int x, int y, int z, int side) {
		ISocket socket = ((TileEntitySocket) world.getTileEntity(x, y, z)).getSocket();
		if (socket.getGate() instanceof IGatePeripheralProvider) {
			IGatePeripheralProvider provider = (IGatePeripheralProvider) socket.getGate();
			return provider.hasPeripheral(side) ? provider.getPeripheral() : null;
		}
		return null;
	}
}