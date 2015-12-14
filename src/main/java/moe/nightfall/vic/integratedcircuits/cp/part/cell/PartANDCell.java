package moe.nightfall.vic.integratedcircuits.cp.part.cell;

import moe.nightfall.vic.integratedcircuits.Config;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.cp.part.PartSimpleGate;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.misc.RenderUtils;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class PartANDCell extends PartSimpleGate {
	@Override
	public Category getCategory() {
		return Category.CELL;
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent) {
		super.onInputChange(pos, parent);
		notifyNeighbours(pos, parent);
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, EnumFacing side) {
		EnumFacing fd = toInternal(pos, parent, side);
		// A-la NullCell (only NORTH<=>SOUTH)
		if ((fd == EnumFacing.NORTH || fd == EnumFacing.SOUTH) &&
				getInputFromSide(pos, parent, side.getOpposite()) && !getInputFromSide(pos, parent, side))
			return true;
		
		// Act as AND gate
		return super.getOutputToSide(pos, parent, side);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderPart(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		Tessellator tes = Tessellator.getInstance();
		EnumFacing rotation = this.getRotation(pos, parent);

		EnumFacing fd = MiscUtils.rot(EnumFacing.NORTH, rotation);
		if (type == CircuitPartRenderer.EnumRenderType.GUI
				&& (this.getOutputToSide(pos, parent, fd) || this.getInputFromSide(pos, parent, fd)
				|| this.getOutputToSide(pos, parent, fd.getOpposite()) || this.getInputFromSide(pos, parent,
				fd.getOpposite())))
			RenderUtils.applyColorIRGBA(tes, Config.colorGreen);
		else
			RenderUtils.applyColorIRGBA(tes, Config.colorGreen, 0.4F);
		CircuitPartRenderer.addQuad(x, y, 0, 2 * 16, 16, 16, rotation);

		fd = MiscUtils.rot(EnumFacing.EAST, rotation);
		if (type == CircuitPartRenderer.EnumRenderType.GUI
				&& (this.getNeighbourOnSide(pos, parent, fd).getInputFromSide(pos.offset(fd), parent, fd.getOpposite()) || this
				.getInputFromSide(pos, parent, fd)))
			RenderUtils.applyColorIRGBA(tes, Config.colorGreen);
		else
			RenderUtils.applyColorIRGBA(tes, Config.colorGreen, 0.4F);
		CircuitPartRenderer.addQuad(x, y, 8 * 16, 2 * 16, 16, 16, rotation);

		fd = MiscUtils.rot(EnumFacing.WEST, rotation);
		if (type == CircuitPartRenderer.EnumRenderType.GUI
				&& (this.getNeighbourOnSide(pos, parent, fd).getInputFromSide(pos.offset(fd), parent, fd.getOpposite()) || this
				.getInputFromSide(pos, parent, fd)))
			RenderUtils.applyColorIRGBA(tes, Config.colorGreen);
		else
			RenderUtils.applyColorIRGBA(tes, Config.colorGreen, 0.4F);

		Vec2 textureOffset = getTextureOffset(pos, parent, x, y, type);
		CircuitPartRenderer.addQuad(x, y, textureOffset.x * 16, textureOffset.y * 16, 16, 16, rotation);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(7, 2);
	}

	@Override
	protected void calcOutput(Vec2 pos, ICircuit parent) {
		EnumFacing f1 = toExternal(pos, parent, EnumFacing.NORTH);
		EnumFacing f2 = f1.getOpposite();
		EnumFacing f3 = toExternal(pos, parent, EnumFacing.EAST);
		setOutput(pos, parent, (getInputFromSide(pos, parent, f1) || getInputFromSide(pos, parent, f2))
				&& getInputFromSide(pos, parent, f3));
	}

	@Override
	protected boolean hasOutputToSide(Vec2 pos, ICircuit parent, EnumFacing fd) {
		return fd == EnumFacing.WEST;
	}
}
