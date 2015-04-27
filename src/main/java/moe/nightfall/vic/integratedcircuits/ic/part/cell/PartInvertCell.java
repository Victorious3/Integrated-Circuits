package moe.nightfall.vic.integratedcircuits.ic.part.cell;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.ic.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;

public class PartInvertCell extends PartBufferCell {
	@Override
	public Category getCategory() {
		return Category.CELL;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderPart(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		CircuitPartRenderer.renderPartCell(pos, parent, this, x, y, type);

		Vec2 textureOffset = getTextureOffset(pos, parent, x, y, type);
		CircuitPartRenderer.addQuad(x, y, textureOffset.x * 16, textureOffset.y * 16, 16, 16,  this.getRotation(pos, parent));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(5, 2);
	}

	@Override
	protected void calcOutput(Vec2 pos, ICircuit parent) {
		super.calcOutput(pos, parent);
		setOutput(pos, parent, !getOutput(pos, parent));
	}
}