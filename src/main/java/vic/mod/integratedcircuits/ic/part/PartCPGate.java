package vic.mod.integratedcircuits.ic.part;

import net.minecraft.init.Items;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.ic.CircuitPart;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.CraftingAmount;
import vic.mod.integratedcircuits.misc.ItemAmount;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.misc.PropertyStitcher.IntProperty;
import vic.mod.integratedcircuits.misc.Vec2;

/** Rotateable Part **/
public abstract class PartCPGate extends CircuitPart
{
	public final IntProperty PROP_ROTATION = new IntProperty(stitcher, 3);
	
	public final int getRotation(Vec2 pos, ICircuit parent)
	{
		return getProperty(pos, parent, PROP_ROTATION);
	}
	
	public final void setRotation(Vec2 pos, ICircuit parent, int rotation)
	{
		setProperty(pos, parent, PROP_ROTATION, rotation);
		notifyNeighbours(pos, parent);
	}
	
	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) 
	{
		if(button == 0 && !ctrl)
			cycleProperty(pos, parent, PROP_ROTATION);
		notifyNeighbours(pos, parent);
	}
	
	public ForgeDirection toInternal(Vec2 pos, ICircuit parent, ForgeDirection dir)
	{
		return MiscUtils.rotn(dir, -getRotation(pos, parent));
		//TODO Find a better replacement or fix the original method
//		return ForgeDirection.getOrientation((dir.ordinal() - 2 + 4 - getRotation(pos, parent)) % 4 + 2);
	}
	
	public ForgeDirection toExternal(Vec2 pos, ICircuit parent, ForgeDirection dir)
	{
		return MiscUtils.rotn(dir, getRotation(pos, parent));
//		return ForgeDirection.getOrientation((dir.ordinal() - 2 + getRotation(pos, parent)) % 4 + 2);
	}

	@Override
	public void onScheduledTick(Vec2 pos, ICircuit parent) 
	{
		notifyNeighbours(pos, parent);
	}

	@Override
	public void getCraftingCost(CraftingAmount cost) 
	{
		cost.add(new ItemAmount(Items.redstone, 0.048));
		cost.add(new ItemAmount(IntegratedCircuits.itemSiliconDrop, 0.1));
	}
}