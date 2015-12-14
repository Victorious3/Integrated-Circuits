package moe.nightfall.vic.integratedcircuits.cp.part;

import moe.nightfall.vic.integratedcircuits.Config;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPart;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import moe.nightfall.vic.integratedcircuits.misc.RenderUtils;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class PartIOBit extends CircuitPart {
	public final IntProperty PROP_ROTATION = new IntProperty("ROTATION", stitcher, 3);
	public final IntProperty PROP_FREQUENCY = new IntProperty("FREQUENCY", stitcher, 15);

	@SideOnly(Side.CLIENT)
	@Override
	public void renderPart(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		int freq = this.getFrequency(pos, parent);
		EnumFacing rot = this.getRotation(pos, parent);
		Tessellator tes = Tessellator.getInstance();
		WorldRenderer wr = tes.getWorldRenderer();

		if (type == CircuitPartRenderer.EnumRenderType.WORLD_16x) {
			wr.color(188, 167, 60, 255);
			CircuitPartRenderer.addQuad(x, y, 6 * 16, 3 * 16, 16, 16, rot);
		} else {
			wr.color(255, 255, 255, 255);
			CircuitPartRenderer.addQuad(x, y, 2 * 16, 2 * 16, 16, 16, rot);
			if (this.isPowered(pos, parent) && type == CircuitPartRenderer.EnumRenderType.GUI)
				RenderUtils.applyColorIRGBA(tes, Config.colorGreen);
			else
				RenderUtils.applyColorIRGBA(tes, Config.colorGreen, 0.4F);
			CircuitPartRenderer.addQuad(x, y, 4 * 16, 2 * 16, 16, 16, rot);
			if (type == CircuitPartRenderer.EnumRenderType.GUI) {
				if (parent.getCircuitData().getProperties().getModeAtSide(getRotation(pos, parent)).isAnalog())
					wr.color(getFrequency(pos, parent) * 17, 0, 0, 255);
				else {
					int color = EnumDyeColor.byMetadata(freq).getMapColor().colorValue;
					wr.color(color >> 8, color >> 4 & 0xFF, color & 0xFF, 255);
				}
				CircuitPartRenderer.addQuad(x, y, 3 * 16, 2 * 16, 16, 16, rot);
			}
		}
	}

	public final EnumFacing getRotation(Vec2 pos, ICircuit parent) {
		return EnumFacing.getHorizontal(getProperty(pos, parent, PROP_ROTATION));
	}

	public final void setRotation(Vec2 pos, ICircuit parent, EnumFacing rotation) {
		setProperty(pos, parent, PROP_ROTATION, rotation.getHorizontalIndex());
	}

	public final int getFrequency(Vec2 pos, ICircuit parent) {
		return getProperty(pos, parent, PROP_FREQUENCY);
	}

	public final void setFrequency(Vec2 pos, ICircuit parent, int frequency) {
		setProperty(pos, parent, PROP_FREQUENCY, frequency);
	}

	public final void updateExternalOutput(Vec2 pos, ICircuit parent) {
		EnumFacing dir = getRotation(pos, parent);
		parent.setOutputToSide(dir, getFrequency(pos, parent),
				getInputFromSide(pos, parent, dir.getOpposite()));
	}

	@Override
	public boolean canConnectToSide(Vec2 pos, ICircuit parent, EnumFacing side) {
		EnumFacing dir = getRotation(pos, parent);
		return side == dir.getOpposite();
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent) {
		scheduleTick(pos, parent);
		notifyNeighbours(pos, parent);
	}

	@Override
	public void onScheduledTick(Vec2 pos, ICircuit parent) {
		updateExternalOutput(pos, parent);
		notifyNeighbours(pos, parent); // Implicit updateExternalInput
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, EnumFacing side) {
		EnumFacing dir = getRotation(pos, parent);
		if (side == dir.getOpposite())
			return parent.getInputFromSide(dir, getFrequency(pos, parent));
		else
			return false;
	}

	public boolean isPowered(Vec2 pos, ICircuit parent) {
		EnumFacing dir = getRotation(pos, parent).getOpposite();
		return getOutputToSide(pos, parent, dir)
				|| getNeighbourOnSide(pos, parent, dir).getOutputToSide(pos.offset(dir), parent, dir.getOpposite());
	}
}
