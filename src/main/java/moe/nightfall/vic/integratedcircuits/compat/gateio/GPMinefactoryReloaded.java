package moe.nightfall.vic.integratedcircuits.compat.gateio;

import moe.nightfall.vic.integratedcircuits.api.gate.GateIOProvider;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket.EnumConnectionType;
import moe.nightfall.vic.integratedcircuits.tile.TileEntitySocket;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import powercrystals.minefactoryreloaded.api.rednet.IRedNetNetworkContainer;
import powercrystals.minefactoryreloaded.api.rednet.IRedNetOmniNode;
import powercrystals.minefactoryreloaded.api.rednet.connectivity.RedNetConnectionType;
import codechicken.lib.vec.BlockCoord;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.Method;

@Interface(iface = "powercrystals.minefactoryreloaded.api.rednet.IRedNetOmniNode", modid = "MineFactoryReloaded")
public class GPMinefactoryReloaded extends GateIOProvider implements IRedNetOmniNode {

	@Override
	@Method(modid = "MineFactoryReloaded")
	public byte[] calculateBundledInput(int side, int rotation, int abs, BlockCoord offset) {
		// Ignore MFR tiles, they update separately.
		Block block = socket.getWorld().getBlock(offset.x, offset.y, offset.z);
		if (block instanceof IRedNetNetworkContainer)
			return socket.getInput()[side];
		return null;
	}

	@Override
	@Method(modid = "MineFactoryReloaded")
	public int calculateRedstoneInput(int side, int rotation, int abs, BlockCoord offset) {
		// Ignore MFR tiles, they update separately.
		Block block = socket.getWorld().getBlock(offset.x, offset.y, offset.z);
		if (block instanceof IRedNetNetworkContainer)
			return socket.getRedstoneInput(side);
		return 0;
	}

	@Override
	@Method(modid = "MineFactoryReloaded")
	public RedNetConnectionType getConnectionType(World world, int x, int y, int z, ForgeDirection fd) {
		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(x, y, z);
		ISocket socket = te.getSocket();

		int side = fd.ordinal();
		if ((side & 6) == (socket.getSide() & 6))
			return RedNetConnectionType.None;
		int rel = socket.getSideRel(side);

		EnumConnectionType type = socket.getConnectionTypeAtSide(rel);
		if (type.isBundled())
			return RedNetConnectionType.PlateAll;
		else if (type.isRedstone())
			return RedNetConnectionType.PlateSingle;
		return RedNetConnectionType.None;
	}

	@Override
	@Method(modid = "MineFactoryReloaded")
	public void onInputsChanged(World world, int x, int y, int z, ForgeDirection fd, int[] inputValues) {
		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(x, y, z);
		ISocket socket = te.getSocket();

		int side = fd.ordinal();
		if ((side & 6) == (socket.getSide() & 6))
			return;
		int rel = socket.getSideRel(side);

		socket.updateInputPre();
		for (int i = 0; i < 16; i++)
			socket.setInput(rel, i, (byte) (inputValues[i] & 0xFF));
		socket.updateInputPost();
	}

	@Override
	@Method(modid = "MineFactoryReloaded")
	public void onInputChanged(World world, int x, int y, int z, ForgeDirection fd, int inputValue) {
		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(x, y, z);
		ISocket socket = te.getSocket();

		int side = fd.ordinal();
		if ((side & 6) == (socket.getSide() & 6))
			return;
		int rel = socket.getSideRel(side);

		socket.updateInputPre();
		socket.setInput(rel, 0, (byte) (inputValue & 0xFF));
		socket.updateInputPost();
	}

	@Override
	@Method(modid = "MineFactoryReloaded")
	public int[] getOutputValues(World world, int x, int y, int z, ForgeDirection fd) {
		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(x, y, z);
		ISocket socket = te.getSocket();

		int side = fd.ordinal();
		if ((side & 6) == (socket.getSide() & 6))
			return new int[16];
		int rel = socket.getSideRel(side);

		// Convert byte array output to int array, just for you MFR
		int[] out = new int[16];
		byte[] bout = socket.getOutput()[rel];
		for (int i = 0; i < 16; i++)
			out[i] = bout[i] & 255;

		return out;
	}

	@Override
	@Method(modid = "MineFactoryReloaded")
	public int getOutputValue(World world, int x, int y, int z, ForgeDirection fd, int subnet) {
		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(x, y, z);
		ISocket socket = te.getSocket();

		int side = fd.ordinal() ^ 1;
		if ((side & 6) == (socket.getSide() & 6))
			return 0;
		int rel = socket.getSideRel(side);

		return socket.getRedstoneOutput(rel);
	}
}