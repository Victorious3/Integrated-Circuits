package vic.mod.integratedcircuits.ic.part.logic;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.ic.part.Part1I3O;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartNOTGate extends Part1I3O
{
	@Override
	public void calcOutput(Vec2 pos, ICircuit parent) 
	{
		setOutput(pos, parent, !getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH)));
	}
}