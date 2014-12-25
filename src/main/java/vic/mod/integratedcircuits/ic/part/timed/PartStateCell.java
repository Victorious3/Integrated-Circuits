package vic.mod.integratedcircuits.ic.part.timed;

import java.util.ArrayList;

import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.CraftingAmount;
import vic.mod.integratedcircuits.misc.ItemAmount;
import vic.mod.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import vic.mod.integratedcircuits.misc.PropertyStitcher.IntProperty;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartStateCell extends PartDelayedAction implements IConfigurableDelay
{
	public final IntProperty PROP_DELAY = new IntProperty(stitcher, 255);
	private final BooleanProperty PROP_OUT_WEST = new BooleanProperty(stitcher);
	private final BooleanProperty PROP_OUT_NORTH = new BooleanProperty(stitcher);

	@Override
	public int getConfigurableDelay(Vec2 pos, ICircuit parent) 
	{
		return getProperty(pos, parent, PROP_DELAY);
	}

	@Override
	public void setConfigurableDelay(Vec2 pos, ICircuit parent, int delay) 
	{
		setProperty(pos, parent, PROP_DELAY, delay);
	}
	
	@Override
	protected int getDelay(Vec2 pos, ICircuit parent)
	{
		if(getProperty(pos, parent, PROP_OUT_NORTH)) return 2;
		return getConfigurableDelay(pos, parent);
	}
	
	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		ForgeDirection s2 = toInternal(pos, parent, side);
		if(s2 == ForgeDirection.WEST && getProperty(pos, parent, PROP_OUT_WEST)) return true;
		if(s2 == ForgeDirection.NORTH && getProperty(pos, parent, PROP_OUT_NORTH)) return true;
		return false;
	}

	@Override
	public void onDelay(Vec2 pos, ICircuit parent) 
	{
		if(getProperty(pos, parent, PROP_OUT_NORTH)) 
			setProperty(pos, parent, PROP_OUT_NORTH, false);
		else if(getProperty(pos, parent, PROP_OUT_WEST)) 
		{
			setProperty(pos, parent, PROP_OUT_WEST, false);
			setProperty(pos, parent, PROP_OUT_NORTH, true);
			setDelay(pos, parent, true);
		}
		super.onDelay(pos, parent);
	}

	@Override
	public void onPlaced(Vec2 pos, ICircuit parent)
	{
		setProperty(pos, parent, PROP_DELAY, 20);
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		updateInput(pos, parent);
		ForgeDirection s2 = toInternal(pos, parent, side);
		if(s2 == ForgeDirection.SOUTH)
		{
			if(getInputFromSide(pos, parent, side))
			{
				setProperty(pos, parent, PROP_OUT_WEST, true);
				setProperty(pos, parent, PROP_OUT_NORTH, false);
				notifyNeighbours(pos, parent);
			}
			else if(!getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.EAST)))
				setDelay(pos, parent, true);
		}
		else if(s2 == ForgeDirection.EAST && getProperty(pos, parent, PROP_OUT_WEST))
		{
			if(getInputFromSide(pos, parent, side)) setDelay(pos, parent, false);
			else if(!getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH)))
				setDelay(pos, parent, true);
			notifyNeighbours(pos, parent);
		}
	}
	
	@Override
	public void getCraftingCost(CraftingAmount cost) 
	{
		cost.add(new ItemAmount(Items.redstone, 0.15));
		cost.add(new ItemAmount(Items.glowstone_dust, 0.1));
	}
	
	@Override
	public ArrayList<String> getInformation(Vec2 pos, ICircuit parent, boolean edit, boolean ctrlDown) 
	{
		ArrayList<String> text = super.getInformation(pos, parent, edit, ctrlDown);
		if(edit && ctrlDown) text.add(I18n.format("gui.integratedcircuits.cad.delay"));
		return text;
	}
}