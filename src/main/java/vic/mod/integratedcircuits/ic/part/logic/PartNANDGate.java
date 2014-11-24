package vic.mod.integratedcircuits.ic.part.logic;

public class PartNANDGate extends PartANDGate
{
	@Override
	public void calcOutput() 
	{
		super.calcOutput();
		setOutput(!getOutput());
	}
}