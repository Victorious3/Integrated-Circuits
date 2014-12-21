package vic.mod.integratedcircuits.ic;

import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.ic.part.PartCPGate;
import vic.mod.integratedcircuits.ic.part.PartIOBit;
import vic.mod.integratedcircuits.ic.part.PartMultiplexer;
import vic.mod.integratedcircuits.ic.part.PartNull;
import vic.mod.integratedcircuits.ic.part.PartSynchronizer;
import vic.mod.integratedcircuits.ic.part.PartTorch;
import vic.mod.integratedcircuits.ic.part.PartWire;
import vic.mod.integratedcircuits.ic.part.cell.PartANDCell;
import vic.mod.integratedcircuits.ic.part.cell.PartBufferCell;
import vic.mod.integratedcircuits.ic.part.cell.PartInvertCell;
import vic.mod.integratedcircuits.ic.part.cell.PartNullCell;
import vic.mod.integratedcircuits.ic.part.latch.PartRSLatch;
import vic.mod.integratedcircuits.ic.part.latch.PartToggleLatch;
import vic.mod.integratedcircuits.ic.part.latch.PartTransparentLatch;
import vic.mod.integratedcircuits.ic.part.logic.PartANDGate;
import vic.mod.integratedcircuits.ic.part.logic.PartBufferGate;
import vic.mod.integratedcircuits.ic.part.logic.PartNANDGate;
import vic.mod.integratedcircuits.ic.part.logic.PartNORGate;
import vic.mod.integratedcircuits.ic.part.logic.PartNOTGate;
import vic.mod.integratedcircuits.ic.part.logic.PartORGate;
import vic.mod.integratedcircuits.ic.part.logic.PartXNORGate;
import vic.mod.integratedcircuits.ic.part.logic.PartXORGate;
import vic.mod.integratedcircuits.ic.part.timed.PartPulseFormer;
import vic.mod.integratedcircuits.ic.part.timed.PartRandomizer;
import vic.mod.integratedcircuits.ic.part.timed.PartRepeater;
import vic.mod.integratedcircuits.ic.part.timed.PartSequencer;
import vic.mod.integratedcircuits.ic.part.timed.PartStateCell;
import vic.mod.integratedcircuits.ic.part.timed.PartTimer;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.misc.Vec2;

public class CircuitPartRenderer 
{
	public static ResourceLocation partResource = new ResourceLocation(IntegratedCircuits.modID, "textures/gui/sublogicpart.png");
	public static ResourceLocation partBG1 = new ResourceLocation(IntegratedCircuits.modID, "textures/gui/bg1.png");
	public static ResourceLocation partBG2 = new ResourceLocation(IntegratedCircuits.modID, "textures/gui/bg2.png");
	
	public static void renderPart(CircuitRenderWrapper crw, double x, double y)
	{
		Minecraft.getMinecraft().getTextureManager().bindTexture(partResource);
		Tessellator tes = Tessellator.instance;
		GL11.glTranslated(x, y, 0);
		tes.startDrawingQuads();
		renderPartPayload(crw.getPos(), crw, crw.getPart(), 0, 0, 0);
		tes.draw();
		GL11.glTranslated(-x, -y, 0);
	}
	
	private static void renderPartPayload(Vec2 pos, ICircuit parent, CircuitPart part, double x, double y, int type)
	{
		if(part instanceof PartWire) renderPartWire(pos, parent, (PartWire)part, x, y, type);
		else if(part instanceof PartIOBit) renderPartIOBit(pos, parent, (PartIOBit)part, x, y, type);
		else if(type == 2 && !(part instanceof PartNull)) 
		{
			Tessellator.instance.setColorRGBA_F(0, 0, 0, 1);
			addQuad(x, y, 0, 15 * 16, 16, 16);
		}
		else if(part instanceof PartNullCell || part instanceof PartInvertCell || part instanceof PartBufferCell) renderPartCell(pos, parent, part, x, y, type);
		else if(part instanceof PartANDCell) renderPartANDCell(pos, parent, (PartANDCell)part, x, y, type);
		else if(part instanceof PartCPGate) renderPartGate(pos, parent, (PartCPGate)part, x, y, type);
		else if(part instanceof PartTorch) renderPartTorch(pos, parent, (PartTorch)part, x, y, type);	
	}

