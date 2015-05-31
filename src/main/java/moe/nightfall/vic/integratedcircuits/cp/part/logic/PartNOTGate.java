package moe.nightfall.vic.integratedcircuits.cp.part.logic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.cp.part.Part1I3O;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraftforge.common.util.ForgeDirection;

public class PartNOTGate extends Part1I3O {
	@Override
	public Category getCategory() {
		return Category.NGATE;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(15, 0);
	}

	@Override
	public void calcOutput(Vec2 pos, ICircuit parent) {
		setOutput(pos, parent, !getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH)));
	}
}
