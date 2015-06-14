package moe.nightfall.vic.integratedcircuits.cp.part.logic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.cp.part.Part2I1O;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraftforge.common.util.ForgeDirection;

public class PartXORGate extends Part2I1O {
	@Override
	public void calcOutput(Vec2 pos, ICircuit parent) {
		ForgeDirection s3 = toExternal(pos, parent, ForgeDirection.SOUTH);
		ForgeDirection s4 = toExternal(pos, parent, ForgeDirection.EAST);
		ForgeDirection s5 = s4.getOpposite();

		// Xor works properly for booleans
		setOutput(pos, parent, getInputFromSide(pos, parent, s3)
				^ getInputFromSide(pos, parent, s4)
				^ getInputFromSide(pos, parent, s5));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(9, 0);
	}
}
