package vic.mod.integratedcircuits.ic.part;

import net.minecraft.init.Items;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.CircuitPart;
import vic.mod.integratedcircuits.misc.CraftingAmount;
import vic.mod.integratedcircuits.misc.ItemAmount;

public class PartWire extends CircuitPart
{
	@Override
	public boolean getOutputToSide(ForgeDirection side) 
	{
		return getInput() && !getInputFromSide(side);
	}

	@Override
	public void onInputChange(ForgeDirection side) 
	{
		super.onInputChange(side);
		notifyNeighbours();
	}

	public int getColor()
	{
		return (getState() & ~16) >> 5;
	}

	@Override
	public boolean canConnectToSide(ForgeDirection side) 
	{
		CircuitPart part = getNeighbourOnSide(side);
		if(part instanceof PartWire)
		{
			int pcolor = ((PartWire)part).getColor();
			int color = getColor();
			if(pcolor == 0 || color == 0) return true;
			return color == pcolor;
		}
		return true;
	}
	
	@Override
	public void getCraftingCost(CraftingAmount cost) 
	{
		cost.add(new ItemAmount(Items.redstone, 0.05));
	}

	@Override
	public String getName() 
	{
		return super.getName() + "." + getColor();
	}
}