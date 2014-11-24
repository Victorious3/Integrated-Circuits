package vic.mod.integratedcircuits.ic.part.timed;

import java.util.Random;

import net.minecraft.init.Items;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.misc.CraftingAmount;
import vic.mod.integratedcircuits.misc.ItemAmount;
import vic.mod.integratedcircuits.misc.MiscUtils;

public class PartRandomizer extends PartDelayedAction
{
	@Override
	protected int getDelay() 
	{
		return 2;
	}
	
	@Override
	public void onPlaced()
	{
		setDelay(true);
	}

	@Override
	public void onDelay() 
	{
		setState(getState() & ~229376);
		setState(getState() | new Random().nextInt(7) << 15);
		notifyNeighbours();
		setDelay(true);
	}

	@Override
	public boolean getOutputToSide(ForgeDirection side) 
	{
		if(!getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, getRotation()))) return false;
		ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
		if(s2 == ForgeDirection.SOUTH) return false;
		int rand = (getState() & 229376) >> 15;
		if(s2 == ForgeDirection.EAST && (rand >> 2 & 1) == 1) return true;
		if(s2 == ForgeDirection.WEST && (rand >> 1 & 1) == 1) return true;
		if(s2 == ForgeDirection.NORTH && (rand & 1) == 1) return true;
		return false;
	}

	@Override
	public void onInputChange(ForgeDirection side) 
	{
		super.onInputChange(side);
		ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
		if(s2 != ForgeDirection.SOUTH) return;
		if(!getInputFromSide(side)) setDelay(false);
		else setDelay(true);
	}
	
	@Override
	public void getCraftingCost(CraftingAmount cost) 
	{
		cost.add(new ItemAmount(Items.redstone, 0.15));
		cost.add(new ItemAmount(Items.glowstone_dust, 0.1));
	}
}