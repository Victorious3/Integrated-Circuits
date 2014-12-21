package vic.mod.integratedcircuits.ic.part.timed;

import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.ic.part.PartCPGate;
import vic.mod.integratedcircuits.misc.Vec2;


/** Uses 8 bits for the delay. 255 ticks = 12.75 seconds*/
public abstract class PartDelayedAction extends PartCPGate
{
	protected abstract int getDelay(Vec2 pos, ICircuit parent);
	
	public int getCurrentDelay(Vec2 pos, ICircuit parent)
	{
		return ((getState(pos, parent) & 32640) >> 7);
	}
	
	@Override
	public void onScheduledTick(Vec2 pos, ICircuit parent) 
	{
		if((getState(pos, parent) & 64) > 0)
		{
			int counter = getCurrentDelay(pos, parent);
			counter--;
			if(counter == 0)
			{
				setState(pos, parent, getState(pos, parent) & ~32640);
				setState(pos, parent, getState(pos, parent) & ~64);
				onDelay(pos, parent);
			}
			else 
			{
				setState(pos, parent, getState(pos, parent) & ~32640 | counter << 7);
				scheduleTick(pos, parent);
			}
		}	
	}
	
	public void onDelay(Vec2 pos, ICircuit parent)
	{
		notifyNeighbours(pos, parent);
	}

	protected void setDelay(Vec2 pos, ICircuit parent, boolean b)
	{
		setState(pos, parent, getState(pos, parent) & ~32640 | getDelay(pos, parent) << 7);
		if(b) 
		{
			setState(pos, parent, getState(pos, parent) | 64);
			scheduleTick(pos, parent);
		}
		else setState(pos, parent, getState(pos, parent) & ~64);
	}
}