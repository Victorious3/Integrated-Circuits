package moe.nightfall.vic.integratedcircuits.cp.part.timed;

import java.util.ArrayList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.CraftingAmount;
import moe.nightfall.vic.integratedcircuits.misc.ItemAmount;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraftforge.common.util.ForgeDirection;

public class PartStateCell extends PartDelayedAction implements IConfigurableDelay {
	public final IntProperty PROP_DELAY = new IntProperty("DELAY", stitcher, 255);
	private final BooleanProperty PROP_OUT_WEST = new BooleanProperty("OUT_WEST", stitcher);
	private final BooleanProperty PROP_OUT_NORTH = new BooleanProperty("OUT_NORTH", stitcher);

	static final int pulseDelay = 2;

	@Override
	public int getConfigurableDelay(Vec2 pos, ICircuit parent) {
		return getProperty(pos, parent, PROP_DELAY);
	}

	@Override
	public void setConfigurableDelay(Vec2 pos, ICircuit parent, int delay) {
		setProperty(pos, parent, PROP_DELAY, delay);
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		ForgeDirection s2 = toInternal(pos, parent, side);
		if (s2 == ForgeDirection.WEST)
			return getProperty(pos, parent, PROP_OUT_WEST);
		if (s2 == ForgeDirection.NORTH)
			return getProperty(pos, parent, PROP_OUT_NORTH);
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(4, 1);
	}

	@Override
	public void onDelay(Vec2 pos, ICircuit parent) {
		if (getProperty(pos, parent, PROP_OUT_NORTH))
			setProperty(pos, parent, PROP_OUT_NORTH, false);
		else if (getProperty(pos, parent, PROP_OUT_WEST)) {
			setProperty(pos, parent, PROP_OUT_WEST, false);
			setProperty(pos, parent, PROP_OUT_NORTH, true);
			setDelay(pos, parent, pulseDelay);
		}
		super.onDelay(pos, parent);
	}

	@Override
	public void onPlaced(Vec2 pos, ICircuit parent) {
		setProperty(pos, parent, PROP_DELAY, 20);
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		updateInput(pos, parent);
		ForgeDirection s2 = toInternal(pos, parent, side);
		if (s2 == ForgeDirection.SOUTH) {
			if (getInputFromSide(pos, parent, side)) {
				setProperty(pos, parent, PROP_OUT_WEST, true);
				setProperty(pos, parent, PROP_OUT_NORTH, false);
				notifyNeighbours(pos, parent);
				setDelay(pos, parent, 0);
			} else if (!getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.EAST)))
				setDelay(pos, parent, getConfigurableDelay(pos, parent));
		} else if (s2 == ForgeDirection.EAST && getProperty(pos, parent, PROP_OUT_WEST)) {
			if (getInputFromSide(pos, parent, side))
				setDelay(pos, parent, 0);
			else if (!getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH)))
				setDelay(pos, parent, getConfigurableDelay(pos, parent));
		}
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
