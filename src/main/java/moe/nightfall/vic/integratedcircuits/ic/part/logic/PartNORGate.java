package moe.nightfall.vic.integratedcircuits.ic.part.logic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.ic.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;

public class PartNORGate extends PartORGate {
	@Override
	public Category getCategory() {
		return Category.NGATE;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(11, 0);
	}

	@Override
	public void calcOutput(Vec2 pos, ICircuit parent) {
		super.calcOutput(pos, parent);
		setOutput(pos, parent, !getOutput(pos, parent));
	}
}
