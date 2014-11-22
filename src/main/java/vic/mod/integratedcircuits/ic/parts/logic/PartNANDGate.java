package vic.mod.integratedcircuits.ic.parts.logic;

public class PartNANDGate extends PartANDGate
{
	@Override
	public void calcOutput() 
	{
		super.calcOutput();
		setOutput(!getOutput());
	}
}