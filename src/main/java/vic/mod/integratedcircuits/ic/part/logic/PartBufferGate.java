package vic.mod.integratedcircuits.ic.part.logic;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.part.Part1I3O;
import vic.mod.integratedcircuits.misc.MiscUtils;

public class PartBufferGate extends Part1I3O
{
	@Override
	public void calcOutput() 
	{
		setOutput(getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, getRotation())));
	}
}