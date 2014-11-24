package vic.mod.integratedcircuits.ic.part.logic;

public class PartNORGate extends PartORGate
{
	@Override
	public void calcOutput() 
	{
		super.calcOutput();
		setOutput(!getOutput());
	}	
}