	private static int checkConnections(Vec2 pos, ICircuit parent, CircuitPart part)
	{
		boolean c1 = false;
		if(pos.y > 0) 
		{
			CircuitPart n = part.getNeighbourOnSide(pos, parent, ForgeDirection.NORTH);
			c1 = part.canConnectToSide(pos, parent, ForgeDirection.NORTH) 
				&& n.canConnectToSide(pos.offset(ForgeDirection.NORTH), parent, ForgeDirection.SOUTH) 
				&& !(n instanceof PartNull);
		}
		
		boolean c2 = false; 
		if(pos.y < parent.getCircuitData().getSize())
		{
			CircuitPart n = part.getNeighbourOnSide(pos, parent, ForgeDirection.SOUTH);
			c2 = part.canConnectToSide(pos, parent, ForgeDirection.SOUTH) 
				&& n.canConnectToSide(pos.offset(ForgeDirection.SOUTH), parent, ForgeDirection.NORTH) 
				&& !(n instanceof PartNull);
		}
		
		boolean c3 = false; 
		if(pos.x > 0)
		{
			CircuitPart n = part.getNeighbourOnSide(pos, parent, ForgeDirection.WEST);
			c3 = part.canConnectToSide(pos, parent, ForgeDirection.WEST) 
				&& n.canConnectToSide(pos.offset(ForgeDirection.WEST), parent, ForgeDirection.EAST) 
				&& !(n instanceof PartNull);
		}
		
		boolean c4 = false; 
		if(pos.x < parent.getCircuitData().getSize())
		{
			CircuitPart n = part.getNeighbourOnSide(pos, parent, ForgeDirection.EAST);
			c4 = part.canConnectToSide(pos, parent, ForgeDirection.EAST) 
				&& n.canConnectToSide(pos.offset(ForgeDirection.EAST), parent, ForgeDirection.WEST)
				&& !(n instanceof PartNull);
		}
		
		if(part instanceof PartTorch && pos.x > 1) System.out.println(pos + " " + c1 + " " + c2 + " " + c3 + " " + c4);
		
		return (c1 ? 1 : 0) << 3 | (c2 ? 1 : 0) << 2 | (c3 ? 1 : 0) << 1 | (c4 ? 1 : 0);
	}
	
	public static void addQuad(double x, double y, double u, double v, double w, double h)
	{
		addQuad(x, y, u, v, w, h, 0);
	}
	
	public static void addQuad(double x, double y, double u, double v, double w, double h, double rotation)
	{
		addQuad(x, y, u, v, w, h, w, h, 256, 256, rotation);
	}
	
	public static void addQuad(double x, double y, double u, double v, double w, double h, double w2, double h2, double tw, double th, double rotation)
	{
		double d1, d2, d3, d4;
		double scalew = 1 / tw;
		double scaleh = 1 / th;
		Tessellator tes = Tessellator.instance;
		
		d1 = u + 0;
		d2 = u + w2;
		
		if(rotation == 1)
		{
			d3 = v + h2;
			d4 = v + 0;
			
			tes.addVertexWithUV(x + w, y + h, 0, d1 * scalew, d4 * scaleh);
			tes.addVertexWithUV(x + w, y + 0, 0, d2 * scalew, d4 * scaleh);
			tes.addVertexWithUV(x + 0, y + 0, 0, d2 * scalew, d3 * scaleh);
			tes.addVertexWithUV(x + 0, y + h, 0, d1 * scalew, d3 * scaleh);
		}
		else if(rotation == 2)
		{
			d3 = v + h2;
			d4 = v + 0;
			
			tes.addVertexWithUV(x + 0, y + h, 0, d1 * scalew, d4 * scaleh);
			tes.addVertexWithUV(x + w, y + h, 0, d2 * scalew, d4 * scaleh);
			tes.addVertexWithUV(x + w, y + 0, 0, d2 * scalew, d3 * scaleh);
			tes.addVertexWithUV(x + 0, y + 0, 0, d1 * scalew, d3 * scaleh);
		}
		else if(rotation == 3)
		{
			d3 = v + 0;
			d4 = v + h2;
			
			tes.addVertexWithUV(x + w, y + h, 0, d1 * scalew, d4 * scaleh);
			tes.addVertexWithUV(x + w, y + 0, 0, d2 * scalew, d4 * scaleh);
			tes.addVertexWithUV(x + 0, y + 0, 0, d2 * scalew, d3 * scaleh);
			tes.addVertexWithUV(x + 0, y + h, 0, d1 * scalew, d3 * scaleh);
		}
		else
		{
			d3 = v + 0;
			d4 = v + h2;
			
			tes.addVertexWithUV(x + 0, y + h, 0, d1 * scalew, d4 * scaleh);
			tes.addVertexWithUV(x + w, y + h, 0, d2 * scalew, d4 * scaleh);
			tes.addVertexWithUV(x + w, y + 0, 0, d2 * scalew, d3 * scaleh);
			tes.addVertexWithUV(x + 0, y + 0, 0, d1 * scalew, d3 * scaleh);
		}
	}
	
