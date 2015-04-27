package moe.nightfall.vic.integratedcircuits.ic.part.logic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.ic.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.ic.part.Part1I3O;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraftforge.common.util.ForgeDirection;

public class PartBufferGate extends Part1I3O {
	@Override
	public void calcOutput(Vec2 pos, ICircuit parent) {
		setOutput(pos, parent, getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH)));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(14, 0);
	}
}