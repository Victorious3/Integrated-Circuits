package moe.nightfall.vic.integratedcircuits.ic.part.cell;

import moe.nightfall.vic.integratedcircuits.ic.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.ic.part.PartCPGate;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;

public class PartInvertCell extends PartBufferCell {
	@Override
	public Category getCategory() {
		return Category.CELL;
	}

	@Override
	public void renderPart(Vec2 pos, ICircuit parent, double x, double y, int type) {
		CircuitPartRenderer.renderPartCell(pos, parent, this, x, y, type);

		CircuitPartRenderer.addQuad(x, y, 5 * 16, 2 * 16, 16, 16,  this.getRotation(pos, parent));
	}

	@Override
	protected void calcOutput(Vec2 pos, ICircuit parent) {
		super.calcOutput(pos, parent);
		setOutput(pos, parent, !getOutput(pos, parent));
	}
}
