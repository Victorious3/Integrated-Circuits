package vic.mod.integratedcircuits.ic.part.timed;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.misc.MiscUtils;

public class PartSequencer extends PartTimer
{
	@Override
	public void onInputChange(ForgeDirection side)
	{
		updateInput();
	}

	@Override
	public void onDelay() 
	{
		if((getState() & 32768) == 0)
		{
			ForgeDirection fd = ForgeDirection.getOrientation(((getState() & 50331648) >> 24) + 2);
			fd = MiscUtils.rot(fd);
			setState(getState() & ~50331648 | (fd.ordinal() - 2) << 24); 
		}
		super.onDelay();
	}

	@Override
	public boolean getOutputToSide(ForgeDirection side) 
	{
		if(ForgeDirection.getOrientation(((getState() & 50331648) >> 24) + 2) == MiscUtils.rotn(side, -getRotation()))
			return (getState() & 32768) > 0;
		else return false;
	}
}