	public static void renderParts(ICircuit circuit, double offX, double offY)
	{
		Tessellator tes = Tessellator.instance;
		int w = circuit.getCircuitData().getSize();

		GL11.glTranslated(offX, offY, 0);
		Minecraft.getMinecraft().getTextureManager().bindTexture(partResource);
		tes.startDrawingQuads();
		for(int x2 = 0; x2 < w; x2++)
		{
			for(int y2 = 0; y2 < w; y2++)
			{
				Vec2 pos = new Vec2(x2, y2);
				renderPartPayload(pos, circuit, circuit.getCircuitData().getPart(pos), x2 * 16, y2 * 16, 0);
			}
		}
		tes.draw();
		GL11.glTranslated(-offX, -offY, 0);
	}
	
	public static void renderParts(ICircuit circuit, double offX, double offY, boolean[][] exc, int type)
	{
		Tessellator tes = Tessellator.instance;
		int w = circuit.getCircuitData().getSize();
	
		GL11.glTranslated(offX, offY, 0);
		Minecraft.getMinecraft().getTextureManager().bindTexture(partResource);
		tes.startDrawingQuads();
		for(int x2 = 0; x2 < w; x2++)
		{
			for(int y2 = 0; y2 < w; y2++)
			{
				Vec2 pos = new Vec2(x2, y2);
				if(exc[x2][y2]) renderPartPayload(pos, circuit, circuit.getCircuitData().getPart(pos), x2 * 16, y2 * 16, type);
			}
		}
		tes.draw();
		GL11.glTranslated(-offX, -offY, 0);
	}
	
	public static void renderPerfboard(double offX, double offY, CircuitData data)
	{
		Tessellator tes = Tessellator.instance;
		int w = data.getSize();
		
		GL11.glTranslated(offX, offY, 0);
		Minecraft.getMinecraft().getTextureManager().bindTexture(partBG1);
		tes.startDrawingQuads();
		tes.setColorRGBA_F(1F, 1F, 1F, 1F);
		addQuad(0, 0, 0, 0, data.getSize() * 16, data.getSize() * 16, 16, 16, 16D / data.getSize(), 16D / data.getSize(), 0);
		tes.draw();
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(partBG2);
		tes.startDrawingQuads();
		tes.setColorRGBA_F(1F, 1F, 1F, 1F);
		addQuad(0, 0, 0, 0, 16, data.getSize() * 16, 16, 16, 16D, 16D / data.getSize(), 0);
		addQuad(data.getSize() * 16 - 16, 0, 0, 0, 16, data.getSize() * 16, 16, 16, 16, 16D / data.getSize(), 0);
		addQuad(0, 0, 0, 0, data.getSize() * 16, 16, 16, 16, 16D / data.getSize(), 16, 0);
		addQuad(0, data.getSize() * 16 - 16, 0, 0, data.getSize() * 16, 16, 16, 16, 16D / data.getSize(), 16, 0);
		tes.draw();
		GL11.glTranslated(-offX, -offY, 0);
	}
	
	public static void renderPartIOBit(Vec2 pos, ICircuit parent, PartIOBit bit, double x, double y, int type)
	{
		int freq = bit.getFrequency(pos, parent);
		int rot = bit.getRotation(pos, parent);
		Tessellator tes = Tessellator.instance;
		
		if(type == 2)
		{
			tes.setColorRGBA(188, 167, 60, 255);
			addQuad(x, y, 6 * 16, 3 * 16, 16, 16, rot);
		}
		else
		{
			tes.setColorRGBA_F(1F, 1F, 1F, 1F);
			addQuad(x, y, 2 * 16, 2 * 16, 16, 16, rot);
			if(bit.isPowered(pos, parent) && type == 0) tes.setColorRGBA_F(0F, 1F, 0F, 1F);
			else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
			addQuad(x, y, 4 * 16, 2 * 16, 16, 16, rot);
			if(type == 0)
			{
				tes.setColorRGBA_I(MapColor.getMapColorForBlockColored(freq).colorValue, 255);
				addQuad(x, y, 3 * 16, 2 * 16, 16, 16, rot);
			}
		}
	}

