package moe.nightfall.vic.integratedcircuits.cp.part.cell;

import moe.nightfall.vic.integratedcircuits.cp.CircuitPart;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class PartNullCell extends CircuitPart {
	@Override
	public Category getCategory() {
		return Category.CELL;
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, EnumFacing side) {
		return getInputFromSide(pos, parent, side.getOpposite()) && !getInputFromSide(pos, parent, side);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderPart(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		CircuitPartRenderer.renderPartCell(pos, parent, this, x, y, type);

		CircuitPartRenderer.addQuad(x, y, 16, 2 * 16, 16, 16);
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent) {
		notifyNeighbours(pos, parent);
	}
}
