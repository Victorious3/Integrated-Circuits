package vic.mod.integratedcircuits.ic.part.cell;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.ic.part.PartSimpleGate;
import vic.mod.integratedcircuits.misc.MiscUtils;
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
		if(fd == ForgeDirection.NORTH) return getInputFromSide(pos, parent, MiscUtils.rotn(ForgeDirection.SOUTH, getRotation(pos, parent)));
		else if(fd == ForgeDirection.SOUTH) return getInputFromSide(pos, parent, MiscUtils.rotn(ForgeDirection.NORTH, getRotation(pos, parent)));
		
		boolean out = super.getOutputToSide(pos, parent, side);
		if(fd == ForgeDirection.EAST && !out) return getInputFromSide(pos, parent, MiscUtils.rotn(ForgeDirection.WEST, getRotation(pos, parent)));
		else if(fd == ForgeDirection.WEST && !out) return getInputFromSide(pos, parent, MiscUtils.rotn(ForgeDirection.EAST, getRotation(pos, parent)));
		return out;
	}
	
	@Override
	protected void calcOutput(Vec2 pos, ICircuit parent) 
	{
		setOutput(pos, parent, (getInputFromSide(pos, parent, MiscUtils.rotn(ForgeDirection.NORTH, getRotation(pos, parent))) 
			|| getInputFromSide(pos, parent, MiscUtils.rotn(ForgeDirection.SOUTH, getRotation(pos, parent)))));
	}
	
	@Override
	protected boolean hasOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection fd) 
	{
		return fd == ForgeDirection.EAST || fd == ForgeDirection.WEST;
	}	
}