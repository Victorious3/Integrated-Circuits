package vic.mod.integratedcircuits.ic.part.logic;


public class PartXNORGate extends PartXORGate
{
	@Override
	public void calcOutput() 
	{
		super.calcOutput();
		setOutput(!getOutput());
	}
}