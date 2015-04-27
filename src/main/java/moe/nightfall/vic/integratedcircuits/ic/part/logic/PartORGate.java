package moe.nightfall.vic.integratedcircuits.ic.part.logic;

import moe.nightfall.vic.integratedcircuits.ic.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.ic.part.Part3I1O;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraftforge.common.util.ForgeDirection;

public class PartORGate extends Part3I1O {
	@Override
	public void calcOutput(Vec2 pos, ICircuit parent) {
		ForgeDirection s3 = toExternal(pos, parent, ForgeDirection.SOUTH);
		ForgeDirection s4 = toExternal(pos, parent, ForgeDirection.EAST);
		ForgeDirection s5 = s4.getOpposite();

		setOutput(pos, parent, getInputFromSide(pos, parent, s3) || getInputFromSide(pos, parent, s4)
				|| getInputFromSide(pos, parent, s5));
	}

	@Override
	public void renderPart(Vec2 pos, ICircuit parent, double x, double y, int type) {
		CircuitPartRenderer.renderPartGate(pos, parent, this, x, y, type);

		CircuitPartRenderer.addQuad(x, y, 8 * 16, 0, 16, 16, this.getRotation(pos, parent));
	}
}
