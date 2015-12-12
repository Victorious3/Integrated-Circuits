package moe.nightfall.vic.integratedcircuits.cp.part;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.Config;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPart;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.RenderUtils;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.common.util.ForgeDirection;

public class PartTorch extends CircuitPart {
	@Override
	public Category getCategory() {
		return Category.TORCH;
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderPart(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		RenderUtils.applyColorIRGBA(Tessellator.instance, Config.colorGreen);

		int con = CircuitPartRenderer.checkConnections(pos, parent, this);
		if ((con & 8) > 0)
			CircuitPartRenderer.addQuad(x, y, 2 * 16, 0, 16, 16);
		if ((con & 4) > 0)
			CircuitPartRenderer.addQuad(x, y, 4 * 16, 0, 16, 16);
		if ((con & 2) > 0)
			CircuitPartRenderer.addQuad(x, y, 1 * 16, 0, 16, 16);
		if ((con & 1) > 0)
			CircuitPartRenderer.addQuad(x, y, 3 * 16, 0, 16, 16);

		CircuitPartRenderer.addQuad(x, y, 13 * 16, 0, 16, 16);
	}
}
