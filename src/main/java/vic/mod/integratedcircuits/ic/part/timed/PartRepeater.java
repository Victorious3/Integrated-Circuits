package vic.mod.integratedcircuits.ic.part.timed;

import java.util.ArrayList;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartRepeater extends PartDelayedAction
{
	@Override
	protected int getDelay(Vec2 pos, ICircuit parent) 
	{
		return ((getState(pos, parent) & 16711680) >> 16);
	}
	
	@Override
	public void onPlaced(Vec2 pos, ICircuit parent)
	{
		setState(pos, parent, 2 << 16);
		setState(pos, parent, getState(pos, parent) | 32768);
	}

	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) 
	{
		super.onClick(pos, parent, button, ctrl);
		if(button == 0 && ctrl)
		{
			int delay = getDelay(pos, parent);
			int newDelay = 0;
			switch (delay) {
			case 2 : delay = 4; break;
			case 4 : delay = 8; break;
			case 8 : delay = 16; break;
			case 16 : delay = 32; break;
			case 32 : delay = 64; break;
			case 64 : delay = 128; break;
			case 128 : delay = 255; break;
			default : delay = 2; break;
			}
			setState(pos, parent, (getState(pos, parent) & ~16711680) | delay << 16);
		}
	}

	@Override
	public boolean canConnectToSide(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		ForgeDirection s2 = toInternal(pos, parent, side);
		return s2 == ForgeDirection.NORTH || s2 == ForgeDirection.SOUTH;
	}
	
	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		ForgeDirection s2 = toInternal(pos, parent, side);
		if(s2 != ForgeDirection.NORTH) return false;
		return getCurrentDelay(pos, parent) > 0 ? (getState(pos, parent) & 32768) > 0 : (getState(pos, parent) & 32768) == 0;
	}

	@Override
	public void onDelay(Vec2 pos, ICircuit parent) 
	{	
		setState(pos, parent, getState(pos, parent) ^ 32768);
		super.onDelay(pos, parent);
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		updateInput(pos, parent);
		if(toInternal(pos, parent, side) != ForgeDirection.SOUTH) return;
		if(getCurrentDelay(pos, parent) == 0)
		{
			if(((getState(pos, parent) & 32768) >> 15) != (getInputFromSide(pos, parent, side) ? 1 : 0))
			{
				setState(pos, parent, getState(pos, parent) & ~32768 | (getInputFromSide(pos, parent, side) ? 1 : 0) << 15);
			}
		}
		setDelay(pos, parent, true);
	}

	@Override
	public ArrayList<String> getInformation(Vec2 pos, ICircuit parent) 
	{
		ArrayList<String> list = super.getInformation(pos, parent);
		list.add("Delay: " + getDelay(pos, parent));
		return list;
	}	
}