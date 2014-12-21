package vic.mod.integratedcircuits.ic.part.timed;

import net.minecraft.init.Items;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.CraftingAmount;
import vic.mod.integratedcircuits.misc.ItemAmount;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartStateCell extends PartDelayedAction
{	
	private static int f1 = 1 << 23;
	private static int f2 = 1 << 24;

	@Override
	protected int getDelay(Vec2 pos, ICircuit parent)
	{
		if((getState(pos, parent) & f2) > 0) return 2;
		return ((getState(pos, parent) & 8355840) >> 16);
	}
	
	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		ForgeDirection s2 = toInternal(pos, parent, side);
		if(s2 == ForgeDirection.WEST && (getState(pos, parent) & f1) > 0) return true;
		if(s2 == ForgeDirection.NORTH && (getState(pos, parent) & f2) > 0) return true;
		return false;
	}

	@Override
	public void onDelay(Vec2 pos, ICircuit parent) 
	{
		if((getState(pos, parent) & f2) > 0) setState(pos, parent, getState(pos, parent) & ~f2);
		else if((getState(pos, parent) & f1) > 0) 
		{
			setState(pos, parent, getState(pos, parent) & ~f1);
			setState(pos, parent, getState(pos, parent) | f2);
			setDelay(pos, parent, true);
		}
		super.onDelay(pos, parent);
	}

	@Override
	public void onPlaced(Vec2 pos, ICircuit parent)
	{
		setState(pos, parent, 10 << 16);
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		updateInput(pos, parent);
		ForgeDirection s2 = toInternal(pos, parent, side);
		if(s2 == ForgeDirection.SOUTH)
		{
			if(getInputFromSide(pos, parent, side))
			{
				setState(pos, parent, getState(pos, parent) | f1);
				setState(pos, parent, getState(pos, parent) & ~f2);
				notifyNeighbours(pos, parent);
			}
			else if(!getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.EAST)))
				setDelay(pos, parent, true);
		}
		else if(s2 == ForgeDirection.EAST && (getState(pos, parent) & f1) > 0)
		{
			if(getInputFromSide(pos, parent, side)) setDelay(pos, parent, false);
			else if(!getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH)))
				setDelay(pos, parent, true);
			notifyNeighbours(pos, parent);
		}
	}
	
	@Override
	public void getCraftingCost(CraftingAmount cost) 
	{
		cost.add(new ItemAmount(Items.redstone, 0.15));
		cost.add(new ItemAmount(Items.glowstone_dust, 0.1));
	}
}