package vic.mod.integratedcircuits.ic.parts.timed;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.util.MiscUtils;

public class PartPulseFormer extends PartDelayedAction
{
	@Override
	public void onInputChange(ForgeDirection side) 
	{
		updateInput();
		if((MiscUtils.rotn(side, -getRotation()) != ForgeDirection.SOUTH)) return;
		if(getInputFromSide(side)) 
		{
			setState(getState() | 128);
			notifyNeighbours();
			setDelay(true);
		}
	}

	@Override
	public boolean getOutputToSide(ForgeDirection side) 
	{
		ForgeDirection f2 = MiscUtils.rotn(side, -getRotation());
		if(f2 != ForgeDirection.NORTH) return false;
		return (getState() & 128) > 0;
	}

	@Override
	public boolean canConnectToSide(ForgeDirection side) 
	{
		ForgeDirection f2 = MiscUtils.rotn(side, -getRotation());
		return f2 == ForgeDirection.NORTH || f2 == ForgeDirection.SOUTH;
	}

	@Override
	public void onTick() 
	{
		super.onTick();
	}

	@Override
	protected int getDelay() 
	{
		return 2;
	}

	@Override
	public void onDelay() 
	{
		setState(getState() & ~128);
		super.onDelay();
	}
}