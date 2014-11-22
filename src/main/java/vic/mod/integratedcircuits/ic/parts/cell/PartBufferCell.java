package vic.mod.integratedcircuits.ic.parts.cell;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.parts.PartSimpleGate;
import vic.mod.integratedcircuits.util.MiscUtils;

public class PartBufferCell extends PartSimpleGate
{
	@Override
	public void onInputChange(ForgeDirection side) 
	{
		super.onInputChange(side);
		getNeighbourOnSide(side.getOpposite()).onInputChange(side);
		markForUpdate();
	}
	
	@Override
	public boolean getOutputToSide(ForgeDirection side) 
	{
		ForgeDirection fd = MiscUtils.rotn(side, -getRotation());
		if(fd == ForgeDirection.NORTH) return getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, getRotation()));
		else if(fd == ForgeDirection.SOUTH) return getInputFromSide(MiscUtils.rotn(ForgeDirection.NORTH, getRotation()));
		
		boolean out = super.getOutputToSide(side);
		if(fd == ForgeDirection.EAST && !out) return getInputFromSide(MiscUtils.rotn(ForgeDirection.WEST, getRotation()));
		else if(fd == ForgeDirection.WEST && !out) return getInputFromSide(MiscUtils.rotn(ForgeDirection.EAST, getRotation()));
		return out;
	}
	
	@Override
	protected void calcOutput() 
	{
		setOutput((getInputFromSide(MiscUtils.rotn(ForgeDirection.NORTH, getRotation())) 
			|| getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, getRotation()))));
	}
	
	@Override
	protected boolean hasOutputToSide(ForgeDirection fd) 
	{
		return fd == ForgeDirection.EAST || fd == ForgeDirection.WEST;
	}	
}