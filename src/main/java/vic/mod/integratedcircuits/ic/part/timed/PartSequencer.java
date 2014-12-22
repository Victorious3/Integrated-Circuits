package vic.mod.integratedcircuits.ic.part.timed;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.misc.PropertyStitcher.IntProperty;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartSequencer extends PartTimer
{
	public final IntProperty PROP_OUTPUT_SIDE = new IntProperty(stitcher, 3);
	
	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		updateInput(pos, parent);
	}

	@Override
	public void onDelay(Vec2 pos, ICircuit parent) 
	{
		if(!getProperty(pos, parent, PROP_OUT))
			cycleProperty(pos, parent, PROP_OUTPUT_SIDE);
		super.onDelay(pos, parent);
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		if(MiscUtils.getDirection(getProperty(pos, parent, PROP_OUTPUT_SIDE)) == toInternal(pos, parent, side))
			return getProperty(pos, parent, PROP_OUT);
		else return false;
	}
}