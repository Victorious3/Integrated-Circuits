package vic.mod.integratedcircuits.ic.part.timed;

import net.minecraft.init.Items;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.misc.CraftingAmount;
import vic.mod.integratedcircuits.misc.ItemAmount;
import vic.mod.integratedcircuits.misc.MiscUtils;

//TODO The timer should really use the tick method instead of scheduled ticks.
public class PartTimer extends PartDelayedAction implements IConfigurableDelay
{
	@Override
	protected int getDelay() 
	{
		if((getState() & 32768) == 0) return getConfigurableDelay();
		else return 2;
	}
	
	public int getConfigurableDelay()
	{
		return ((getState() & 16711680) >> 16);
	}
	
	public void setConfigurableDelay(int delay)
	{
		setState(getState() & ~16711680);
		setState(getState() | delay << 16);
	}

	@Override
	public void onPlaced()
	{
		setState(10 << 16);
		updateInput();
		if(!getInputFromSide(ForgeDirection.SOUTH)) setDelay(true);
	}
	
	@Override
	public void onInputChange(ForgeDirection side) 
	{
		updateInput();
		if(MiscUtils.rotn(side, -getRotation()) != ForgeDirection.SOUTH) return;
		setState(getState() & ~32768);
		if(getInputFromSide(side))
		{
			setDelay(false);
			notifyNeighbours();
		}
		else setDelay(true);
	}

	@Override
	public void onDelay() 
	{
		setState(getState() ^ 32768);
		setDelay(true);
		super.onDelay();
	}

	@Override
	public boolean getOutputToSide(ForgeDirection side) 
	{
		if(MiscUtils.rotn(side, -getRotation()) == ForgeDirection.SOUTH) return false;
		return (getState() & 32768) != 0;
	}
	
	@Override
	public void getCraftingCost(CraftingAmount cost) 
	{
		cost.add(new ItemAmount(Items.redstone, 0.15));
		cost.add(new ItemAmount(Items.glowstone_dust, 0.1));
	}
}