package vic.mod.integratedcircuits.ic.part.logic;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.part.PartSimpleGate;
import vic.mod.integratedcircuits.misc.MiscUtils;

public class PartXORGate extends PartSimpleGate
{
	@Override
	public boolean canConnectToSide(ForgeDirection side) 
	{
		return MiscUtils.rotn(side, -getRotation()) != ForgeDirection.SOUTH;
	}
	
	@Override
	protected boolean hasOutputToSide(ForgeDirection fd) 
	{
		return fd == ForgeDirection.NORTH;
	}
	
	@Override
	protected void calcOutput() 
	{
		setOutput(getInputFromSide(MiscUtils.rotn(ForgeDirection.EAST, getRotation()))
			!= getInputFromSide(MiscUtils.rotn(ForgeDirection.WEST, getRotation())));
	}
}