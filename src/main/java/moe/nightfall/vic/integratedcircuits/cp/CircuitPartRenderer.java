package moe.nightfall.vic.integratedcircuits.cp;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.client.Resources;
import moe.nightfall.vic.integratedcircuits.cp.part.PartCPGate;
import moe.nightfall.vic.integratedcircuits.cp.part.PartIOBit;
import moe.nightfall.vic.integratedcircuits.cp.part.PartNull;
import moe.nightfall.vic.integratedcircuits.cp.part.PartWire;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class CircuitPartRenderer {

	public enum EnumRenderType {
		GUI, WORLD, WORLD_16x
	}

	public static void renderPart(CircuitRenderWrapper crw, double x, double y) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.RESOURCE_PCB);
		Tessellator tes = Tessellator.instance;
		GL11.glTranslated(x, y, 0);
		tes.startDrawingQuads();
		renderPartPayload(crw.getPos(), crw, crw.getPart(), 0, 0, EnumRenderType.GUI);
		tes.draw();
		GL11.glTranslated(-x, -y, 0);
	}

	private static void renderPartPayload(Vec2 pos, ICircuit parent, CircuitPart part, double x, double y, EnumRenderType type) {
		if (type == EnumRenderType.WORLD_16x && !(part instanceof PartNull || part instanceof PartWire || part instanceof PartIOBit)) {
			Tessellator.instance.setColorRGBA_F(0, 0, 0, 1);
			addQuad(x, y, 0, 15 * 16, 16, 16);
		}

		part.renderPart(pos, parent, x, y, type);
	}

	@SideOnly(Side.CLIENT)
	public static int checkConnections(Vec2 pos, ICircuit parent, CircuitPart part) {
		boolean c1 = false;
		if (pos.y > 0) {
			CircuitPart n = part.getNeighbourOnSide(pos, parent, ForgeDirection.NORTH);
			c1 = part.canConnectToSide(pos, parent, ForgeDirection.NORTH)
					&& n.canConnectToSide(pos.offset(ForgeDirection.NORTH), parent, ForgeDirection.SOUTH)
					&& !(n instanceof PartNull);
		}

		boolean c2 = false;
		if (pos.y < parent.getCircuitData().getSize()) {
			CircuitPart n = part.getNeighbourOnSide(pos, parent, ForgeDirection.SOUTH);
			c2 = part.canConnectToSide(pos, parent, ForgeDirection.SOUTH)
					&& n.canConnectToSide(pos.offset(ForgeDirection.SOUTH), parent, ForgeDirection.NORTH)
					&& !(n instanceof PartNull);
		}

		boolean c3 = false;
		if (pos.x > 0) {
			CircuitPart n = part.getNeighbourOnSide(pos, parent, ForgeDirection.WEST);
			c3 = part.canConnectToSide(pos, parent, ForgeDirection.WEST)
					&& n.canConnectToSide(pos.offset(ForgeDirection.WEST), parent, ForgeDirection.EAST)
					&& !(n instanceof PartNull);
		}

		boolean c4 = false;
		if (pos.x < parent.getCircuitData().getSize()) {
			CircuitPart n = part.getNeighbourOnSide(pos, parent, ForgeDirection.EAST);
			c4 = part.canConnectToSide(pos, parent, ForgeDirection.EAST)
					&& n.canConnectToSide(pos.offset(ForgeDirection.EAST), parent, ForgeDirection.WEST)
					&& !(n instanceof PartNull);
		}

		return (c1 ? 1 : 0) << 3 | (c2 ? 1 : 0) << 2 | (c3 ? 1 : 0) << 1 | (c4 ? 1 : 0);
	}

	public static void addQuad(double x, double y, double u, double v, double w, double h) {
		addQuad(x, y, u, v, w, h, 0);
	}

	public static void addQuad(double x, double y, double u, double v, double w, double h, double rotation) {
		addQuad(x, y, u, v, w, h, w, h, 256, 256, rotation);
	}

	public static void addQuad(double x, double y, double u, double v, double w, double h, double w2, double h2,
			double tw, double th, double rotation) {
		double d1, d2, d3, d4;
		double scalew = 1 / tw;
		double scaleh = 1 / th;
		Tessellator tes = Tessellator.instance;

		d1 = u + 0;
		d2 = u + w2;

		if (rotation == 1) {
			d3 = v + h2;
			d4 = v + 0;

			tes.addVertexWithUV(x + w, y + h, 0, d2 * scalew, d4 * scaleh);
			tes.addVertexWithUV(x + w, y + 0, 0, d1 * scalew, d4 * scaleh);
			tes.addVertexWithUV(x + 0, y + 0, 0, d1 * scalew, d3 * scaleh);
			tes.addVertexWithUV(x + 0, y + h, 0, d2 * scalew, d3 * scaleh);
		} else if (rotation == 2) {
			d3 = v + h2;
			d4 = v + 0;

			tes.addVertexWithUV(x + 0, y + h, 0, d2 * scalew, d4 * scaleh);
			tes.addVertexWithUV(x + w, y + h, 0, d1 * scalew, d4 * scaleh);
			tes.addVertexWithUV(x + w, y + 0, 0, d1 * scalew, d3 * scaleh);
			tes.addVertexWithUV(x + 0, y + 0, 0, d2 * scalew, d3 * scaleh);
		} else if (rotation == 3) {
			d3 = v + 0;
			d4 = v + h2;

			tes.addVertexWithUV(x + w, y + h, 0, d1 * scalew, d4 * scaleh);
			tes.addVertexWithUV(x + w, y + 0, 0, d2 * scalew, d4 * scaleh);
			tes.addVertexWithUV(x + 0, y + 0, 0, d2 * scalew, d3 * scaleh);
			tes.addVertexWithUV(x + 0, y + h, 0, d1 * scalew, d3 * scaleh);
		} else {
			d3 = v + 0;
			d4 = v + h2;

			tes.addVertexWithUV(x + 0, y + h, 0, d1 * scalew, d4 * scaleh);
			tes.addVertexWithUV(x + w, y + h, 0, d2 * scalew, d4 * scaleh);
			tes.addVertexWithUV(x + w, y + 0, 0, d2 * scalew, d3 * scaleh);
			tes.addVertexWithUV(x + 0, y + 0, 0, d1 * scalew, d3 * scaleh);
		}
	}

	public static void renderParts(ICircuit circuit, double offX, double offY) {
		Tessellator tes = Tessellator.instance;
		int w = circuit.getCircuitData().getSize();

		GL11.glTranslated(offX, offY, 0);
		Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.RESOURCE_PCB);
		tes.startDrawingQuads();
		for (int x2 = 0; x2 < w; x2++) {
			for (int y2 = 0; y2 < w; y2++) {
				Vec2 pos = new Vec2(x2, y2);
				renderPartPayload(pos, circuit, circuit.getCircuitData().getPart(pos), x2 * 16, y2 * 16, EnumRenderType.GUI);
			}
		}
		tes.draw();
		GL11.glTranslated(-offX, -offY, 0);
	}

	public static void renderParts(ICircuit circuit, double offX, double offY, boolean[][] exc, EnumRenderType type) {
		Tessellator tes = Tessellator.instance;
		int w = circuit.getCircuitData().getSize();

		GL11.glTranslated(offX, offY, 0);
		Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.RESOURCE_PCB);
		tes.startDrawingQuads();
		for (int x2 = 0; x2 < w; x2++) {
			for (int y2 = 0; y2 < w; y2++) {
				Vec2 pos = new Vec2(x2, y2);
				if (exc[x2][y2])
					renderPartPayload(pos, circuit, circuit.getCircuitData().getPart(pos), x2 * 16, y2 * 16, type);
			}
		}
		tes.draw();
		GL11.glTranslated(-offX, -offY, 0);
	}

	public static void renderPerfboard(double offX, double offY, CircuitData data) {
		Tessellator tes = Tessellator.instance;
		int w = data.getSize();

		GL11.glTranslated(offX, offY, 0);
		Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.RESOURCE_PCB_PERF1);
		tes.startDrawingQuads();
		tes.setColorRGBA_F(1F, 1F, 1F, 1F);
		addQuad(0, 0, 0, 0, data.getSize() * 16, data.getSize() * 16, 16, 16, 16D / data.getSize(),
				16D / data.getSize(), 0);
		tes.draw();

		Minecraft.getMinecraft().getTextureManager().bindTexture(Resources.RESOURCE_PCB_PERF2);
		tes.startDrawingQuads();
		tes.setColorRGBA_F(1F, 1F, 1F, 1F);
		addQuad(0, 0, 0, 0, 16, data.getSize() * 16, 16, 16, 16D, 16D / data.getSize(), 0);
		addQuad(data.getSize() * 16 - 16, 0, 0, 0, 16, data.getSize() * 16, 16, 16, 16, 16D / data.getSize(), 0);
		addQuad(0, 0, 0, 0, data.getSize() * 16, 16, 16, 16, 16D / data.getSize(), 16, 0);
		addQuad(0, data.getSize() * 16 - 16, 0, 0, data.getSize() * 16, 16, 16, 16, 16D / data.getSize(), 16, 0);
		tes.draw();
		GL11.glTranslated(-offX, -offY, 0);
	}

	public static void renderPartGate(Vec2 pos, ICircuit parent, PartCPGate gate, double x, double y, EnumRenderType type) {
		Tessellator tes = Tessellator.instance;
		if (gate.canConnectToSide(pos, parent, ForgeDirection.NORTH)) {
			if (type == EnumRenderType.GUI
					&& (gate.getNeighbourOnSide(pos, parent, ForgeDirection.NORTH).getInputFromSide(
					pos.offset(ForgeDirection.NORTH), parent, ForgeDirection.SOUTH) || gate.getInputFromSide(
					pos, parent, ForgeDirection.NORTH)))
				tes.setColorRGBA_F(0F, 1F, 0F, 1F);
			else
				tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
			addQuad(x, y, 2 * 16, 0, 16, 16);
		}

		if (gate.canConnectToSide(pos, parent, ForgeDirection.SOUTH)) {
			if (type == EnumRenderType.GUI
					&& (gate.getNeighbourOnSide(pos, parent, ForgeDirection.SOUTH).getInputFromSide(
					pos.offset(ForgeDirection.SOUTH), parent, ForgeDirection.NORTH) || gate.getInputFromSide(
					pos, parent, ForgeDirection.SOUTH)))
				tes.setColorRGBA_F(0F, 1F, 0F, 1F);
			else
				tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
			addQuad(x, y, 4 * 16, 0, 16, 16);
		}

		if (gate.canConnectToSide(pos, parent, ForgeDirection.WEST)) {
			if (type == EnumRenderType.GUI
					&& (gate.getNeighbourOnSide(pos, parent, ForgeDirection.WEST).getInputFromSide(
					pos.offset(ForgeDirection.WEST), parent, ForgeDirection.EAST) || gate.getInputFromSide(pos,
					parent, ForgeDirection.WEST)))
				tes.setColorRGBA_F(0F, 1F, 0F, 1F);
			else
				tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
			addQuad(x, y, 1 * 16, 0, 16, 16);
		}

		if (gate.canConnectToSide(pos, parent, ForgeDirection.EAST)) {
			if (type == EnumRenderType.GUI
					&& (gate.getNeighbourOnSide(pos, parent, ForgeDirection.EAST).getInputFromSide(
					pos.offset(ForgeDirection.EAST), parent, ForgeDirection.WEST) || gate.getInputFromSide(pos,
					parent, ForgeDirection.EAST)))
				tes.setColorRGBA_F(0F, 1F, 0F, 1F);
			else
				tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
			addQuad(x, y, 3 * 16, 0, 16, 16);
		}

		tes.setColorRGBA_F(0F, 1F, 0F, 1F);
	}

	@SideOnly(Side.CLIENT)
	public static void renderPartCell(Vec2 pos, ICircuit parent, CircuitPart cell, double x, double y, EnumRenderType type) {
		Tessellator tes = Tessellator.instance;

		int rotation = 0;
		if(cell instanceof PartCPGate)
			rotation = ((PartCPGate) cell).getRotation(pos, parent);

		if (type == EnumRenderType.GUI
				&& (cell.getOutputToSide(pos, parent, MiscUtils.rotn(ForgeDirection.NORTH, rotation))
				|| cell.getInputFromSide(pos, parent, MiscUtils.rotn(ForgeDirection.NORTH, rotation))
				|| cell.getOutputToSide(pos, parent, MiscUtils.rotn(ForgeDirection.SOUTH, rotation)) || cell
				.getInputFromSide(pos, parent, MiscUtils.rotn(ForgeDirection.SOUTH, rotation))))
			tes.setColorRGBA_F(0F, 1F, 0F, 1F);
		else
			tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
		addQuad(x, y, 0, 2 * 16, 16, 16, rotation);

		if (type == EnumRenderType.GUI
				&& (cell.getOutputToSide(pos, parent, MiscUtils.rotn(ForgeDirection.EAST, rotation))
				|| cell.getInputFromSide(pos, parent, MiscUtils.rotn(ForgeDirection.EAST, rotation))
				|| cell.getOutputToSide(pos, parent, MiscUtils.rotn(ForgeDirection.WEST, rotation)) || cell
				.getInputFromSide(pos, parent, MiscUtils.rotn(ForgeDirection.WEST, rotation))))
			tes.setColorRGBA_F(0F, 1F, 0F, 1F);
		else
			tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
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

		@Override
		public void setCircuitData(CircuitData data) {
		}

		@Override
		public boolean getInputFromSide(ForgeDirection dir, int frequency) {
			return false;
		}

		@Override
		public void setOutputToSide(ForgeDirection dir, int frequency, boolean output) {
		}
	}
}