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

//TODO The timer should really use the tick method instead of scheduled ticks.
public class PartTimer extends PartDelayedAction implements IConfigurableDelay
{
	public final BooleanProperty PROP_OUT = new BooleanProperty(stitcher);
	public final IntProperty PROP_DELAY = new IntProperty(stitcher, 255);
	
	@Override
	protected int getDelay(Vec2 pos, ICircuit parent) 
	{
		if(!getProperty(pos, parent, PROP_OUT)) return getConfigurableDelay(pos, parent);
		else return 2;
	}
	
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
	public void onPlaced(Vec2 pos, ICircuit parent)
	{
		setState(pos, parent, 10 << 16);
		updateInput(pos, parent);
		if(!getInputFromSide(pos, parent, ForgeDirection.SOUTH)) 
			setDelay(pos, parent, true);
	}
	
	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		updateInput(pos, parent);
		if(toInternal(pos, parent, side) != ForgeDirection.SOUTH) return;
		setProperty(pos, parent, PROP_OUT, false);
		if(getInputFromSide(pos, parent, side))
		{
			setDelay(pos, parent, false);
			notifyNeighbours(pos, parent);
		}
		else setDelay(pos, parent, true);
	}

	@Override
	public void onDelay(Vec2 pos, ICircuit parent) 
	{
		invertProperty(pos, parent, PROP_OUT);
		setDelay(pos, parent, true);
		super.onDelay(pos, parent);
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		if(toInternal(pos, parent, side) == ForgeDirection.SOUTH) return false;
		return getProperty(pos, parent, PROP_OUT);
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