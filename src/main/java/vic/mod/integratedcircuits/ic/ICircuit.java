package vic.mod.integratedcircuits.ic;

import net.minecraftforge.common.util.ForgeDirection;

public interface ICircuit 
{
	public CircuitData getCircuitData();
	
	public void setCircuitData(CircuitData data);
	
	public boolean getInputFromSide(ForgeDirection dir, int frequency);
	
	public void setOutputToSide(ForgeDirection dir, int frequency, boolean output);
}
