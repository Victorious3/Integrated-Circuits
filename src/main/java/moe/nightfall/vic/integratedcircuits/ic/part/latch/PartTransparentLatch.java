package moe.nightfall.vic.integratedcircuits.ic.part.latch;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.ic.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraftforge.common.util.ForgeDirection;

public class PartTransparentLatch extends PartLatch {
	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		super.onInputChange(pos, parent, side);
		ForgeDirection s2 = toInternal(pos, parent, side);
		boolean lock = getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH));
		if (s2 == ForgeDirection.WEST && lock || s2 == ForgeDirection.SOUTH && !lock) {
			if (getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.WEST)))
				setProperty(pos, parent, PROP_OUT, true);
			else
				setProperty(pos, parent, PROP_OUT, false);
			scheduleTick(pos, parent);
		}
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		ForgeDirection s2 = toInternal(pos, parent, side);
		if (s2 == ForgeDirection.NORTH || s2 == ForgeDirection.EAST)
			return getProperty(pos, parent, PROP_TMP);
		return false;
	}

	@Override
	public void renderPart(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		CircuitPartRenderer.renderPartGate(pos, parent, this, x, y, type);

		CircuitPartRenderer.addQuad(x, y, 9 * 16, 16, 16, 16, this.getRotation(pos, parent));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(9, 1);
	}
}