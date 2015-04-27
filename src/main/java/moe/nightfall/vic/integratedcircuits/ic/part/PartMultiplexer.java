package moe.nightfall.vic.integratedcircuits.ic.part;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.ic.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraftforge.common.util.ForgeDirection;

public class PartMultiplexer extends PartSimpleGate {
	@Override
	public Category getCategory() {
		return Category.MISC;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(0, 1);
	}

	@Override
	protected void calcOutput(Vec2 pos, ICircuit parent) {
		if (getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH)))
			setOutput(pos, parent, getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.WEST)));
		else
			setOutput(pos, parent, getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.EAST)));
	}

	@Override
	protected boolean hasOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection fd) {
		return fd == ForgeDirection.NORTH;
	}
}
