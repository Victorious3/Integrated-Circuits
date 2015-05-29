package moe.nightfall.vic.integratedcircuits.compat.gateio;

import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI.Type;
import moe.nightfall.vic.integratedcircuits.api.gate.IGateRegistry;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;

public final class GateIO {
	private GateIO() {
	}

	public static void initialize() {
		IGateRegistry registry = IntegratedCircuitsAPI.getGateRegistry();

		registry.registerGateIOProvider(new GPProjectRedTile(), Type.TILE);
		registry.registerGateIOProvider(new GPProjectRedFMP(), Type.TILE_FMP);
		registry.registerGateIOProvider(new GPBluePower(), Type.TILE, Type.TILE_FMP);
		registry.registerGateIOProvider(new GPRedLogic(), Type.TILE);
		registry.registerGateIOProvider(new GPOpenComputers(), Type.TILE);
		registry.registerGateIOProvider(new GPMinefactoryReloaded(), Type.BLOCK);
		registry.registerGateIOProvider(new GPComputerCraft(), Type.BLOCK);
	}

	public static byte[] getBundledSignal(ISocket socket, int dir) {

		if ((dir & 6) == (socket.getSide() & 6))
			return null;
		int rot = socket.getSideRel(dir);
		if (!socket.getConnectionTypeAtSide(rot).isBundled())
			return null;
		return socket.getOutput()[rot];
	}

	public static int vanillaToSide(int vside) {
		return GateIO.vanillaSideMap[vside + 1];
	}

	public static final int[] vanillaSideMap = { 1, 2, 5, 3, 4 };
}
