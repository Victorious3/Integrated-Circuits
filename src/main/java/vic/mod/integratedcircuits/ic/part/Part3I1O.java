package vic.mod.integratedcircuits.ic.part;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.Vec2;

public abstract class Part3I1O extends PartSimpleGate
{
	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) 
	{
		if(button == 0 && ctrl)
		{
			int i1 = (getState(pos, parent) & 768) >> 8;
			i1 = i1 + 1 > 3 ? 0 : i1 + 1;
			setState(pos, parent, getState(pos, parent) & ~768 | i1 << 8);
		}
		super.onClick(pos, parent, button, ctrl);
	}
	
	@Override
	public boolean canConnectToSide(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		ForgeDirection s2 = toInternal(pos, parent, side);
		if(s2 == ForgeDirection.NORTH) return true;
		int i = (getState(pos, parent) & 768) >> 8;
		if(s2 == ForgeDirection.EAST && i == 1) return false;
		if(s2 == ForgeDirection.SOUTH && i == 2) return false;
		if(s2 == ForgeDirection.WEST && i == 3) return false;
		return true;
	}

	@Override
	protected boolean hasOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection fd) 
	{
		return fd == ForgeDirection.NORTH;
	}
}