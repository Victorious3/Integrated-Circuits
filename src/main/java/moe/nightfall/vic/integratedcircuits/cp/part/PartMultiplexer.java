package moe.nightfall.vic.integratedcircuits.cp.part;

import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


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
		if (getInputFromSide(pos, parent, toExternal(pos, parent, EnumFacing.SOUTH)))
			setOutput(pos, parent, getInputFromSide(pos, parent, toExternal(pos, parent, EnumFacing.WEST)));
		else
			setOutput(pos, parent, getInputFromSide(pos, parent, toExternal(pos, parent, EnumFacing.EAST)));
	}

	@Override
	protected boolean hasOutputToSide(Vec2 pos, ICircuit parent, EnumFacing fd) {
		return fd == EnumFacing.NORTH;
	}
}
