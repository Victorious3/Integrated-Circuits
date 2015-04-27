package moe.nightfall.vic.integratedcircuits.ic.part;

import moe.nightfall.vic.integratedcircuits.ic.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraftforge.common.util.ForgeDirection;

public class PartMultiplexer extends PartSimpleGate {
	@Override
	public Category getCategory() {
		return Category.MISC;
	}

	@Override
	public void renderPart(Vec2 pos, ICircuit parent, double x, double y, int type) {
		CircuitPartRenderer.renderPartGate(pos, parent, this, x, y, type);

		CircuitPartRenderer.addQuad(x, y, 0, 16, 16, 16, this.getRotation(pos, parent));
	}

	@Override
	protected void calcOutput(Vec2 pos, ICircuit parent) {
		if (getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH)))
			setOutput(pos, parent, getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.WEST)));
		else
			setOutput(pos, parent, getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.EAST)));
	}

	@Override
	protected boolean hasOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection fd) {
		return fd == ForgeDirection.NORTH;
	}
}
