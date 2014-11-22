package vic.mod.integratedcircuits.ic.parts;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.CircuitPart;
import vic.mod.integratedcircuits.util.MiscUtils;

public class PartIOBit extends CircuitPart
{
	public final int getRotation()
	{
		return (getState() & 48) >> 4;
	}
	
	public final void setRotation(int rotation)
	{
		setState(getState() & ~48 | rotation << 4);
	}
	
	public final int getFrequency()
	{
		return (getState() & 960) >> 6;
	}
	
	public final void setFrequency(int frequency)
	{
		setState(getState() & ~960 | frequency << 6);
	}

	@Override
	public void onInputChange(ForgeDirection side) 
	{
		updateInput();
		ForgeDirection dir = MiscUtils.getDirection(getRotation());
		if(side == dir.getOpposite())
			getData().getCircuit().setOutputToSide(dir, getFrequency(), getInputFromSide(side));
	}

	@Override
	public boolean getOutputToSide(ForgeDirection side) 
	{
		ForgeDirection dir = MiscUtils.getDirection(getRotation());
		if(side == dir.getOpposite())
			return getData().getCircuit().getInputFromSide(dir, getFrequency());
		else return false;
	}
	
	public boolean isPowered()
	{
		ForgeDirection dir = MiscUtils.getDirection(getRotation()).getOpposite();
		return getOutputToSide(dir) || getNeighbourOnSide(dir).getOutputToSide(dir.getOpposite());
	}
}