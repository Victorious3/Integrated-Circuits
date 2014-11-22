package vic.mod.integratedcircuits.ic.parts.logic;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.parts.Part1I3O;
import vic.mod.integratedcircuits.util.MiscUtils;

public class PartBufferGate extends Part1I3O
{
	@Override
	public void calcOutput() 
	{
		setOutput(getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, getRotation())));
	}
}