package vic.mod.integratedcircuits.ic.part.logic;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.part.Part3I1O;
import vic.mod.integratedcircuits.misc.MiscUtils;

public class PartORGate extends Part3I1O
{
	@Override
	public void calcOutput()
	{
		ForgeDirection s3 = MiscUtils.rotn(ForgeDirection.SOUTH, getRotation());
		ForgeDirection s4 = MiscUtils.rotn(ForgeDirection.EAST, getRotation());
		ForgeDirection s5 = s4.getOpposite();
		
		setOutput(getInputFromSide(s3) || getInputFromSide(s4) || getInputFromSide(s5));
	}
}