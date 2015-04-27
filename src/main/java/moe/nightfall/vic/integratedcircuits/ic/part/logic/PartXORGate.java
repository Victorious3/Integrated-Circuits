package moe.nightfall.vic.integratedcircuits.ic.part.logic;

import moe.nightfall.vic.integratedcircuits.ic.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.ic.part.PartSimpleGate;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraftforge.common.util.ForgeDirection;

public class PartXORGate extends PartSimpleGate {
	@Override
	public boolean canConnectToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		return toInternal(pos, parent, side) != ForgeDirection.SOUTH;
	}

	@Override
	public void renderPart(Vec2 pos, ICircuit parent, double x, double y, int type) {
		CircuitPartRenderer.renderPartGate(pos, parent, this, x, y, type);

		CircuitPartRenderer.addQuad(x, y, 9 * 16, 0, 16, 16, this.getRotation(pos, parent));
	}

	@Override
	protected boolean hasOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection fd) {
		return fd == ForgeDirection.NORTH;
	}

	@Override
	protected void calcOutput(Vec2 pos, ICircuit parent) {
		setOutput(
				pos,
				parent,
				getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.EAST)) != getInputFromSide(pos,
						parent, toExternal(pos, parent, ForgeDirection.WEST)));
	}
}