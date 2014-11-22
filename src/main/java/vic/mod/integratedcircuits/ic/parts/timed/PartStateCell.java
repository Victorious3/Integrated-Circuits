package vic.mod.integratedcircuits.ic.parts.timed;

import net.minecraft.init.Items;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.util.CraftingAmount;
import vic.mod.integratedcircuits.util.ItemAmount;
import vic.mod.integratedcircuits.util.MiscUtils;

public class PartStateCell extends PartDelayedAction
{	
	private static int f1 = 1 << 23;
	private static int f2 = 1 << 24;
	
	@Override
	public void onClick(int button, boolean ctrl) 
	{
		//TODO Insert some gui here too.
		super.onClick(button, ctrl);
	}

	@Override
	protected int getDelay()
	{
		if((getState() & f2) > 0) return 2;
		return ((getState() & 8355840) >> 16);
	}
	
	@Override
	public boolean getOutputToSide(ForgeDirection side) 
	{
		ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
		if(s2 == ForgeDirection.WEST && (getState() & f1) > 0) return true;
		if(s2 == ForgeDirection.NORTH && (getState() & f2) > 0) return true;
		return false;
	}

	@Override
	public void onDelay() 
	{
		if((getState() & f2) > 0) setState(getState() & ~f2);
		else if((getState() & f1) > 0) 
		{
			setState(getState() & ~f1);
			setState(getState() | f2);
			setDelay(true);
		}
		super.onDelay();
	}

	@Override
	public void onPlaced()
	{
		setState(10 << 16);
	}

	@Override
	public void onInputChange(ForgeDirection side) 
	{
		updateInput();
		ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
		if(s2 == ForgeDirection.SOUTH)
		{
			if(getInputFromSide(side))
			{
				setState(getState() | f1);
				setState(getState() & ~f2);
				notifyNeighbours();
			}
			else if(!getInputFromSide(MiscUtils.rotn(ForgeDirection.EAST, getRotation())))
				setDelay(true);
		}
		else if(s2 == ForgeDirection.EAST && (getState() & f1) > 0)
		{
			if(getInputFromSide(side)) setDelay(false);
			else if(!getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, getRotation())))
				setDelay(true);
			notifyNeighbours();
		}
	}
	
	@Override
	public void getCraftingCost(CraftingAmount cost) 
	{
		cost.add(new ItemAmount(Items.redstone, 0.15));
		cost.add(new ItemAmount(Items.glowstone_dust, 0.1));
	}
}