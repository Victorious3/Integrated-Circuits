package moe.nightfall.vic.integratedcircuits.gate;


public interface IGatePeripheralProvider 
{
	public boolean hasPeripheral(int side);
	
	public GatePeripheral getPeripheral();
}
