package vic.mod.integratedcircuits.ic.part.latch;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.ic.part.PartCPGate;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartToggleLatch extends PartCPGate
{
	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) 
	{
		super.onClick(pos, parent, button, ctrl);
		if(button == 0 && ctrl) 
		{
			setState(pos, parent, getState(pos, parent) ^ 128);
			notifyNeighbours(pos, parent);
		}
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		super.onInputChange(pos, parent, side);
		ForgeDirection s2 = toInternal(pos, parent, side);
		if((s2 == ForgeDirection.NORTH || s2 == ForgeDirection.SOUTH))
		{
			if(getInputFromSide(pos, parent, side)) setState(pos, parent, getState(pos, parent) ^ 128);
			scheduleTick(pos, parent);
		}
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		ForgeDirection s2 = toInternal(pos, parent, side);
		if(s2 == ForgeDirection.EAST) return (getState(pos, parent) & 128) > 0;
		if(s2 == ForgeDirection.WEST) return (getState(pos, parent) & 128) == 0;
		return false;
	}
}