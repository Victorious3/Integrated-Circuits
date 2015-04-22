package moe.nightfall.vic.integratedcircuits.api;

public abstract class GateIOProvider
{
	public ISocket socket;
	
	public byte[] calculateBundledInput(int side) {
		return null;
	}
	
	public int calculateRedstoneInput(int side) {
		return 0;
	}
}
