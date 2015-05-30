package moe.nightfall.vic.integratedcircuits.api;

import moe.nightfall.vic.integratedcircuits.api.gate.IGateRegistry;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketProvider;
import net.minecraft.world.World;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;

/**
 * Integrated Circuits API.
 * 
 * @author Vic Nightfall
 */
public class IntegratedCircuitsAPI {
	private static IAPI instance;

	public static enum Type {
		TILE("moe.nightfall.vic.integratedcircuits.tile.TileEntitySocket"),
		TILE_FMP("moe.nightfall.vic.integratedcircuits.tile.FMPartGate"),
		BLOCK("moe.nightfall.vic.integratedcircuits.tile.BlockSocket");

		public final String classname;

		Type(String classname) {
			this.classname = classname;
		}
	}

	public static IAPI getInstance() {
		if (instance == null)
			throw new RuntimeException("Integrated Circuits not installed, aborting!");
		return instance;
	}

	public static ISocket getSocketAt(World world, BlockCoord pos, int side) {
		return getInstance().getSocketAt(world, pos, side);
	}

	public static void registerSocketProvider(ISocketProvider provider) {
		getInstance().registerSocketProvider(provider);
	}

	public static MCDataOutput getWriteStream(World world, BlockCoord pos, int side) {
		return getInstance().getWriteStream(world, pos, side);
	}

	public static IGateRegistry getGateRegistry() {
		return getInstance().getGateRegistry();
	}

	public static int updateRedstoneInput(ISocket socket, int side) {
		return getInstance().updateRedstoneInput(socket, side);
	}

	public static byte[] updateBundledInput(ISocket socket, int side) {
		return getInstance().updateBundledInput(socket, side);
	}
}
