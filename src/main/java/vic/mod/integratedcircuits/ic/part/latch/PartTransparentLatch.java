package vic.mod.integratedcircuits.ic.part.latch;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.ic.part.PartCPGate;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartTransparentLatch extends PartCPGate
{
	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		super.onInputChange(pos, parent, side);
		ForgeDirection s2 = toInternal(pos, parent, side);
		if(s2 == ForgeDirection.SOUTH || (s2 == ForgeDirection.WEST && getInputFromSide(pos, parent, MiscUtils.rotn(ForgeDirection.SOUTH, getRotation(pos, parent))))) 
		{
			if(getInputFromSide(pos, parent, MiscUtils.rotn(ForgeDirection.WEST, getRotation(pos, parent)))) 
				setState(pos, parent, getState(pos, parent) | 128);
			else setState(pos, parent, getState(pos, parent) & ~128);
		}
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		ForgeDirection s2 = toInternal(pos, parent, side);
		if(s2 == ForgeDirection.NORTH || s2 == ForgeDirection.EAST) return (getState(pos, parent) & 128) > 0;
		return false;
	}
}