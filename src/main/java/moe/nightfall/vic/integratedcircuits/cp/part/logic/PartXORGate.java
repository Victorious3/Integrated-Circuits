package moe.nightfall.vic.integratedcircuits.cp.part.logic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.cp.part.PartSimpleGate;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraftforge.common.util.ForgeDirection;

public class PartXORGate extends PartSimpleGate {
	@Override
	public boolean canConnectToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		return toInternal(pos, parent, side) != ForgeDirection.SOUTH;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(9, 0);
	}

	@Override
	protected boolean hasOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection fd) {
		return fd == ForgeDirection.NORTH;
	}

	@Override
	protected void calcOutput(Vec2 pos, ICircuit parent) {
		setOutput(
				pos,
				parent,
				getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.EAST)) != getInputFromSide(pos,
						parent, toExternal(pos, parent, ForgeDirection.WEST)));
	}
}