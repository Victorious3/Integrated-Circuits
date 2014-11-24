package vic.mod.integratedcircuits.ic.part.latch;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.part.PartGate;
import vic.mod.integratedcircuits.misc.MiscUtils;

public class PartToggleLatch extends PartGate
{
	@Override
	public void onClick(int button, boolean ctrl) 
	{
		super.onClick(button, ctrl);
		if(button == 0 && ctrl) 
		{
			setState(getState() ^ 128);
			notifyNeighbours();
		}
	}

	@Override
	public void onInputChange(ForgeDirection side) 
	{
		super.onInputChange(side);
		ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
		if((s2 == ForgeDirection.NORTH || s2 == ForgeDirection.SOUTH))
		{
			if(getInputFromSide(side)) setState(getState() ^ 128);
			scheduleTick();
		}
	}

	@Override
	public boolean getOutputToSide(ForgeDirection side) 
	{
		ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
		if(s2 == ForgeDirection.EAST) return (getState() & 128) > 0;
		if(s2 == ForgeDirection.WEST) return (getState() & 128) == 0;
		return false;
	}
}