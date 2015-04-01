package moe.nightfall.vic.integratedcircuits.ic.part.logic;

import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;

public class PartNANDGate extends PartANDGate
{
	@Override
	public void calcOutput(Vec2 pos, ICircuit parent) 
	{
		super.calcOutput(pos, parent);
		setOutput(pos, parent, !getOutput(pos, parent));
	}
}