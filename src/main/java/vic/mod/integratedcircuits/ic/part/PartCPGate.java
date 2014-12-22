package vic.mod.integratedcircuits.ic.part;

import net.minecraft.init.Items;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.ic.CircuitPart;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.CraftingAmount;
import vic.mod.integratedcircuits.misc.ItemAmount;
import vic.mod.integratedcircuits.misc.Vec2;

/** Rotateable Part **/
public abstract class PartCPGate extends CircuitPart
{
	public final int getRotation(Vec2 pos, ICircuit parent)
	{
		return (getState(pos, parent) & 48) >> 4;
	}
	
	public final void setRotation(Vec2 pos, ICircuit parent, int rotation)
	{
		setState(pos, parent, getState(pos, parent) & ~48 | rotation << 4);
		notifyNeighbours(pos, parent);
	}
	
	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) 
	{
		if(button == 0 && !ctrl)
		{
			int rot = getRotation(pos, parent) + 1;
			setRotation(pos, parent, rot > 3 ? 0 : rot);
		}
		markForUpdate(pos, parent);
	}
	
	public ForgeDirection toInternal(Vec2 pos, ICircuit parent, ForgeDirection dir)
	{
		return ForgeDirection.getOrientation((dir.ordinal() - 2 + 4 - getRotation(pos, parent)) % 4 + 2);
	}
	
	public ForgeDirection toExternal(Vec2 pos, ICircuit parent, ForgeDirection dir)
	{
		return ForgeDirection.getOrientation((dir.ordinal() - 2 + getRotation(pos, parent)) % 4 + 2);
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