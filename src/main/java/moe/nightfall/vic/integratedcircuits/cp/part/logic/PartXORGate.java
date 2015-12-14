package moe.nightfall.vic.integratedcircuits.cp.part.logic;

import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.cp.part.Part2I1O;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class PartXORGate extends Part2I1O {
	@Override
	public void calcOutput(Vec2 pos, ICircuit parent) {
		EnumFacing s3 = toExternal(pos, parent, EnumFacing.SOUTH);
		EnumFacing s4 = toExternal(pos, parent, EnumFacing.EAST);
		EnumFacing s5 = s4.getOpposite();

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
