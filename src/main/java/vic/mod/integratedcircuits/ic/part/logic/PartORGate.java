package vic.mod.integratedcircuits.ic.part.logic;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.ic.part.Part3I1O;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartORGate extends Part3I1O
{
	@Override
	public void calcOutput(Vec2 pos, ICircuit parent)
	{
		ForgeDirection s3 = MiscUtils.rotn(ForgeDirection.SOUTH, getRotation(pos, parent));
		ForgeDirection s4 = MiscUtils.rotn(ForgeDirection.EAST, getRotation(pos, parent));
		ForgeDirection s5 = s4.getOpposite();
		
		setOutput(pos, parent, getInputFromSide(pos, parent, s3) || getInputFromSide(pos, parent, s4) || getInputFromSide(pos, parent, s5));
	}
}