package moe.nightfall.vic.integratedcircuits.ic.part.timed;

import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import net.minecraftforge.common.util.ForgeDirection;

public class PartPulseFormer extends PartDelayedAction
{
	public BooleanProperty PROP_OUTPUT = new BooleanProperty("OUTPUT", stitcher);
	
	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		updateInput(pos, parent);
		if((toInternal(pos, parent, side) != ForgeDirection.SOUTH)) return;
		if(getInputFromSide(pos, parent, side)) 
		{
			setProperty(pos, parent, PROP_OUTPUT, true);
			notifyNeighbours(pos, parent);
			setDelay(pos, parent, true);
		}
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		ForgeDirection f2 = toInternal(pos, parent, side);
		if(f2 != ForgeDirection.NORTH) return false;
		return getProperty(pos, parent, PROP_OUTPUT);
	}

	@Override
	public boolean canConnectToSide(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		ForgeDirection f2 = toInternal(pos, parent, side);
		return f2 == ForgeDirection.NORTH || f2 == ForgeDirection.SOUTH;
	}

	@Override
	protected int getDelay(Vec2 pos, ICircuit parent) 
	{
		return 2;
	}

	@Override
	public void onDelay(Vec2 pos, ICircuit parent) 
	{
		setProperty(pos, parent, PROP_OUTPUT, false);
		super.onDelay(pos, parent);
	}
}