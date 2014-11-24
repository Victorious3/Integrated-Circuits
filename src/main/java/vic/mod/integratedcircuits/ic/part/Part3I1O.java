package vic.mod.integratedcircuits.ic.part;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.misc.MiscUtils;

public abstract class Part3I1O extends PartSimpleGate
{
	@Override
	public void onClick(int button, boolean ctrl) 
	{
		if(button == 0 && ctrl)
		{
			int i1 = (getState() & 768) >> 8;
			i1 = i1 + 1 > 3 ? 0 : i1 + 1;
			setState(getState() & ~768 | i1 << 8);
		}
		super.onClick(button, ctrl);
	}
	
	@Override
	public boolean canConnectToSide(ForgeDirection side) 
	{
		ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
		if(s2 == ForgeDirection.NORTH) return true;
		int i = (getState() & 768) >> 8;
		if(s2 == ForgeDirection.EAST && i == 1) return false;
		if(s2 == ForgeDirection.SOUTH && i == 2) return false;
		if(s2 == ForgeDirection.WEST && i == 3) return false;
		return true;
	}

	@Override
	protected boolean hasOutputToSide(ForgeDirection fd) 
	{
		return fd == ForgeDirection.NORTH;
	}
}