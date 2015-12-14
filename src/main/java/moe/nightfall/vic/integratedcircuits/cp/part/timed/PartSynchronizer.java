package moe.nightfall.vic.integratedcircuits.cp.part.timed;

import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class PartSynchronizer extends PartDelayedAction {
	public final BooleanProperty PROP_IN_EAST = new BooleanProperty("IN_EAST", stitcher);
	public final BooleanProperty PROP_IN_WEST = new BooleanProperty("IN_WEST", stitcher);
	public final BooleanProperty PROP_OLD_EAST = new BooleanProperty("OLD_EAST", stitcher);
	public final BooleanProperty PROP_OLD_WEST = new BooleanProperty("OLD_WEST", stitcher);

	@Override
	public int getDelay(Vec2 pos, ICircuit parent) {
		return 2;
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent) {
		scheduleTick(pos, parent);
	}

	@Override
	public void onScheduledTick(Vec2 pos, ICircuit parent) {
		super.onScheduledTick(pos, parent);
		EnumFacing west = toExternal(pos, parent, EnumFacing.WEST);
		boolean westIn = getInputFromSide(pos, parent, west);
		boolean eastIn = getInputFromSide(pos, parent, west.getOpposite());
		if (getInputFromSide(pos, parent, toExternal(pos, parent, EnumFacing.SOUTH))) {
			setProperty(pos, parent, PROP_IN_EAST, false);
			setProperty(pos, parent, PROP_IN_WEST, false);
			setDelay(pos, parent, false);
			notifyNeighbours(pos, parent);
		} else if (!isDelayActive(pos, parent)) {
			if (westIn && !getProperty(pos, parent, PROP_OLD_WEST))
				setProperty(pos, parent, PROP_IN_WEST, true);
			if (eastIn && !getProperty(pos, parent, PROP_OLD_EAST))
				setProperty(pos, parent, PROP_IN_EAST, true);
			if (getProperty(pos, parent, PROP_IN_WEST)
					&& getProperty(pos, parent, PROP_IN_EAST)) {
				setDelay(pos, parent, true);
				setProperty(pos, parent, PROP_IN_EAST, false);
				setProperty(pos, parent, PROP_IN_WEST, false);
				notifyNeighbours(pos, parent);
			}
		}
		setProperty(pos, parent, PROP_OLD_WEST, westIn);
		setProperty(pos, parent, PROP_OLD_EAST, eastIn);
	}

	@Override
	public void onDelay(Vec2 pos, ICircuit parent) {
		notifyNeighbours(pos, parent);
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, EnumFacing side) {
		EnumFacing s2 = toInternal(pos, parent, side);
		if (s2 == EnumFacing.NORTH)
			return isDelayActive(pos, parent);
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(10, 1);
	}
}
