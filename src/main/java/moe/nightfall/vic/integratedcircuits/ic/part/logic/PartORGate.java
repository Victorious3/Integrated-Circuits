package moe.nightfall.vic.integratedcircuits.ic.part.logic;

import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.ic.part.Part3I1O;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraftforge.common.util.ForgeDirection;

public class PartORGate extends Part3I1O
{
	@Override
	public Category getCategory() {
		return Category.NGATE;
	}


	@Override
	public void calcOutput(Vec2 pos, ICircuit parent)
	{
		ForgeDirection s3 = toExternal(pos, parent, ForgeDirection.SOUTH);
		ForgeDirection s4 = toExternal(pos, parent, ForgeDirection.EAST);
		ForgeDirection s5 = s4.getOpposite();
		
		setOutput(pos, parent, getInputFromSide(pos, parent, s3) || getInputFromSide(pos, parent, s4) || getInputFromSide(pos, parent, s5));
	}
}
