package vic.mod.integratedcircuits.ic.parts.cell;

import vic.mod.integratedcircuits.ic.CircuitPart;
import net.minecraftforge.common.util.ForgeDirection;

public class PartNullCell extends CircuitPart
{
	@Override
	public boolean getOutputToSide(ForgeDirection side) 
	{
		return getInputFromSide(side.getOpposite()) && !getInputFromSide(side);
	}
	
	@Override
	public void onInputChange(ForgeDirection side) 
	{
		updateInput();
		notifyNeighbours();
	}
}