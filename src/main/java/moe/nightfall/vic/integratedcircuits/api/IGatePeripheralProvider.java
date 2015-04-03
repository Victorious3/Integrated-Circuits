package moe.nightfall.vic.integratedcircuits.api;

import moe.nightfall.vic.integratedcircuits.gate.GatePeripheral;


public interface IGatePeripheralProvider 
{
	public boolean hasPeripheral(int side);
	
	public GatePeripheral getPeripheral();
}
