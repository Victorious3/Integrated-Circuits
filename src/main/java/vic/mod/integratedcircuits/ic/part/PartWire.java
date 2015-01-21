package vic.mod.integratedcircuits.ic.part;

import net.minecraft.init.Items;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.CircuitPart;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.CraftingAmount;
import vic.mod.integratedcircuits.misc.ItemAmount;
import vic.mod.integratedcircuits.misc.PropertyStitcher.IntProperty;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartWire extends CircuitPart
{
	public final IntProperty PROP_COLOR = new IntProperty("PROP_COLOR", stitcher, 2);
	
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