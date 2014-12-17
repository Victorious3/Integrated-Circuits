package vic.mod.integratedcircuits.ic.part;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.misc.MiscUtils;

/** Has only one type of output **/
public abstract class PartSimpleGate extends PartCPGate
{
	protected final boolean getOutput()
	{
		return ((getState()) & 64) != 0;
	}
	
	protected final void setOutput(boolean output)
	{
		setState(getState() & ~64);
		if(output) setState(getState() | 64);
	}
	
	protected abstract void calcOutput();
	
	/** already rotated **/
	protected abstract boolean hasOutputToSide(ForgeDirection fd);
	
	@Override
	public boolean getOutputToSide(ForgeDirection side)
	{
		return hasOutputToSide(MiscUtils.rotn(side, -getRotation())) && ((getState() & 128) >> 7) > 0;
	}
	
	@Override
	public void onInputChange(ForgeDirection side)
	{
		updateInput();
		calcOutput();
		ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
		if(canConnectToSide(side) && !hasOutputToSide(s2)) scheduleTick();
	}

	@Override
	public void onScheduledTick()
	{
		setState(getState() & ~128);
		setState(getState() | (getState() & 64) << 1);
		notifyNeighbours();
	}

	@Override
	public void onPlaced() 
	{
		updateInput();
		calcOutput();
		setState(getState() & ~128);
		setState(getState() | (getState() & 64) << 1);
		notifyNeighbours();
	}

	@Override
	public void onClick(int button, boolean ctrl) 
	{
		super.onClick(button, ctrl);
		onPlaced();
	}
}