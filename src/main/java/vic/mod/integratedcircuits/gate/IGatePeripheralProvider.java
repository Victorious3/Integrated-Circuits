package vic.mod.integratedcircuits.gate;

import dan200.computercraft.api.peripheral.IPeripheral;

public interface IGatePeripheralProvider 
{
	public IPeripheral getPeripheral(int side);
}
