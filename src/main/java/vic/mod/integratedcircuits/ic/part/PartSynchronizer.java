package vic.mod.integratedcircuits.ic.part;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.Vec2;

//TODO Is currently giving a one tick pulse, might cause problems with other gates.
public class PartSynchronizer extends PartCPGate
{
	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		updateInput(pos, parent);
		ForgeDirection s2 = toInternal(pos, parent, side);
		boolean input = getInputFromSide(pos, parent, s2);
		
		if(s2 == ForgeDirection.SOUTH && getInputFromSide(pos, parent, s2)) setState(pos, parent, getState(pos, parent) & ~896);
		else if(s2 == ForgeDirection.EAST && !getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH)) && input) 
			setState(pos, parent, getState(pos, parent) | 128);
		else if(s2 == ForgeDirection.WEST && !getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH)) && input) 
			setState(pos, parent, getState(pos, parent) | 256);
		
		if((getState(pos, parent) & 384) >> 7 == 3) 
		{
			setState(pos, parent, getState(pos, parent) & ~384);
			setState(pos, parent, getState(pos, parent) | 512);
			scheduleTick(pos, parent);
		}
	}

	@Override
	public void onScheduledTick(Vec2 pos, ICircuit parent)
	{
		notifyNeighbours(pos, parent);
		if((getState(pos, parent) & 512) > 0)
		{
			setState(pos, parent, getState(pos, parent) & ~512);
			scheduleTick(pos, parent);
		}
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		ForgeDirection s2 = toInternal(pos, parent, side);
		if(s2 == ForgeDirection.NORTH) return (getState(pos, parent) & 512) > 0;
		return false;
	}
}