	public static void renderPartWire(Vec2 pos, ICircuit parent, PartWire wire, double x, double y, int type)
	{
		int color = wire.getColor(pos, parent);
		Tessellator tes = Tessellator.instance;
		
		if(type == 0)
		{
			switch (color) {
			case 1:
				if(wire.getInput(pos, parent)) tes.setColorRGBA_F(1F, 0F, 0F, 1F);
				else tes.setColorRGBA_F(0.4F, 0F, 0F, 1F);
				break;
			case 2:
				if(wire.getInput(pos, parent)) tes.setColorRGBA_F(1F, 0.4F, 0F, 1F);
				else tes.setColorRGBA_F(0.4F, 0.2F, 0F, 1F);
				break;
			default:
				if(wire.getInput(pos, parent)) tes.setColorRGBA_F(0F, 1F, 0F, 1F);
				else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
				break;
			}
		}
		else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
		
		int ty = type == 2 ? 3 * 16 : 0;
		
		int con = checkConnections(pos, parent, wire);
		if((con & 12) == 12 && (con & ~12) == 0) addQuad(x, y, 6 * 16, ty, 16, 16);
		else if((con & 3) == 3 && (con & ~3) == 0) addQuad(x, y, 5 * 16, ty, 16, 16);
		else 
		{
			if((con & 8) > 0) addQuad(x, y, 2 * 16, ty, 16, 16);
			if((con & 4) > 0) addQuad(x, y, 4 * 16, ty, 16, 16);
			if((con & 2) > 0) addQuad(x, y, 1 * 16, ty, 16, 16);
			if((con & 1) > 0) addQuad(x, y, 3 * 16, ty, 16, 16);
			addQuad(x, y, 0, ty, 16, 16);
		}
	}

	public static void renderPartCell(Vec2 pos, ICircuit parent, CircuitPart cell, double x, double y, int type) 
	{
		Tessellator tes = Tessellator.instance;
		int rotation = 0;
		if(cell instanceof PartCPGate) rotation = ((PartCPGate)cell).getRotation(pos, parent);
		
		if(type == 0 && (cell.getOutputToSide(pos, parent, MiscUtils.rotn(ForgeDirection.NORTH, rotation)) 
			|| cell.getInputFromSide(pos, parent, MiscUtils.rotn(ForgeDirection.NORTH, rotation)) 
			|| cell.getOutputToSide(pos, parent, MiscUtils.rotn(ForgeDirection.SOUTH, rotation)) 
			|| cell.getInputFromSide(pos, parent, MiscUtils.rotn(ForgeDirection.SOUTH, rotation))))
			tes.setColorRGBA_F(0F, 1F, 0F, 1F);
		else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
		addQuad(x, y, 0, 2 * 16, 16, 16, rotation);
		
		if(type == 0 && (cell.getOutputToSide(pos, parent, MiscUtils.rotn(ForgeDirection.EAST, rotation)) 
			|| cell.getInputFromSide(pos, parent, MiscUtils.rotn(ForgeDirection.EAST, rotation)) 
			|| cell.getOutputToSide(pos, parent, MiscUtils.rotn(ForgeDirection.WEST, rotation)) 
			|| cell.getInputFromSide(pos, parent, MiscUtils.rotn(ForgeDirection.WEST, rotation)))) 
			tes.setColorRGBA_F(0F, 1F, 0F, 1F);
		else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
		
		if(cell instanceof PartNullCell) addQuad(x, y, 16, 2 * 16, 16, 16, rotation);
		else if(cell instanceof PartInvertCell) addQuad(x, y, 5 * 16, 2 * 16, 16, 16, rotation);
		else if(cell instanceof PartBufferCell) addQuad(x, y, 6 * 16, 2 * 16, 16, 16, rotation);
	}
	
