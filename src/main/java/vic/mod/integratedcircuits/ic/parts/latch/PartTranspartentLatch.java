package vic.mod.integratedcircuits.ic.parts.latch;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.parts.PartGate;
import vic.mod.integratedcircuits.util.MiscUtils;

public class PartTranspartentLatch extends PartGate
{
	@Override
	public void onInputChange(ForgeDirection side) 
	{
		super.onInputChange(side);
		ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
		if(s2 == ForgeDirection.SOUTH || (s2 == ForgeDirection.WEST && getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, getRotation())))) 
		{
			if(getInputFromSide(MiscUtils.rotn(ForgeDirection.WEST, getRotation()))) setState(getState() | 128);
			else setState(getState() & ~128);
		}
	}

	@Override
	public boolean getOutputToSide(ForgeDirection side) 
	{
		ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
		if(s2 == ForgeDirection.NORTH || s2 == ForgeDirection.EAST) return (getState() & 128) > 0;
		return false;
	}
}