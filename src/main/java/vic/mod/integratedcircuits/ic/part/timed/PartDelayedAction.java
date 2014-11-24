package vic.mod.integratedcircuits.ic.part.timed;

import vic.mod.integratedcircuits.ic.part.PartGate;


/** Uses 8 bits for the delay. 255 ticks = 12.75 seconds*/
public abstract class PartDelayedAction extends PartGate
{
	protected abstract int getDelay();
	
	public int getCurrentDelay()
	{
		return ((getState() & 32640) >> 7);
	}
	
	@Override
	public void onScheduledTick() 
	{
		if((getState() & 64) > 0)
		{
			int counter = getCurrentDelay();
			counter--;
			if(counter == 0)
			{
				setState(getState() & ~32640);
				setState(getState() & ~64);
				onDelay();
			}
			else 
			{
				setState(getState() & ~32640 | counter << 7);
				scheduleTick();
			}
		}	
	}
	
	public void onDelay()
	{
		notifyNeighbours();
	}

	protected void setDelay(boolean b)
	{
		setState(getState() & ~32640 | getDelay() << 7);
		if(b) 
		{
			setState(getState() | 64);
			scheduleTick();
		}
		else setState(getState() & ~64);
	}
}