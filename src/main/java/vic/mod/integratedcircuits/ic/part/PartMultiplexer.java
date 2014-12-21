package vic.mod.integratedcircuits.ic.part;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartMultiplexer extends PartSimpleGate
{
	@Override
	protected void calcOutput(Vec2 pos, ICircuit parent) 
	{
		if(getInputFromSide(pos, parent, MiscUtils.rotn(ForgeDirection.SOUTH, getRotation(pos, parent))))
			setOutput(pos, parent, getInputFromSide(pos, parent, MiscUtils.rotn(ForgeDirection.WEST, getRotation(pos, parent))));
		else setOutput(pos, parent, getInputFromSide(pos, parent, MiscUtils.rotn(ForgeDirection.EAST, getRotation(pos, parent))));
	}

	@Override
	protected boolean hasOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection fd) 
	{
		return fd == ForgeDirection.NORTH;
	}	
}