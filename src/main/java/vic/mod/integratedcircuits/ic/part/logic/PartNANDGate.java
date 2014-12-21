package vic.mod.integratedcircuits.ic.part.logic;

import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartNANDGate extends PartANDGate
{
	@Override
	public void calcOutput(Vec2 pos, ICircuit parent) 
	{
		super.calcOutput(pos, parent);
		setOutput(pos, parent, !getOutput(pos, parent));
	}
}