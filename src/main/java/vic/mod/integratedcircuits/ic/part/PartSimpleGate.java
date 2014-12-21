package vic.mod.integratedcircuits.ic.part;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.Vec2;

/** Has only one type of output **/
public abstract class PartSimpleGate extends PartCPGate
{
	protected final boolean getOutput(Vec2 pos, ICircuit parent)
	{
		return ((getState(pos, parent)) & 64) != 0;
	}
	
	protected final void setOutput(Vec2 pos, ICircuit parent, boolean output)
	{
		setState(pos, parent, getState(pos, parent) & ~64);
		if(output) setState(pos, parent, getState(pos, parent) | 64);
	}
	
	protected abstract void calcOutput(Vec2 pos, ICircuit parent);
	
	/** already rotated **/
	protected abstract boolean hasOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection fd);
	
	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		return hasOutputToSide(pos, parent, toInternal(pos, parent, side)) && ((getState(pos, parent) & 128) >> 7) > 0;
	}
	
	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		updateInput(pos, parent);
		calcOutput(pos, parent);
		ForgeDirection s2 = toInternal(pos, parent, side);
		if(canConnectToSide(pos, parent, side) && !hasOutputToSide(pos, parent, s2)) scheduleTick(pos, parent);
	}

	@Override
	public void onScheduledTick(Vec2 pos, ICircuit parent)
	{
		setState(pos, parent, getState(pos, parent) & ~128);
		setState(pos, parent, getState(pos, parent) | (getState(pos, parent) & 64) << 1);
		notifyNeighbours(pos, parent);
	}

	@Override
	public void onPlaced(Vec2 pos, ICircuit parent) 
	{
		updateInput(pos, parent);
		calcOutput(pos, parent);
		setState(pos, parent, getState(pos, parent) & ~128);
		setState(pos, parent, getState(pos, parent) | (getState(pos, parent) & 64) << 1);
		notifyNeighbours(pos, parent);
	}

	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) 
	{
		super.onClick(pos, parent, button, ctrl);
		onPlaced(pos, parent);
	}
}