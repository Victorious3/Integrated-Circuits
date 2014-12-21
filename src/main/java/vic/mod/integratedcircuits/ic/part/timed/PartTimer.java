package vic.mod.integratedcircuits.ic.part.timed;

import net.minecraft.init.Items;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.CraftingAmount;
import vic.mod.integratedcircuits.misc.ItemAmount;
import vic.mod.integratedcircuits.misc.Vec2;

//TODO The timer should really use the tick method instead of scheduled ticks.
public class PartTimer extends PartDelayedAction implements IConfigurableDelay
{
	@Override
	protected int getDelay(Vec2 pos, ICircuit parent) 
	{
		if((getState(pos, parent) & 32768) == 0) return getConfigurableDelay(pos, parent);
		else return 2;
	}
	
	@Override
	public int getConfigurableDelay(Vec2 pos, ICircuit parent)
	{
		return ((getState(pos, parent) & 16711680) >> 16);
	}
	
	@Override
	public void setConfigurableDelay(Vec2 pos, ICircuit parent, int delay)
	{
		setState(pos, parent, getState(pos, parent) & ~16711680);
		setState(pos, parent, getState(pos, parent) | delay << 16);
	}

	@Override
	public void onPlaced(Vec2 pos, ICircuit parent)
	{
		setState(pos, parent, 10 << 16);
		updateInput(pos, parent);
		if(!getInputFromSide(pos, parent, ForgeDirection.SOUTH)) setDelay(pos, parent, true);
	}
	
	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		updateInput(pos, parent);
		if(toInternal(pos, parent, side) != ForgeDirection.SOUTH) return;
		setState(pos, parent, getState(pos, parent) & ~32768);
		if(getInputFromSide(pos, parent, side))
		{
			setDelay(pos, parent, false);
			notifyNeighbours(pos, parent);
		}
		else setDelay(pos, parent, true);
	}

	@Override
	public void onDelay(Vec2 pos, ICircuit parent) 
	{
		setState(pos, parent, getState(pos, parent) ^ 32768);
		setDelay(pos, parent, true);
		super.onDelay(pos, parent);
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		if(toInternal(pos, parent, side) == ForgeDirection.SOUTH) return false;
		return (getState(pos, parent) & 32768) != 0;
	}
	
	@Override
	public void getCraftingCost(CraftingAmount cost) 
	{
		cost.add(new ItemAmount(Items.redstone, 0.15));
		cost.add(new ItemAmount(Items.glowstone_dust, 0.1));
	}
}