package moe.nightfall.vic.integratedcircuits.cp.part.timed;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import net.minecraftforge.common.util.ForgeDirection;

public class PartSynchronizer extends PartDelayedAction {
	public final BooleanProperty PROP_IN_EAST = new BooleanProperty("IN_EAST", stitcher);
	public final BooleanProperty PROP_IN_WEST = new BooleanProperty("IN_WEST", stitcher);
	public final BooleanProperty PROP_OUT = new BooleanProperty("OUT", stitcher);

	@Override
	protected int getDelay(Vec2 pos, ICircuit parent) {
		return 2;
	}

	@Override
	public void onAfterRotation(Vec2 pos, ICircuit parent) {
		updateInput(pos, parent);
		if (getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH))) {
			setProperty(pos, parent, PROP_IN_EAST, false);
			setProperty(pos, parent, PROP_IN_WEST, false);
			if (getProperty(pos, parent, PROP_OUT)) {
				setProperty(pos, parent, PROP_OUT, false);
				setDelay(pos, parent, false);
			}
		}
		notifyNeighbours(pos, parent);
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		updateInput(pos, parent);
		if (side != toExternal(pos, parent, ForgeDirection.NORTH))
			togglePostponedInputChange(pos, parent, side);
	}

	@Override
	public void onPostponedInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		// Nothing happens on falling edge
		if (!getInputFromSide(pos, parent, side))
			return;

		ForgeDirection s2 = toInternal(pos, parent, side);

		if (s2 == ForgeDirection.SOUTH) {
			// Reset
			setProperty(pos, parent, PROP_IN_EAST, false);
			setProperty(pos, parent, PROP_IN_WEST, false);
			if (getProperty(pos, parent, PROP_OUT)) {
				setProperty(pos, parent, PROP_OUT, false);
				setDelay(pos, parent, false);
				notifyNeighbours(pos, parent);
			}
		} else if (!getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH))) {
			// Reset is low
			if (s2 == ForgeDirection.EAST)
				setProperty(pos, parent, PROP_IN_EAST, true);
			else if (s2 == ForgeDirection.WEST)
				setProperty(pos, parent, PROP_IN_WEST, true);
			if (getProperty(pos, parent, PROP_IN_EAST) && getProperty(pos, parent, PROP_IN_WEST)) {
				setProperty(pos, parent, PROP_IN_EAST, false);
				setProperty(pos, parent, PROP_IN_WEST, false);
				setProperty(pos, parent, PROP_OUT, true);
				setDelay(pos, parent, true);
				notifyNeighbours(pos, parent);
			}
		}
	}

	@Override
	public void onDelay(Vec2 pos, ICircuit parent) {
		setProperty(pos, parent, PROP_OUT, false);
		notifyNeighbours(pos, parent);
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		ForgeDirection s2 = toInternal(pos, parent, side);
		if (s2 == ForgeDirection.NORTH)
			return getProperty(pos, parent, PROP_OUT);
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(10, 1);
	}
}
