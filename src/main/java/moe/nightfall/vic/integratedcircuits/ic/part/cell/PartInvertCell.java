package moe.nightfall.vic.integratedcircuits.ic.part.cell;

import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;

public class PartInvertCell extends PartBufferCell
{
	@Override
	protected void calcOutput(Vec2 pos, ICircuit parent) 
	{
		super.calcOutput(pos, parent);
		setOutput(pos, parent, !getOutput(pos, parent));
	}
}