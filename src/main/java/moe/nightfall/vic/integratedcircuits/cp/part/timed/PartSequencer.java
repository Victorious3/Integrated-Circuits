package moe.nightfall.vic.integratedcircuits.cp.part.timed;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import net.minecraftforge.common.util.ForgeDirection;

public class PartSequencer extends PartDelayedAction implements IConfigurableDelay {
	public final IntProperty PROP_OUTPUT_SIDE = new IntProperty("OUTPUT_SIDE", stitcher, 3);
	public final IntProperty PROP_DELAY = new IntProperty("DELAY", stitcher, 255);

	@Override
	protected int getDelay(Vec2 pos, ICircuit parent) {
		return getConfigurableDelay(pos, parent);
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
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(3, 1);
	}

	@Override
	public void onPlaced(Vec2 pos, ICircuit parent) {
		updateInput(pos, parent);
		setProperty(pos, parent, PROP_OUTPUT_SIDE, 0);
		setConfigurableDelay(pos, parent, 10);
		setDelay(pos, parent, true);
		notifyNeighbours(pos, parent);
	}

	@Override
	public void onDelay(Vec2 pos, ICircuit parent) {
		cycleProperty(pos, parent, PROP_OUTPUT_SIDE);
		notifyNeighbours(pos, parent);
		setDelay(pos, parent, true);
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		return MiscUtils.getDirection(getProperty(pos, parent, PROP_OUTPUT_SIDE)) == toInternal(pos, parent, side);
	}
}
