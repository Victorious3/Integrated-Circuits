package vic.mod.integratedcircuits.gate;


public interface IGatePeripheralProvider 
{
	public boolean hasPeripheral(int side);
	
	public GatePeripheral getPeripheral();
}
