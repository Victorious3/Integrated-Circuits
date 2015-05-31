package moe.nightfall.vic.integratedcircuits.cp.part;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import net.minecraftforge.common.util.ForgeDirection;

//TODO Is currently giving a one tick pulse, might cause problems with other gates.
public class PartSynchronizer extends PartCPGate {
	public final BooleanProperty PROP_IN_EAST = new BooleanProperty("IN_EAST", stitcher);
	public final BooleanProperty PROP_IN_WEST = new BooleanProperty("IN_WEST", stitcher);
	public final BooleanProperty PROP_OUT = new BooleanProperty("OUT", stitcher);

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		updateInput(pos, parent);
		ForgeDirection s2 = toInternal(pos, parent, side);
		boolean input = getInputFromSide(pos, parent, s2);

		if (s2 == ForgeDirection.SOUTH && getInputFromSide(pos, parent, s2)) {
			setProperty(pos, parent, PROP_IN_EAST, false);
			setProperty(pos, parent, PROP_IN_WEST, false);
			setProperty(pos, parent, PROP_OUT, false);
		} else if (s2 == ForgeDirection.EAST
				&& !getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH)) && input)
			setProperty(pos, parent, PROP_IN_EAST, true);
		else if (s2 == ForgeDirection.WEST
				&& !getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH)) && input)
			setProperty(pos, parent, PROP_IN_WEST, true);

		if (getProperty(pos, parent, PROP_IN_EAST) && getProperty(pos, parent, PROP_IN_WEST)) {
			setProperty(pos, parent, PROP_IN_EAST, false);
			setProperty(pos, parent, PROP_IN_WEST, false);
			setProperty(pos, parent, PROP_OUT, true);
			scheduleTick(pos, parent);
		}
	}

	@Override
	public void onScheduledTick(Vec2 pos, ICircuit parent) {
		notifyNeighbours(pos, parent);
		if (getProperty(pos, parent, PROP_OUT)) {
			setProperty(pos, parent, PROP_OUT, false);
			scheduleTick(pos, parent);
		}
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
