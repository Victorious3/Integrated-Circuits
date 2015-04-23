package moe.nightfall.vic.integratedcircuits.api.gate;

import codechicken.lib.vec.BlockCoord;

public abstract class GateIOProvider {
	public ISocket socket;

	public byte[] calculateBundledInput(int side, BlockCoord offset, int abs) {
		return null;
	}

	public int calculateRedstoneInput(int side, BlockCoord offset, int abs) {
		return 0;
	}
}
