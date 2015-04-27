package moe.nightfall.vic.integratedcircuits.ic.part.timed;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.ic.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import net.minecraftforge.common.util.ForgeDirection;

public class PartSequencer extends PartTimer {
	public final IntProperty PROP_OUTPUT_SIDE = new IntProperty("OUTPUT_SIDE", stitcher, 3);

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(3, 1);
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		updateInput(pos, parent);
	}

	@Override
	public void onDelay(Vec2 pos, ICircuit parent) {
		if (!getProperty(pos, parent, PROP_OUT))
			cycleProperty(pos, parent, PROP_OUTPUT_SIDE);
		super.onDelay(pos, parent);
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		if (MiscUtils.getDirection(getProperty(pos, parent, PROP_OUTPUT_SIDE)) == toInternal(pos, parent, side))
			return getProperty(pos, parent, PROP_OUT);
		else
			return false;
	}
}