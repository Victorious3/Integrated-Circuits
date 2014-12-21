package vic.mod.integratedcircuits.ic.part.timed;

import java.util.Random;

import net.minecraft.init.Items;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.CraftingAmount;
import vic.mod.integratedcircuits.misc.ItemAmount;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartRandomizer extends PartDelayedAction
{
	@Override
	protected int getDelay(Vec2 pos, ICircuit parent) 
	{
		return 2;
	}
	
	@Override
	public void onPlaced(Vec2 pos, ICircuit parent)
	{
		setDelay(pos, parent, true);
	}

	@Override
	public void onDelay(Vec2 pos, ICircuit parent) 
	{
		setState(pos, parent, getState(pos, parent) & ~229376);
		setState(pos, parent, getState(pos, parent) | new Random().nextInt(7) << 15);
		notifyNeighbours(pos, parent);
		setDelay(pos, parent, true);
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		if(!getInputFromSide(pos, parent, MiscUtils.rotn(ForgeDirection.SOUTH, getRotation(pos, parent)))) return false;
		ForgeDirection s2 = toInternal(pos, parent, side);
		if(s2 == ForgeDirection.SOUTH) return false;
		int rand = (getState(pos, parent) & 229376) >> 15;
		if(s2 == ForgeDirection.EAST && (rand >> 2 & 1) == 1) return true;
		if(s2 == ForgeDirection.WEST && (rand >> 1 & 1) == 1) return true;
		if(s2 == ForgeDirection.NORTH && (rand & 1) == 1) return true;
		return false;
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		super.onInputChange(pos, parent, side);
		ForgeDirection s2 = toInternal(pos, parent, side);
		if(s2 != ForgeDirection.SOUTH) return;
		if(!getInputFromSide(pos, parent, side)) setDelay(pos, parent, false);
		else setDelay(pos, parent, true);
	}
	
	@Override
	public void getCraftingCost(CraftingAmount cost) 
	{
		cost.add(new ItemAmount(Items.redstone, 0.15));
		cost.add(new ItemAmount(Items.glowstone_dust, 0.1));
	}
}