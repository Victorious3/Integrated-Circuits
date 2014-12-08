package vic.mod.integratedcircuits.ic.part;

import net.minecraft.init.Items;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.ic.CircuitPart;
import vic.mod.integratedcircuits.misc.CraftingAmount;
import vic.mod.integratedcircuits.misc.ItemAmount;

/** Rotateable Part **/
public abstract class PartGate extends CircuitPart
{
	public final int getRotation()
	{
		return (getState() & 48) >> 4;
	}
	
	public final void setRotation(int rotation)
	{
		setState(getState() & ~48 | rotation << 4);
		notifyNeighbours();
	}
	
	@Override
	public void onClick(int button, boolean ctrl) 
	{
		if(button == 0 && !ctrl)
		{
			int rot = getRotation() + 1;
			setRotation(rot > 3 ? 0 : rot);
		}
		markForUpdate();
	}

	@Override
	public void onScheduledTick() 
	{
		notifyNeighbours();
	}

	@Override
	public void getCraftingCost(CraftingAmount cost) 
	{
		cost.add(new ItemAmount(Items.redstone, 0.048));
		cost.add(new ItemAmount(IntegratedCircuits.itemSiliconDrop, 0.1));
	}
}