package moe.nightfall.vic.integratedcircuits.api.gate;

import codechicken.lib.vec.BlockCoord;

public abstract class GateIOProvider {
	public ISocket socket;

	public byte[] calculateBundledInput(int side, int rotation, int abs, BlockCoord offset) {
		return null;
	}

	public int calculateRedstoneInput(int side, int rotation, int abs, BlockCoord offset) {
		return 0;
	}
}
