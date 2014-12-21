package vic.mod.integratedcircuits.ic.part.timed;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartSequencer extends PartTimer
{
	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		updateInput(pos, parent);
	}

	@Override
	public void onDelay(Vec2 pos, ICircuit parent) 
	{
		if((getState(pos, parent) & 32768) == 0)
		{
			ForgeDirection fd = ForgeDirection.getOrientation(((getState(pos, parent) & 50331648) >> 24) + 2);
			fd = MiscUtils.rot(fd);
			setState(pos, parent, getState(pos, parent) & ~50331648 | (fd.ordinal() - 2) << 24); 
		}
		super.onDelay(pos, parent);
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		if(ForgeDirection.getOrientation(((getState(pos, parent) & 50331648) >> 24) + 2) == toInternal(pos, parent, side))
			return (getState(pos, parent) & 32768) > 0;
		else return false;
	}
}