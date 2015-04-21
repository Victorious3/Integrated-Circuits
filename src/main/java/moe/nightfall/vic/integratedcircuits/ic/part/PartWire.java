package moe.nightfall.vic.integratedcircuits.ic.part;

import moe.nightfall.vic.integratedcircuits.ic.CircuitPart;
import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.CraftingAmount;
import moe.nightfall.vic.integratedcircuits.misc.ItemAmount;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import net.minecraft.init.Items;
import net.minecraftforge.common.util.ForgeDirection;

public class PartWire extends CircuitPart
{
	public final IntProperty PROP_COLOR = new IntProperty("COLOR", stitcher, 2);

	@Override
	public Category getCategory() {
		return Category.WIRE;
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		return getInput(pos, parent) && !getInputFromSide(pos, parent, side);
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		super.onInputChange(pos, parent, side);
		notifyNeighbours(pos, parent);
	}

	public int getColor(Vec2 pos, ICircuit parent)
	{
		return getProperty(pos, parent, PROP_COLOR);
	}

	@Override
	public boolean canConnectToSide(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		CircuitPart part = getNeighbourOnSide(pos, parent, side);
		if(part instanceof PartWire)
		{
			int pcolor = ((PartWire)part).getColor(pos.offset(side), parent);
			int color = getColor(pos, parent);
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
	public String getName(Vec2 pos, ICircuit parent) 
	{
		return super.getName(pos, parent) + "." + getColor(pos, parent);
	}
}
