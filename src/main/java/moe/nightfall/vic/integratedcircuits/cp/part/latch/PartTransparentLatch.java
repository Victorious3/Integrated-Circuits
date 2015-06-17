package moe.nightfall.vic.integratedcircuits.cp.part.latch;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.cp.part.PartCPGate;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import net.minecraftforge.common.util.ForgeDirection;

public class PartTransparentLatch extends PartCPGate {
	public final BooleanProperty PROP_OUT = new BooleanProperty("OUT", stitcher);

	@Override
	public void onPlaced(Vec2 pos, ICircuit parent) {
		setProperty(pos, parent, PROP_OUT, false);
		updateInput(pos, parent);
		onPostponedInputChange(pos, parent, ForgeDirection.UNKNOWN);
		notifyNeighbours(pos, parent);
	}

	@Override
	public void onAfterRotation(Vec2 pos, ICircuit parent) {
		onPostponedInputChange(pos, parent, ForgeDirection.UNKNOWN);
		notifyNeighbours(pos, parent);
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		updateInput(pos, parent);
		ForgeDirection s2 = toInternal(pos, parent, side);
		if (s2 == ForgeDirection.WEST || s2 == ForgeDirection.SOUTH)
			togglePostponedInputChange(pos, parent, side);
	}

	@Override
	public void onPostponedInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		ForgeDirection s2 = toInternal(pos, parent, side);
		if (s2 == ForgeDirection.SOUTH ||
				getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH))) {
			setProperty(pos, parent, PROP_OUT,
					getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.WEST)));
			notifyNeighbours(pos, parent);
		}
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		ForgeDirection s2 = toInternal(pos, parent, side);
		if (s2 == ForgeDirection.NORTH || s2 == ForgeDirection.EAST)
			return getProperty(pos, parent, PROP_OUT);
		return false;
	}

	@Override
	public void renderPart(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		CircuitPartRenderer.renderPartGate(pos, parent, this, x, y, type);
		CircuitPartRenderer.addQuad(x, y, 9 * 16, 16, 16, 16, this.getRotation(pos, parent));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(9, 1);
	}

	@Override
	public Category getCategory() {
		return Category.LATCH;
	}
}
