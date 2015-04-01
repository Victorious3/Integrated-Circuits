package moe.nightfall.vic.integratedcircuits.ic.part;

import moe.nightfall.vic.integratedcircuits.ic.CircuitPart;
import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import net.minecraftforge.common.util.ForgeDirection;

public class PartIOBit extends CircuitPart
{
	public final IntProperty PROP_ROTATION = new IntProperty("ROTATION", stitcher, 3);
	public final IntProperty PROP_FREQUENCY = new IntProperty("FREQUENCY", stitcher, 15);
	
	public final int getRotation(Vec2 pos, ICircuit parent)
	{
		return getProperty(pos, parent, PROP_ROTATION);
	}
	
	public final void setRotation(Vec2 pos, ICircuit parent, int rotation)
	{
		setProperty(pos, parent, PROP_ROTATION, rotation);
	}
	
	public final int getFrequency(Vec2 pos, ICircuit parent)
	{
		return getProperty(pos, parent, PROP_FREQUENCY);
	}
	
	public final void setFrequency(Vec2 pos, ICircuit parent, int frequency)
	{
		setProperty(pos, parent, PROP_FREQUENCY, frequency);
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		updateInput(pos, parent);
		ForgeDirection dir = MiscUtils.getDirection(getRotation(pos, parent));
		if(side == dir.getOpposite())
			parent.setOutputToSide(dir, getFrequency(pos, parent), getInputFromSide(pos, parent, side));
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		ForgeDirection dir = MiscUtils.getDirection(getRotation(pos, parent));
		if(side == dir.getOpposite())
			return parent.getInputFromSide(dir, getFrequency(pos, parent));
		else return false;
	}
	
	public boolean isPowered(Vec2 pos, ICircuit parent)
	{
		ForgeDirection dir = MiscUtils.getDirection(getRotation(pos, parent)).getOpposite();
		return getOutputToSide(pos, parent, dir) || getNeighbourOnSide(pos, parent, dir).getOutputToSide(pos.offset(dir), parent, dir.getOpposite());
	}
}