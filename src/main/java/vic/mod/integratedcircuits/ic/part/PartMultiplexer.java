package vic.mod.integratedcircuits.ic.part;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartMultiplexer extends PartSimpleGate
{
	@Override
	protected void calcOutput(Vec2 pos, ICircuit parent) 
	{
		if(getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH)))
			setOutput(pos, parent, getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.WEST)));
		else setOutput(pos, parent, getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.EAST)));
	}

	@Override
	protected boolean hasOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection fd) 
	{
		return fd == ForgeDirection.NORTH;
	}	
}