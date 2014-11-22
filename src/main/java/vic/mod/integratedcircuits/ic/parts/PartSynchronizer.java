package vic.mod.integratedcircuits.ic.parts;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.util.MiscUtils;

//TODO Is currently giving a one tick pulse, might cause problems with other gates.
public class PartSynchronizer extends PartGate
{
	@Override
	public void onInputChange(ForgeDirection side) 
	{
		updateInput();
		ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
		boolean input = getInputFromSide(s2);
		
		if(s2 == ForgeDirection.SOUTH && getInputFromSide(s2)) setState(getState() & ~896);
		else if(s2 == ForgeDirection.EAST && !getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, getRotation())) && input) 
			setState(getState() | 128);
		else if(s2 == ForgeDirection.WEST && !getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, getRotation())) && input) 
			setState(getState() | 256);
		
		if((getState() & 384) >> 7 == 3) 
		{
			setState(getState() & ~384);
			setState(getState() | 512);
			scheduleTick();
		}
	}

	@Override
	public void onScheduledTick()
	{
		notifyNeighbours();
		if((getState() & 512) > 0)
		{
			setState(getState() & ~512);
			scheduleTick();
		}
	}

	@Override
	public boolean getOutputToSide(ForgeDirection side) 
	{
		ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
		if(s2 == ForgeDirection.NORTH) return (getState() & 512) > 0;
		return false;
	}
}