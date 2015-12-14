package moe.nightfall.vic.integratedcircuits.cp.part.timed;

import java.util.ArrayList;

import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.CraftingAmount;
import moe.nightfall.vic.integratedcircuits.misc.ItemAmount;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


//TODO The timer should really use the tick method instead of scheduled ticks.
public class PartTimer extends PartDelayedAction implements IConfigurableDelay {
	public final BooleanProperty PROP_OUT = new BooleanProperty("OUT", stitcher);
	public final IntProperty PROP_DELAY = new IntProperty("DELAY", stitcher, 255);

	@Override
	protected int getDelay(Vec2 pos, ICircuit parent) {
		if (!getProperty(pos, parent, PROP_OUT))
			return getConfigurableDelay(pos, parent);
		else
			return 2;
	}

	@Override
	public int getConfigurableDelay(Vec2 pos, ICircuit parent) {
		return getProperty(pos, parent, PROP_DELAY);
	}

	@Override
	public void setConfigurableDelay(Vec2 pos, ICircuit parent, int delay) {
		setProperty(pos, parent, PROP_DELAY, delay);
	}

	@Override
	public void onPlaced(Vec2 pos, ICircuit parent) {
		setConfigurableDelay(pos, parent, 10);
		super.onPlaced(pos, parent);
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent) {
		scheduleTick(pos, parent);
	}

	@Override
	public void onScheduledTick(Vec2 pos, ICircuit parent) {
		super.onScheduledTick(pos, parent);
		if (getInputFromSide(pos, parent, toExternal(pos, parent, EnumFacing.SOUTH))) {
			setProperty(pos, parent, PROP_OUT, false);
			setDelay(pos, parent, false);
			notifyNeighbours(pos, parent);
		} else if (!isDelayActive(pos, parent))
			setDelay(pos, parent, true);
	}

	@Override
	public void onDelay(Vec2 pos, ICircuit parent) {
		invertProperty(pos, parent, PROP_OUT);
		setDelay(pos, parent, true);
		notifyNeighbours(pos, parent);
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, EnumFacing side) {
		if (toInternal(pos, parent, side) == EnumFacing.SOUTH)
			return false;
		return getProperty(pos, parent, PROP_OUT);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(2, 1);
	}

	@Override
	public void getCraftingCost(CraftingAmount cost, CircuitData parent, Vec2 pos) {
		cost.add(new ItemAmount(Items.redstone, 0.15));
		cost.add(new ItemAmount(Items.glowstone_dust, 0.1));
	}

	@Override
	public ArrayList<String> getInformation(Vec2 pos, ICircuit parent, boolean edit, boolean ctrlDown) {
		ArrayList<String> text = super.getInformation(pos, parent, edit, ctrlDown);
		if (edit && ctrlDown)
			text.add(I18n.format("gui.integratedcircuits.cad.delay"));
		return text;
	}
}
