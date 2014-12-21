package vic.mod.integratedcircuits.ic.part.cell;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.CircuitPart;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartNullCell extends CircuitPart
{
	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		return getInputFromSide(pos, parent, side.getOpposite()) && !getInputFromSide(pos, parent, side);
	}
	
	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		updateInput(pos, parent);
		notifyNeighbours(pos, parent);
	}
}