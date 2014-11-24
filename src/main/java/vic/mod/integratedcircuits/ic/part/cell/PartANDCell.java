package vic.mod.integratedcircuits.ic.part.cell;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.part.PartSimpleGate;
import vic.mod.integratedcircuits.misc.MiscUtils;

public class PartANDCell extends PartSimpleGate
{
	@Override
	public void onInputChange(ForgeDirection side) 
	{
		super.onInputChange(side);
		ForgeDirection fd = MiscUtils.rotn(side, -getRotation());
		if(fd == ForgeDirection.NORTH || fd == ForgeDirection.SOUTH) 
			getNeighbourOnSide(side.getOpposite()).onInputChange(side);	
		markForUpdate();
	}

	@Override
	public boolean getOutputToSide(ForgeDirection side) 
	{
		ForgeDirection fd = MiscUtils.rotn(side, -getRotation());
		if(fd == ForgeDirection.NORTH || fd == ForgeDirection.SOUTH)
			return getInputFromSide(side.getOpposite());
		return super.getOutputToSide(side);
	}

	@Override
	protected void calcOutput() 
	{
		ForgeDirection f1 = MiscUtils.rotn(ForgeDirection.NORTH, getRotation());
		ForgeDirection f2 = f1.getOpposite();
		ForgeDirection f3 = MiscUtils.rotn(ForgeDirection.EAST, getRotation());
		setOutput((getInputFromSide(f1) || getInputFromSide(f2)) && getInputFromSide(f3));
	}

	@Override
	protected boolean hasOutputToSide(ForgeDirection fd) 
	{
		return fd == ForgeDirection.WEST;
	}
}