package vic.mod.integratedcircuits.ic.part.logic;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.ic.part.PartSimpleGate;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartXORGate extends PartSimpleGate
{
	@Override
	public boolean canConnectToSide(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		return toInternal(pos, parent, side) != ForgeDirection.SOUTH;
	}
	
	@Override
	protected boolean hasOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection fd) 
	{
		return fd == ForgeDirection.NORTH;
	}
	
	@Override
	protected void calcOutput(Vec2 pos, ICircuit parent) 
	{
		setOutput(pos, parent, getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.EAST))
			!= getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.WEST)));
	}
}