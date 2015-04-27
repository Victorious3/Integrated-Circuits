package moe.nightfall.vic.integratedcircuits.ic.part.logic;

import moe.nightfall.vic.integratedcircuits.ic.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.ic.part.Part1I3O;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraftforge.common.util.ForgeDirection;

public class PartNOTGate extends Part1I3O {
	@Override
	public Category getCategory() {
		return Category.NGATE;
	}

	@Override
	public void renderPart(Vec2 pos, ICircuit parent, double x, double y, int type) {
		CircuitPartRenderer.renderPartGate(pos, parent, this, x, y, type);

		CircuitPartRenderer.addQuad(x, y, 15 * 16, 0, 16, 16, this.getRotation(pos, parent));
	}

	@Override
	public void calcOutput(Vec2 pos, ICircuit parent) {
		setOutput(pos, parent, !getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH)));
	}
}
