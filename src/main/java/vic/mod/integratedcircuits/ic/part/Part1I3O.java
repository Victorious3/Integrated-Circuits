package vic.mod.integratedcircuits.ic.part;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.Vec2;

public abstract class Part1I3O extends PartSimpleGate
{
	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) 
	{
		if(button == 0 && ctrl)
		{
			int i1 = (getState(pos, parent) & 1792) >> 8;
			i1 = i1 + 1 > 6 ? 0 : i1 + 1;
			setState(pos, parent, getState(pos, parent) & ~1792 | i1 << 8);
		}
		super.onClick(pos, parent, button, ctrl);
	}

	@Override
	public boolean canConnectToSide(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		ForgeDirection s2 = toInternal(pos, parent, side);
		if(s2 == ForgeDirection.SOUTH) return true;
		int i = (getState(pos, parent) & 1792) >> 8;
		if(s2 == ForgeDirection.EAST && (i == 3 || i == 4 || i == 5)) return false;
		if(s2 == ForgeDirection.NORTH && (i == 2 || i == 4 || i == 6)) return false;
		if(s2 == ForgeDirection.WEST && (i == 1 || i == 5 || i == 6)) return false;
		return true;
	}
	
	@Override
	protected boolean hasOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection fd) 
	{
		return fd != ForgeDirection.SOUTH;
	}
}