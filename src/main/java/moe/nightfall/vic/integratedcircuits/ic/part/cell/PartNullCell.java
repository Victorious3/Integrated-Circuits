package moe.nightfall.vic.integratedcircuits.ic.part.cell;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.ic.CircuitPart;
import moe.nightfall.vic.integratedcircuits.ic.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraftforge.common.util.ForgeDirection;

public class PartNullCell extends CircuitPart {
	@Override
	public Category getCategory() {
		return Category.CELL;
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		return getInputFromSide(pos, parent, side.getOpposite()) && !getInputFromSide(pos, parent, side);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderPart(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		CircuitPartRenderer.renderPartCell(pos, parent, this, x, y, type);

		CircuitPartRenderer.addQuad(x, y, 16, 2 * 16, 16, 16, 0);
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		updateInput(pos, parent);
		notifyNeighbours(pos, parent);
	}
}