	public static void renderPartANDCell(Vec2 pos, ICircuit parent, PartANDCell cell, double x, double y, int type)
	{
		Tessellator tes = Tessellator.instance;
		int rotation = cell.getRotation(pos, parent);
		
		ForgeDirection fd = MiscUtils.rotn(ForgeDirection.NORTH, rotation);
		if(type == 0 && (cell.getOutputToSide(pos, parent, fd) 
			|| cell.getInputFromSide(pos, parent, fd) 
			|| cell.getOutputToSide(pos, parent, fd.getOpposite()) 
			|| cell.getInputFromSide(pos, parent, fd.getOpposite()))) 
			tes.setColorRGBA_F(0F, 1F, 0F, 1F);
		else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
		addQuad(x, y, 0, 2 * 16, 16, 16, rotation);
		
		fd = MiscUtils.rotn(ForgeDirection.WEST, rotation);
		if(type == 0 && (cell.getNeighbourOnSide(pos, parent, fd).getInputFromSide(pos.offset(fd), parent, fd.getOpposite())
			|| cell.getInputFromSide(pos, parent, fd)))
			tes.setColorRGBA_F(0F, 1F, 0F, 1F);
		else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
		addQuad(x, y, 7 * 16, 2 * 16, 16, 16, rotation);
		
		fd = MiscUtils.rotn(ForgeDirection.EAST, rotation);
		if(type == 0 && (cell.getNeighbourOnSide(pos, parent, fd).getInputFromSide(pos.offset(fd), parent, fd.getOpposite())
			|| cell.getInputFromSide(pos, parent, fd)))
			tes.setColorRGBA_F(0F, 1F, 0F, 1F);
		else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
		addQuad(x, y, 8 * 16, 2 * 16, 16, 16, rotation);	
	}
	
	public static void renderPartGate(Vec2 pos, ICircuit parent, PartCPGate gate, double x, double y, int type) 
	{
		Tessellator tes = Tessellator.instance;
		if(gate.canConnectToSide(pos, parent, ForgeDirection.NORTH))
		{
			if(type == 0 && (gate.getNeighbourOnSide(pos, parent, ForgeDirection.NORTH)
				.getInputFromSide(pos.offset(ForgeDirection.NORTH), parent, ForgeDirection.SOUTH) 
				|| gate.getInputFromSide(pos, parent, ForgeDirection.NORTH))) tes.setColorRGBA_F(0F, 1F, 0F, 1F);
			else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
			addQuad(x, y, 2 * 16, 0, 16, 16);
		}

		if(gate.canConnectToSide(pos, parent, ForgeDirection.SOUTH))
		{
			if(type == 0 && (gate.getNeighbourOnSide(pos, parent, ForgeDirection.SOUTH)
				.getInputFromSide(pos.offset(ForgeDirection.SOUTH), parent, ForgeDirection.NORTH) 
				|| gate.getInputFromSide(pos, parent, ForgeDirection.SOUTH))) tes.setColorRGBA_F(0F, 1F, 0F, 1F);
			else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
			addQuad(x, y, 4 * 16, 0, 16, 16);
		}
		
		if(gate.canConnectToSide(pos, parent, ForgeDirection.WEST))
		{
			if(type == 0 && (gate.getNeighbourOnSide(pos, parent, ForgeDirection.WEST)
				.getInputFromSide(pos.offset(ForgeDirection.WEST), parent, ForgeDirection.EAST) 
				|| gate.getInputFromSide(pos, parent, ForgeDirection.WEST))) tes.setColorRGBA_F(0F, 1F, 0F, 1F);
			else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
			addQuad(x, y, 1 * 16, 0, 16, 16);
		}
		
		if(gate.canConnectToSide(pos, parent, ForgeDirection.EAST))
		{
			if(type == 0 && (gate.getNeighbourOnSide(pos, parent, ForgeDirection.EAST)
				.getInputFromSide(pos.offset(ForgeDirection.EAST), parent, ForgeDirection.WEST) 
				|| gate.getInputFromSide(pos, parent, ForgeDirection.EAST))) tes.setColorRGBA_F(0F, 1F, 0F, 1F);
			else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
			addQuad(x, y, 3 * 16, 0, 16, 16);
		}

		tes.setColorRGBA_F(0F, 1F, 0F, 1F);
		
		if(gate instanceof PartNANDGate) addQuad(x, y, 10 * 16, 0, 16, 16, gate.getRotation(pos, parent));
		else if(gate instanceof PartNORGate) addQuad(x, y, 11 * 16, 0, 16, 16, gate.getRotation(pos, parent));
		else if(gate instanceof PartXNORGate) addQuad(x, y, 12 * 16, 0, 16, 16, gate.getRotation(pos, parent));
		else if(gate instanceof PartANDGate) addQuad(x, y, 7 * 16, 0, 16, 16, gate.getRotation(pos, parent));
		else if(gate instanceof PartORGate) addQuad(x, y, 8 * 16, 0, 16, 16, gate.getRotation(pos, parent));
		else if(gate instanceof PartXORGate) addQuad(x, y, 9 * 16, 0, 16, 16, gate.getRotation(pos, parent));
		else if(gate instanceof PartBufferGate) addQuad(x, y, 14 * 16, 0, 16, 16, gate.getRotation(pos, parent));
		else if(gate instanceof PartNOTGate) addQuad(x, y, 15 * 16, 0, 16, 16, gate.getRotation(pos, parent));
		
		else if(gate instanceof PartMultiplexer) addQuad(x, y, 0, 16, 16, 16, gate.getRotation(pos, parent));
		else if(gate instanceof PartTimer) addQuad(x, y, 2 * 16, 16, 16, 16, gate.getRotation(pos, parent));
		else if(gate instanceof PartSequencer) addQuad(x, y, 3 * 16, 16, 16, 16, gate.getRotation(pos, parent));
		else if(gate instanceof PartStateCell) addQuad(x, y, 4 * 16, 16, 16, 16, gate.getRotation(pos, parent));
		else if(gate instanceof PartRandomizer) addQuad(x, y, 5 * 16, 16, 16, 16, gate.getRotation(pos, parent));
		else if(gate instanceof PartSynchronizer) addQuad(x, y, 10 * 16, 16, 16, 16, gate.getRotation(pos, parent));
		
		else if(gate instanceof PartRSLatch) addQuad(x, y, 7 * 16, 16, 16, 16, gate.getRotation(pos, parent));
		else if(gate instanceof PartToggleLatch) addQuad(x, y, 8 * 16, 16, 16, 16, gate.getRotation(pos, parent));
		else if(gate instanceof PartTransparentLatch) addQuad(x, y, 9 * 16, 16, 16, 16, gate.getRotation(pos, parent));
		
		else if(gate instanceof PartRepeater) addQuad(x, y, 16, 16, 16, 16, gate.getRotation(pos, parent));
		else if(gate instanceof PartPulseFormer) addQuad(x, y, 6 * 16, 16, 16, 16, gate.getRotation(pos, parent));
	}
	
