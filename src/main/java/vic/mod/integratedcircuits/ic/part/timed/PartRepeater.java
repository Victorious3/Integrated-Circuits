package vic.mod.integratedcircuits.ic.part.timed;

import java.util.ArrayList;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import vic.mod.integratedcircuits.misc.PropertyStitcher.IntProperty;
import vic.mod.integratedcircuits.misc.Vec2;

//FIXME The repeater currently breaks when pulsing it quickly!
public class PartRepeater extends PartDelayedAction
{
	public final IntProperty PROP_DELAY = new IntProperty(stitcher, 255);
	private final BooleanProperty PROP_OUT = new BooleanProperty(stitcher);
	
	@Override
	protected int getDelay(Vec2 pos, ICircuit parent) 
	{
		return getProperty(pos, parent, PROP_DELAY);
	}
	
	@Override
	public void onPlaced(Vec2 pos, ICircuit parent)
	{
		setProperty(pos, parent, PROP_DELAY, 2);
		setProperty(pos, parent, PROP_OUT, true);
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
			case 255 : delay = 2; break;
			case 128 : delay = 255; break;
			default : delay <<= 1; break;
			}
			setProperty(pos, parent, PROP_DELAY, delay);
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
		boolean tmp = getProperty(pos, parent, PROP_OUT);
		return getCurrentDelay(pos, parent) > 0 ? tmp : !tmp;
	}

	@Override
	public void onDelay(Vec2 pos, ICircuit parent) 
	{	
		invertProperty(pos, parent, PROP_OUT);
		super.onDelay(pos, parent);
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		updateInput(pos, parent);
		if(toInternal(pos, parent, side) != ForgeDirection.SOUTH) return;
		if(getCurrentDelay(pos, parent) == 0)
		{
			boolean in = getInputFromSide(pos, parent, side);
			if(getProperty(pos, parent, PROP_OUT) != in)
				setProperty(pos, parent, PROP_OUT, in);
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