package vic.mod.integratedcircuits.ic.part.cell;


public class PartInvertCell extends PartBufferCell
{
	@Override
	protected void calcOutput() 
	{
		super.calcOutput();
		setOutput(!getOutput());
	}
}