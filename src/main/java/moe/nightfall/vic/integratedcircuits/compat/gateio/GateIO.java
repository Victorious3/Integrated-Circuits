package moe.nightfall.vic.integratedcircuits.compat.gateio;

import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.IGateRegistry;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;

public final class GateIO {
	private GateIO() {
	}

	public static void initialize() {
		IGateRegistry registry = IntegratedCircuitsAPI.getGateRegistry();

		registry.registerGateIOProvider(new GPProjectRed.GPProjectRedTile(), IntegratedCircuitsAPI.TILE);
		registry.registerGateIOProvider(new GPProjectRed.GPProjectRedFMP(), IntegratedCircuitsAPI.TILE_FMP);
		registry.registerGateIOProvider(new GPBluePower(), IntegratedCircuitsAPI.TILE, IntegratedCircuitsAPI.TILE_FMP);
		registry.registerGateIOProvider(new GPRedLogic(), IntegratedCircuitsAPI.TILE);
		registry.registerGateIOProvider(new GPOpenComputers(), IntegratedCircuitsAPI.TILE);
		registry.registerGateIOProvider(new GPMinefactoryReloaded(), IntegratedCircuitsAPI.BLOCK);
		registry.registerGateIOProvider(new GPComputerCraft(), IntegratedCircuitsAPI.BLOCK);
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
