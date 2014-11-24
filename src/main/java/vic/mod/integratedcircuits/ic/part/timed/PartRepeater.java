package vic.mod.integratedcircuits.ic.part.timed;

import java.util.ArrayList;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.misc.MiscUtils;

public class PartRepeater extends PartDelayedAction
{
	@Override
	protected int getDelay() 
	{
		return ((getState() & 16711680) >> 16);
	}
	
	@Override
	public void onPlaced()
	{
		setState(2 << 16);
		setState(getState() | 32768);
	}

	@Override
	public void onClick(int button, boolean ctrl) 
	{
		super.onClick(button, ctrl);
		if(button == 0 && ctrl)
		{
			int delay = getDelay();
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
			setState((getState() & ~16711680) | delay << 16);
		}
	}

	@Override
	public boolean canConnectToSide(ForgeDirection side) 
	{
		ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
		return s2 == ForgeDirection.NORTH || s2 == ForgeDirection.SOUTH;
	}
	
	@Override
	public boolean getOutputToSide(ForgeDirection side) 
	{
		ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
		if(s2 != ForgeDirection.NORTH) return false;
		return getCurrentDelay() > 0 ? (getState() & 32768) > 0 : (getState() & 32768) == 0;
	}

	@Override
	public void onDelay() 
	{	
		setState(getState() ^ 32768);
		super.onDelay();
	}

	@Override
	public void onInputChange(ForgeDirection side) 
	{
		updateInput();
		if(MiscUtils.rotn(side, -getRotation()) != ForgeDirection.SOUTH) return;
		if(getCurrentDelay() == 0)
		{
			if(((getState() & 32768) >> 15) != (getInputFromSide(side) ? 1 : 0))
			{
				setState(getState() & ~32768 | (getInputFromSide(side) ? 1 : 0) << 15);
			}
		}
		setDelay(true);
	}

	@Override
	public ArrayList<String> getInformation() 
	{
		ArrayList<String> list = super.getInformation();
		list.add("Delay: " + getDelay());
		return list;
	}	
}