	public static void renderPartTorch(Vec2 pos, ICircuit parent, PartTorch torch, double x, double y, int type) 
	{
		Tessellator.instance.setColorRGBA_F(0F, 1F, 0F, 1F);
		
		int con = checkConnections(pos, parent, torch);
		if((con & 8) > 0) addQuad(x, y, 2 * 16, 0, 16, 16);
		if((con & 4) > 0) addQuad(x, y, 4 * 16, 0, 16, 16);
		if((con & 2) > 0) addQuad(x, y, 1 * 16, 0, 16, 16);
		if((con & 1) > 0) addQuad(x, y, 3 * 16, 0, 16, 16);
		
		addQuad(x, y, 13 * 16, 0, 16, 16);
	}
	
	public static class CircuitRenderWrapper implements ICircuit
	{
		private CircuitData data;
		private CircuitPart part;
		private Vec2 pos = new Vec2(1, 1);

		public CircuitRenderWrapper(Class<? extends CircuitPart> clazz)
		{
			this(clazz, 0);
		}
		
		public CircuitRenderWrapper(Class<? extends CircuitPart> clazz, int state)
		{
			this.data = CircuitData.createShallowInstance(state, this);
			this.part = CircuitPart.getPart(clazz);
		}
		
		public CircuitRenderWrapper(int state, CircuitPart part)
		{
			this.data = CircuitData.createShallowInstance(state, this);
			this.part = part;
		}
		
		public CircuitRenderWrapper(CircuitData data)
		{
			this.data = data;
		}

		public CircuitRenderWrapper setPart(CircuitPart part)
		{
			this.part = part;
			return this;
		}
		
		public CircuitPart getPart()
		{
			return part;
		}
		
		public CircuitRenderWrapper setState(int state)
		{
			this.data = CircuitData.createShallowInstance(state, this);
			return this;
		}
		
		@Override 
		public CircuitData getCircuitData() 
		{
			return data;
		}
		
		public Vec2 getPos()
		{
			return pos;
		}
		
		public int getState()
		{
			return getCircuitData().getMeta(getPos());
		}
		
		@Override public void setCircuitData(CircuitData data) {}
		@Override public boolean getInputFromSide(ForgeDirection dir, int frequency) { return false; }
		@Override public void setOutputToSide(ForgeDirection dir, int frequency, boolean output) {}
	}
}
