package moe.nightfall.vic.integratedcircuits.cp.part.cell;

import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.cp.part.PartSimpleGate;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartBufferCell extends PartSimpleGate {
	@Override
	public Category getCategory() {
		return Category.CELL;
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		super.onInputChange(pos, parent, side);
		notifyNeighbours(pos, parent);
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		// A-la NullCell
		if (getInputFromSide(pos, parent, side.getOpposite()) && !getInputFromSide(pos, parent, side))
			return true;
		
		// But also works as BufferGate
		return super.getOutputToSide(pos, parent, side);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderPart(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		CircuitPartRenderer.renderPartCell(pos, parent, this, x, y, type);

		Vec2 textureOffset = getTextureOffset(pos, parent, x, y, type);
		CircuitPartRenderer.addQuad(x, y, textureOffset.x * 16, textureOffset.y * 16, 16, 16,  this.getRotation(pos, parent));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(6, 2);
	}

	@Override
	protected void calcOutput(Vec2 pos, ICircuit parent) {
		ForgeDirection east = toExternal(pos, parent, ForgeDirection.EAST);
		setOutput(pos, parent,
			getInputFromSide(pos, parent, east) || getInputFromSide(pos, parent, east.getOpposite()));
	}

	@Override
	protected boolean hasOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection fd) {
		return fd == ForgeDirection.NORTH || fd == ForgeDirection.SOUTH;
	}
}
