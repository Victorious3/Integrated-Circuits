package vic.mod.integratedcircuits.ic.part;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.misc.MiscUtils;

public class PartMultiplexer extends PartSimpleGate
{
	@Override
	protected void calcOutput() 
	{
		if(getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, getRotation())))
			setOutput(getInputFromSide(MiscUtils.rotn(ForgeDirection.WEST, getRotation())));
		else setOutput(getInputFromSide(MiscUtils.rotn(ForgeDirection.EAST, getRotation())));
	}

	@Override
	protected boolean hasOutputToSide(ForgeDirection fd) 
	{
		return fd == ForgeDirection.NORTH;
	}	
}