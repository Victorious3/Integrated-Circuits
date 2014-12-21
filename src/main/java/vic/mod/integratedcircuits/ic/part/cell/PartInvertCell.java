package vic.mod.integratedcircuits.ic.part.cell;

import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartInvertCell extends PartBufferCell
{
	@Override
	protected void calcOutput(Vec2 pos, ICircuit parent) 
	{
		super.calcOutput(pos, parent);
		setOutput(pos, parent, !getOutput(pos, parent));
	}
}