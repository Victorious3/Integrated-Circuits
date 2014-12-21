package vic.mod.integratedcircuits.ic.part;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.CircuitPart;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartIOBit extends CircuitPart
{
	public final int getRotation(Vec2 pos, ICircuit parent)
	{
		return (getState(pos, parent) & 48) >> 4;
	}
	
	public final void setRotation(Vec2 pos, ICircuit parent, int rotation)
	{
		setState(pos, parent, getState(pos, parent) & ~48 | rotation << 4);
	}
	
	public final int getFrequency(Vec2 pos, ICircuit parent)
	{
		return (getState(pos, parent) & 960) >> 6;
	}
	
	public final void setFrequency(Vec2 pos, ICircuit parent, int frequency)
	{
		setState(pos, parent, getState(pos, parent) & ~960 | frequency << 6);
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