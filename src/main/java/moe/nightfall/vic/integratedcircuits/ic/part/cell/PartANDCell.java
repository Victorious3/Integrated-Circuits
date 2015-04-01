package moe.nightfall.vic.integratedcircuits.ic.part.cell;

import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.ic.part.PartSimpleGate;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraftforge.common.util.ForgeDirection;

public class PartANDCell extends PartSimpleGate
{
	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		super.onInputChange(pos, parent, side);
		ForgeDirection fd = toInternal(pos, parent, side);
		if(fd == ForgeDirection.NORTH || fd == ForgeDirection.SOUTH) 
			getNeighbourOnSide(pos, parent, side.getOpposite()).onInputChange(pos.offset(side.getOpposite()), parent, side);	
		markForUpdate(pos, parent);
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		ForgeDirection fd = toInternal(pos, parent, side);
		if(fd == ForgeDirection.NORTH || fd == ForgeDirection.SOUTH)
			return getInputFromSide(pos, parent, side.getOpposite());
		return super.getOutputToSide(pos, parent, side);
	}

	@Override
	protected void calcOutput(Vec2 pos, ICircuit parent) 
	{
		ForgeDirection f1 = toExternal(pos, parent, ForgeDirection.NORTH);
		ForgeDirection f2 = f1.getOpposite();
		ForgeDirection f3 = toExternal(pos, parent, ForgeDirection.EAST);
		setOutput(pos, parent, (getInputFromSide(pos, parent, f1) || getInputFromSide(pos, parent, f2)) && getInputFromSide(pos, parent, f3));
	}

	@Override
	protected boolean hasOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection fd) 
	{
		return fd == ForgeDirection.WEST;
	}
}