package vic.mod.integratedcircuits.ic.part.latch;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.ic.part.PartCPGate;
import vic.mod.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartTransparentLatch extends PartCPGate
{
	public final BooleanProperty PROP_OUT = new BooleanProperty("OUT", stitcher);
	
	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		super.onInputChange(pos, parent, side);
		ForgeDirection s2 = toInternal(pos, parent, side);
		if(s2 == ForgeDirection.SOUTH || (s2 == ForgeDirection.WEST && getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH)))) 
		{
			if(getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.WEST))) 
				setProperty(pos, parent, PROP_OUT, true);
			else setProperty(pos, parent, PROP_OUT, false);
			scheduleTick(pos, parent);
		}
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		ForgeDirection s2 = toInternal(pos, parent, side);
		if(s2 == ForgeDirection.NORTH || s2 == ForgeDirection.EAST) 
			return getProperty(pos, parent, PROP_OUT);
		return false;
	}
}