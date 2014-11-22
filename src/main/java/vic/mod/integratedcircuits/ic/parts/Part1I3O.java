package vic.mod.integratedcircuits.ic.parts;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.util.MiscUtils;

public abstract class Part1I3O extends PartSimpleGate
{
	@Override
	public void onClick(int button, boolean ctrl) 
	{
		if(button == 0 && ctrl)
		{
			int i1 = (getState() & 1792) >> 8;
			i1 = i1 + 1 > 6 ? 0 : i1 + 1;
			setState(getState() & ~1792 | i1 << 8);
		}
		super.onClick(button, ctrl);
	}

	@Override
	public boolean canConnectToSide(ForgeDirection side) 
	{
		ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
		if(s2 == ForgeDirection.SOUTH) return true;
		int i = (getState() & 1792) >> 8;
		if(s2 == ForgeDirection.EAST && (i == 3 || i == 4 || i == 5)) return false;
		if(s2 == ForgeDirection.NORTH && (i == 2 || i == 4 || i == 6)) return false;
		if(s2 == ForgeDirection.WEST && (i == 1 || i == 5 || i == 6)) return false;
		return true;
	}
	
	@Override
	protected boolean hasOutputToSide(ForgeDirection fd) 
	{
		return fd != ForgeDirection.SOUTH;
	}
}