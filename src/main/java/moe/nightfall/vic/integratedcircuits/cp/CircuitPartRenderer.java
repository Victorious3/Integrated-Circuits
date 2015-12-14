package moe.nightfall.vic.integratedcircuits.cp;

import org.lwjgl.opengl.GL11;

import moe.nightfall.vic.integratedcircuits.Config;
import moe.nightfall.vic.integratedcircuits.client.Resources;
import moe.nightfall.vic.integratedcircuits.cp.part.PartCPGate;
import moe.nightfall.vic.integratedcircuits.cp.part.PartIOBit;
import moe.nightfall.vic.integratedcircuits.cp.part.PartNull;
import moe.nightfall.vic.integratedcircuits.cp.part.PartWire;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.misc.RenderUtils;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class CircuitPartRenderer {

	public static final int PART_SIZE = 16;
	
	public enum EnumRenderType {
		GUI, WORLD, WORLD_16x
	}

	public static void renderPart(CircuitRenderWrapper crw, double x, double y) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.RESOURCE_PCB);
		Tessellator tes = Tessellator.getInstance();
		WorldRenderer wr = tes.getWorldRenderer();
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		GL11.glTranslated(x, y, 0);
		renderPartPayload(crw.getPos(), crw, crw.getPart(), 0, 0, EnumRenderType.GUI);
		tes.draw();
		GL11.glTranslated(-x, -y, 0);
	}

	private static void renderPartPayload(Vec2 pos, ICircuit parent, CircuitPart part, double x, double y, EnumRenderType type) {
		if (type == EnumRenderType.WORLD_16x && !(part instanceof PartNull || part instanceof PartWire || part instanceof PartIOBit)) {
			Tessellator tes = Tessellator.getInstance();
			WorldRenderer wr = tes.getWorldRenderer();
			wr.putColorRGB_F4(0, 0, 0);
			addQuad(x, y, 0, 15 * 16, PART_SIZE, PART_SIZE);
		}

		part.renderPart(pos, parent, x, y, type);
	}

	@SideOnly(Side.CLIENT)
	public static int checkConnections(Vec2 pos, ICircuit parent, CircuitPart part) {
		boolean c1 = part.hasConnectionOnSide(pos, parent, EnumFacing.NORTH);
		boolean c2 = part.hasConnectionOnSide(pos, parent, EnumFacing.SOUTH);
		boolean c3 = part.hasConnectionOnSide(pos, parent, EnumFacing.WEST);
		boolean c4 = part.hasConnectionOnSide(pos, parent, EnumFacing.EAST);

		return (c1 ? 1 : 0) << 3 | (c2 ? 1 : 0) << 2 | (c3 ? 1 : 0) << 1 | (c4 ? 1 : 0);
	}

	public static void addQuad(double x, double y, double u, double v, double w, double h) {
		addQuad(x, y, u, v, w, h, EnumFacing.NORTH);
	}

	public static void addQuad(double x, double y, double u, double v, double w, double h, EnumFacing rotation) {
		addQuad(x, y, u, v, w, h, w, h, 256, 256, rotation);
	}

	//TODO No idea what magic numbers we have now, but be prepared for bugs...
	public static void addQuad(double x, double y, double u, double v, double w, double h, double w2, double h2, double tw, double th, EnumFacing rotation) {
		double d1, d2, d3, d4;
		double scalew = 1 / tw;
		double scaleh = 1 / th;
		Tessellator tes = Tessellator.getInstance();
		WorldRenderer wr = tes.getWorldRenderer();

		d1 = u + 0;
		d2 = u + w2;

		if (rotation == EnumFacing.EAST) {
			d3 = v + h2;
			d4 = v + 0;

			wr.pos(x + w, y + h, 0).tex(d2 * scalew, d4 * scaleh);
			wr.pos(x + w, y + 0, 0).tex(d1 * scalew, d4 * scaleh);
			wr.pos(x + 0, y + 0, 0).tex(d1 * scalew, d3 * scaleh);
			wr.pos(x + 0, y + h, 0).tex(d2 * scalew, d3 * scaleh);
		} else if (rotation == EnumFacing.SOUTH) {
			d3 = v + h2;
			d4 = v + 0;

			wr.pos(x + 0, y + h, 0).tex(d2 * scalew, d4 * scaleh);
			wr.pos(x + w, y + h, 0).tex(d1 * scalew, d4 * scaleh);
			wr.pos(x + w, y + 0, 0).tex(d1 * scalew, d3 * scaleh);
			wr.pos(x + 0, y + 0, 0).tex(d2 * scalew, d3 * scaleh);
		} else if (rotation == EnumFacing.WEST) {
			d3 = v + 0;
			d4 = v + h2;

			wr.pos(x + w, y + h, 0).tex(d1 * scalew, d4 * scaleh);
			wr.pos(x + w, y + 0, 0).tex(d2 * scalew, d4 * scaleh);
			wr.pos(x + 0, y + 0, 0).tex(d2 * scalew, d3 * scaleh);
			wr.pos(x + 0, y + h, 0).tex(d1 * scalew, d3 * scaleh);
		} else {
			d3 = v + 0;
			d4 = v + h2;

			wr.pos(x + 0, y + h, 0).tex(d1 * scalew, d4 * scaleh);
			wr.pos(x + w, y + h, 0).tex(d2 * scalew, d4 * scaleh);
			wr.pos(x + w, y + 0, 0).tex(d2 * scalew, d3 * scaleh);
			wr.pos(x + 0, y + 0, 0).tex(d1 * scalew, d3 * scaleh);
		}
	}

	public static void renderParts(ICircuit circuit) {
		Tessellator tes = Tessellator.getInstance();
		WorldRenderer wr = tes.getWorldRenderer();
		int w = circuit.getCircuitData().getSize();

		Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.RESOURCE_PCB);
		GL11.glPushMatrix();
		GL11.glScalef(1F / PART_SIZE, 1F / PART_SIZE, 1);
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		for (int x2 = 0; x2 < w; x2++) {
			for (int y2 = 0; y2 < w; y2++) {
				Vec2 pos = new Vec2(x2, y2);
				renderPartPayload(pos, circuit, circuit.getCircuitData().getPart(pos), x2 * PART_SIZE, y2 * PART_SIZE, EnumRenderType.GUI);
			}
		}
		tes.draw();
		GL11.glPopMatrix();
	}

	public static void renderParts(ICircuit circuit, double offX, double offY, boolean[][] exc, EnumRenderType type) {
		Tessellator tes = Tessellator.getInstance();
		WorldRenderer wr = tes.getWorldRenderer();
		int w = circuit.getCircuitData().getSize();

		GL11.glPushMatrix();
		GL11.glTranslated(offX, offY, 0);
		if (type == EnumRenderType.GUI)
			GL11.glScalef(1F / PART_SIZE, 1F / PART_SIZE, 1);
		Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.RESOURCE_PCB);
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		for (int x2 = 0; x2 < w; x2++) {
			for (int y2 = 0; y2 < w; y2++) {
				Vec2 pos = new Vec2(x2, y2);
				if (exc[x2][y2])
					renderPartPayload(pos, circuit, circuit.getCircuitData().getPart(pos), x2 * PART_SIZE, y2 * PART_SIZE, type);
			}
		}
		tes.draw();
		GL11.glPopMatrix();
	}

	public static void renderPerfboard(CircuitData data) {
		Tessellator tes = Tessellator.getInstance();
		WorldRenderer wr = tes.getWorldRenderer();
		int size = data.getSize();

		Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.RESOURCE_PCB_PERF1);
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		wr.putColorRGB_F4(1, 1, 1);
		addQuad(0, 0, 0, 0, size, size, 16, 16, 16D / size, 16D / size, EnumFacing.NORTH);
		tes.draw();

		Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.RESOURCE_PCB_PERF2);
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		wr.putColorRGB_F4(1, 1, 1);
		addQuad(0, 0, 0, 0, 1, size, 16, 16, 16D, 16D / size, EnumFacing.NORTH);
		addQuad(size - 1, 0, 0, 0, 1, size, 16, 16, 16, 16D / size, EnumFacing.NORTH);
		addQuad(0, 0, 0, 0, size, 1, 16, 16, 16D / size, 16, EnumFacing.NORTH);
		addQuad(0, size - 1, 0, 0, size, 1, 16, 16, 16D / size, 16, EnumFacing.NORTH);
		tes.draw();
	}

	public static void renderPartGate(Vec2 pos, ICircuit parent, PartCPGate gate, double x, double y, EnumRenderType type) {
		Tessellator tes = Tessellator.getInstance();
		WorldRenderer wr = tes.getWorldRenderer();
		if (gate.canConnectToSide(pos, parent, EnumFacing.NORTH)) {
			if (type == EnumRenderType.GUI && (
					gate.getOutputToSide(pos, parent, EnumFacing.NORTH)
					|| gate.getInputFromSide(pos, parent, EnumFacing.NORTH)))
				RenderUtils.applyColorIRGBA(tes, Config.colorGreen);
			else
				RenderUtils.applyColorIRGBA(tes, Config.colorGreen, 0.4F);
			addQuad(x, y, 2 * 16, 0, PART_SIZE, PART_SIZE);
		}

		if (gate.canConnectToSide(pos, parent, EnumFacing.SOUTH)) {
			if (type == EnumRenderType.GUI && (
					gate.getOutputToSide(pos, parent, EnumFacing.SOUTH)
					|| gate.getInputFromSide(pos, parent, EnumFacing.SOUTH)))
				RenderUtils.applyColorIRGBA(tes, Config.colorGreen);
			else
				RenderUtils.applyColorIRGBA(tes, Config.colorGreen, 0.4F);
			addQuad(x, y, 4 * 16, 0, PART_SIZE, PART_SIZE);
		}

		if (gate.canConnectToSide(pos, parent, EnumFacing.WEST)) {
			if (type == EnumRenderType.GUI && (
					gate.getOutputToSide(pos, parent, EnumFacing.WEST)
					|| gate.getInputFromSide(pos, parent, EnumFacing.WEST)))
				RenderUtils.applyColorIRGBA(tes, Config.colorGreen);
			else
				RenderUtils.applyColorIRGBA(tes, Config.colorGreen, 0.4F);
			addQuad(x, y, 1 * 16, 0, PART_SIZE, PART_SIZE);
		}

		if (gate.canConnectToSide(pos, parent, EnumFacing.EAST)) {
			if (type == EnumRenderType.GUI && (
					gate.getOutputToSide(pos, parent, EnumFacing.EAST)
					|| gate.getInputFromSide(pos, parent, EnumFacing.EAST)))
				RenderUtils.applyColorIRGBA(tes, Config.colorGreen);
			else
				RenderUtils.applyColorIRGBA(tes, Config.colorGreen, 0.4F);
			addQuad(x, y, 3 * 16, 0, PART_SIZE, PART_SIZE);
		}

		RenderUtils.applyColorIRGBA(tes, Config.colorGreen);
	}

	@SideOnly(Side.CLIENT)
	public static void renderPartCell(Vec2 pos, ICircuit parent, CircuitPart cell, double x, double y, EnumRenderType type) {
		Tessellator tes = Tessellator.getInstance();
		WorldRenderer wr = tes.getWorldRenderer();

		EnumFacing rotation = EnumFacing.NORTH;
		if(cell instanceof PartCPGate)
			rotation = ((PartCPGate) cell).getRotation(pos, parent);

		if (type == EnumRenderType.GUI
				&& (cell.getOutputToSide(pos, parent, MiscUtils.rot(EnumFacing.NORTH, rotation))
				|| cell.getInputFromSide(pos, parent, MiscUtils.rot(EnumFacing.NORTH, rotation))
				|| cell.getOutputToSide(pos, parent, MiscUtils.rot(EnumFacing.SOUTH, rotation))
				|| cell.getInputFromSide(pos, parent, MiscUtils.rot(EnumFacing.SOUTH, rotation))))
			RenderUtils.applyColorIRGBA(tes, Config.colorGreen);
		else
			RenderUtils.applyColorIRGBA(tes, Config.colorGreen, 0.4F);
		addQuad(x, y, 0, 2 * 16, PART_SIZE, PART_SIZE, rotation);

		if (type == EnumRenderType.GUI
				&& (cell.getOutputToSide(pos, parent, MiscUtils.rot(EnumFacing.EAST, rotation))
				|| cell.getInputFromSide(pos, parent, MiscUtils.rot(EnumFacing.EAST, rotation))
				|| cell.getOutputToSide(pos, parent, MiscUtils.rot(EnumFacing.WEST, rotation))
				|| cell.getInputFromSide(pos, parent, MiscUtils.rot(EnumFacing.WEST, rotation))))
			RenderUtils.applyColorIRGBA(tes, Config.colorGreen);
		else
			RenderUtils.applyColorIRGBA(tes, Config.colorGreen, 0.4F);
	}

	public static class CircuitRenderWrapper implements ICircuit {
		private final CircuitData data;
		private final CircuitPart part;
		private final Vec2 pos;

		public CircuitRenderWrapper(Class<? extends CircuitPart> clazz) {
			this(clazz, 0);
		}

		public CircuitRenderWrapper(Class<? extends CircuitPart> clazz, int state) {
			this(state, CircuitPart.getPart(clazz));
		}

		public CircuitRenderWrapper(int state, CircuitPart part) {
			this.data = CircuitData.createShallowInstance(state, this);
			this.part = part;
			this.pos = new Vec2(1, 1);
		}

		public CircuitRenderWrapper(CircuitData data) {
			this(data, null, null);
		}

		public CircuitRenderWrapper(CircuitData data, CircuitPart part, Vec2 pos) {
			this.data = data;
			this.part = part;
			this.pos = pos;
		}

		public CircuitPart getPart() {
			return part;
		}

		@Override
		public CircuitData getCircuitData() {
			return data;
		}

		public Vec2 getPos() {
			return pos;
		}

		public int getState() {
			return getCircuitData().getMeta(getPos());
		}

		public void setState(int state) {
			getCircuitData().setMeta(getPos(), state);
		}

		@Override
		public void setCircuitData(CircuitData data) {
		}

		@Override
		public boolean getInputFromSide(EnumFacing dir, int frequency) {
			return false;
		}

		@Override
		public void setOutputToSide(EnumFacing dir, int frequency, boolean output) {
		}
	}
}
