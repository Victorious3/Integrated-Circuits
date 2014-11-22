package vic.mod.integratedcircuits.ic.parts.logic;


public class PartXNORGate extends PartXORGate
{
	@Override
	public void calcOutput() 
	{
		super.calcOutput();
		setOutput(!getOutput());
	}
}