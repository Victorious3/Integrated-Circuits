package vic.mod.integratedcircuits.ic.parts.cell;


public class PartInvertCell extends PartBufferCell
{
	@Override
	protected void calcOutput() 
	{
		super.calcOutput();
		setOutput(!getOutput());
	}
}