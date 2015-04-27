package moe.nightfall.vic.integratedcircuits.ic.part.logic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.ic.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.ic.part.Part3I1O;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraftforge.common.util.ForgeDirection;

public class PartANDGate extends Part3I1O {
	@Override
	protected void calcOutput(Vec2 pos, ICircuit parent) {
		ForgeDirection s3 = toExternal(pos, parent, ForgeDirection.SOUTH);
		ForgeDirection s4 = toExternal(pos, parent, ForgeDirection.EAST);
		ForgeDirection s5 = s4.getOpposite();

		setOutput(
				pos,
				parent,
				(!canConnectToSide(pos, parent, s3) || getInputFromSide(pos, parent, s3))
						&& (!canConnectToSide(pos, parent, s4) || getInputFromSide(pos, parent, s4))
						&& (!canConnectToSide(pos, parent, s5) || getInputFromSide(pos, parent, s5)));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(7, 0);
	}
}