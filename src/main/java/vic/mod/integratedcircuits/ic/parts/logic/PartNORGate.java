package vic.mod.integratedcircuits.ic.parts.logic;

public class PartNORGate extends PartORGate
{
	@Override
	public void calcOutput() 
	{
		super.calcOutput();
		setOutput(!getOutput());
	}	
}