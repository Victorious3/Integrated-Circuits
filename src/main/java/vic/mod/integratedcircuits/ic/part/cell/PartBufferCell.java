package vic.mod.integratedcircuits.ic.part.cell;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.ic.part.PartSimpleGate;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartBufferCell extends PartSimpleGate
{
	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		super.onInputChange(pos, parent, side);
		getNeighbourOnSide(pos, parent, side.getOpposite()).onInputChange(pos.offset(side), parent, side);
		markForUpdate(pos, parent);
	}
	
	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		ForgeDirection fd = toInternal(pos, parent, side);
		if(fd == ForgeDirection.NORTH) 
			return getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH));
		else if(fd == ForgeDirection.SOUTH) 
			return getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.NORTH));
		
		boolean out = super.getOutputToSide(pos, parent, side);
		if(fd == ForgeDirection.EAST && !out) 
			return getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.WEST));
		else if(fd == ForgeDirection.WEST && !out) 
			return getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.EAST));
		
		return out;
	}
	
	@Override
	protected void calcOutput(Vec2 pos, ICircuit parent) 
	{
		setOutput(pos, parent, (getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.NORTH)) 
			|| getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH))));
	}
	
	@Override
	protected boolean hasOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection fd) 
	{
		return fd == ForgeDirection.EAST || fd == ForgeDirection.WEST;
	}	
}