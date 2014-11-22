package vic.mod.integratedcircuits.ic;

import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.ic.parts.PartGate;
import vic.mod.integratedcircuits.ic.parts.PartIOBit;
import vic.mod.integratedcircuits.ic.parts.PartMultiplexer;
import vic.mod.integratedcircuits.ic.parts.PartNull;
import vic.mod.integratedcircuits.ic.parts.PartSynchronizer;
import vic.mod.integratedcircuits.ic.parts.PartTorch;
import vic.mod.integratedcircuits.ic.parts.PartWire;
import vic.mod.integratedcircuits.ic.parts.cell.PartANDCell;
import vic.mod.integratedcircuits.ic.parts.cell.PartBufferCell;
import vic.mod.integratedcircuits.ic.parts.cell.PartInvertCell;
import vic.mod.integratedcircuits.ic.parts.cell.PartNullCell;
import vic.mod.integratedcircuits.ic.parts.latch.PartRSLatch;
import vic.mod.integratedcircuits.ic.parts.latch.PartToggleLatch;
import vic.mod.integratedcircuits.ic.parts.latch.PartTranspartentLatch;
import vic.mod.integratedcircuits.ic.parts.logic.PartANDGate;
import vic.mod.integratedcircuits.ic.parts.logic.PartBufferGate;
import vic.mod.integratedcircuits.ic.parts.logic.PartNANDGate;
import vic.mod.integratedcircuits.ic.parts.logic.PartNORGate;
import vic.mod.integratedcircuits.ic.parts.logic.PartNOTGate;
import vic.mod.integratedcircuits.ic.parts.logic.PartORGate;
import vic.mod.integratedcircuits.ic.parts.logic.PartXNORGate;
import vic.mod.integratedcircuits.ic.parts.logic.PartXORGate;
import vic.mod.integratedcircuits.ic.parts.timed.PartPulseFormer;
import vic.mod.integratedcircuits.ic.parts.timed.PartRandomizer;
import vic.mod.integratedcircuits.ic.parts.timed.PartRepeater;
import vic.mod.integratedcircuits.ic.parts.timed.PartSequencer;
import vic.mod.integratedcircuits.ic.parts.timed.PartStateCell;
import vic.mod.integratedcircuits.ic.parts.timed.PartTimer;
import vic.mod.integratedcircuits.util.MiscUtils;

public class CircuitPartRenderer 
{
	public static ResourceLocation partResource = new ResourceLocation(IntegratedCircuits.modID, "textures/gui/sublogicpart.png");
	public static ResourceLocation partBG1 = new ResourceLocation(IntegratedCircuits.modID, "textures/gui/bg1.png");
	public static ResourceLocation partBG2 = new ResourceLocation(IntegratedCircuits.modID, "textures/gui/bg2.png");
	
	public static void renderPart(CircuitPart part, double x, double y)
	{
		Minecraft.getMinecraft().getTextureManager().bindTexture(partResource);
		Tessellator tes = Tessellator.instance;
		GL11.glTranslated(x, y, 0);
		tes.startDrawingQuads();
		renderPartPayload(part, 0, 0, 0);
		tes.draw();
		GL11.glTranslated(-x, -y, 0);
	}
	
	private static void renderPartPayload(CircuitPart part, double x, double y, int type)
	{
		if(part instanceof PartWire) renderPartWire((PartWire)part, x, y, type);
		else if(part instanceof PartIOBit) renderPartIOBit((PartIOBit)part, x, y, type);
		else if(type == 2 && !(part instanceof PartNull)) 
		{
			Tessellator.instance.setColorRGBA_F(0, 0, 0, 1);
			addQuad(x, y, 0, 15 * 16, 16, 16);
		}
		else if(part instanceof PartNullCell || part instanceof PartInvertCell || part instanceof PartBufferCell) renderPartCell(part, x, y, type);
		else if(part instanceof PartANDCell) renderPartANDCell((PartANDCell)part, x, y, type);
		else if(part instanceof PartGate) renderPartGate((PartGate)part, x, y, type);
		else if(part instanceof PartTorch) renderPartTorch((PartTorch)part, x, y, type);	
	}

	private static int checkConnections(CircuitPart part)
	{
		boolean c1 = false;
		if(part.getY() > 0) 
		{
			CircuitPart n = part.getNeighbourOnSide(ForgeDirection.NORTH);
			c1 = part.canConnectToSide(ForgeDirection.NORTH) && n.canConnectToSide(ForgeDirection.SOUTH) && !(n instanceof PartNull);
		}
		
		boolean c2 = false; 
		if(part.getY() < part.getData().getSize())
		{
			CircuitPart n = part.getNeighbourOnSide(ForgeDirection.SOUTH);
			c2 = part.canConnectToSide(ForgeDirection.SOUTH) && n.canConnectToSide(ForgeDirection.NORTH) && !(n instanceof PartNull);
		}
		
		boolean c3 = false; 
		if(part.getX() > 0)
		{
			CircuitPart n = part.getNeighbourOnSide(ForgeDirection.WEST);
			c3 = part.canConnectToSide(ForgeDirection.WEST) && n.canConnectToSide(ForgeDirection.EAST) && !(n instanceof PartNull);
		}
		
		boolean c4 = false; 
		if(part.getX() < part.getData().getSize())
		{
			CircuitPart n = part.getNeighbourOnSide(ForgeDirection.EAST);
			c4 = part.canConnectToSide(ForgeDirection.EAST) && n.canConnectToSide(ForgeDirection.WEST) && !(n instanceof PartNull);
		}
		
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
	
	public static void renderParts(double offX, double offY, CircuitData data)
	{
		Tessellator tes = Tessellator.instance;
		int w = data.getSize();

		GL11.glTranslated(offX, offY, 0);
		Minecraft.getMinecraft().getTextureManager().bindTexture(partResource);
		tes.startDrawingQuads();
		for(int x2 = 0; x2 < data.getSize(); x2++)
			for(int y2 = 0; y2 < data.getSize(); y2++)
				renderPartPayload(data.getPart(x2, y2), x2 * 16, y2 * 16, 0);
		tes.draw();
		GL11.glTranslated(-offX, -offY, 0);
	}
	
	public static void renderParts(double offX, double offY, CircuitData data, boolean[][] exc, int type)
	{
		Tessellator tes = Tessellator.instance;
		int w = data.getSize();
	
		GL11.glTranslated(offX, offY, 0);
		Minecraft.getMinecraft().getTextureManager().bindTexture(partResource);
		tes.startDrawingQuads();
		for(int x2 = 0; x2 < data.getSize(); x2++)
			for(int y2 = 0; y2 < data.getSize(); y2++)
				if(exc[x2][y2]) renderPartPayload(data.getPart(x2, y2), x2 * 16, y2 * 16, type);
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
	
	public static void renderPartIOBit(PartIOBit bit, double x, double y, int type)
	{
		int freq = bit.getFrequency();
		int rot = bit.getRotation();
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
			if(bit.isPowered() && type == 0) tes.setColorRGBA_F(0F, 1F, 0F, 1F);
			else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
			addQuad(x, y, 4 * 16, 2 * 16, 16, 16, rot);
			if(type == 0)
			{
				tes.setColorRGBA_I(MapColor.getMapColorForBlockColored(freq).colorValue, 255);
				addQuad(x, y, 3 * 16, 2 * 16, 16, 16, rot);
			}
		}
	}

	public static void renderPartWire(PartWire wire, double x, double y, int type)
	{
		int color = wire.getColor();
		Tessellator tes = Tessellator.instance;
		
		if(type == 0)
		{
			switch (color) {
			case 1:
				if(wire.getInput()) tes.setColorRGBA_F(1F, 0F, 0F, 1F);
				else tes.setColorRGBA_F(0.4F, 0F, 0F, 1F);
				break;
			case 2:
				if(wire.getInput()) tes.setColorRGBA_F(1F, 0.4F, 0F, 1F);
				else tes.setColorRGBA_F(0.4F, 0.2F, 0F, 1F);
				break;
			default:
				if(wire.getInput()) tes.setColorRGBA_F(0F, 1F, 0F, 1F);
				else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
				break;
			}
		}
		else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
		
		int ty = type == 2 ? 3 * 16 : 0;
		
		int con = checkConnections(wire);
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

	public static void renderPartCell(CircuitPart cell, double x, double y, int type) 
	{
		Tessellator tes = Tessellator.instance;
		int rotation = 0;
		if(cell instanceof PartGate) rotation = ((PartGate)cell).getRotation();
		
		if(type == 0 && (cell.getOutputToSide(MiscUtils.rotn(ForgeDirection.NORTH, rotation)) 
			|| cell.getInputFromSide(MiscUtils.rotn(ForgeDirection.NORTH, rotation)) 
			|| cell.getOutputToSide(MiscUtils.rotn(ForgeDirection.SOUTH, rotation)) 
			|| cell.getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, rotation))))
			tes.setColorRGBA_F(0F, 1F, 0F, 1F);
		else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
		addQuad(x, y, 0, 2 * 16, 16, 16, rotation);
		
		if(type == 0 && (cell.getOutputToSide(MiscUtils.rotn(ForgeDirection.EAST, rotation)) 
			|| cell.getInputFromSide(MiscUtils.rotn(ForgeDirection.EAST, rotation)) 
			|| cell.getOutputToSide(MiscUtils.rotn(ForgeDirection.WEST, rotation)) 
			|| cell.getInputFromSide(MiscUtils.rotn(ForgeDirection.WEST, rotation)))) 
			tes.setColorRGBA_F(0F, 1F, 0F, 1F);
		else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
		
		if(cell instanceof PartNullCell) addQuad(x, y, 16, 2 * 16, 16, 16, rotation);
		else if(cell instanceof PartInvertCell) addQuad(x, y, 5 * 16, 2 * 16, 16, 16, rotation);
		else if(cell instanceof PartBufferCell) addQuad(x, y, 6 * 16, 2 * 16, 16, 16, rotation);
	}
	
	public static void renderPartANDCell(PartANDCell cell, double x, double y, int type)
	{
		Tessellator tes = Tessellator.instance;
		int rotation = cell.getRotation();
		
		ForgeDirection fd = MiscUtils.rotn(ForgeDirection.NORTH, rotation);
		if(type == 0 && (cell.getOutputToSide(fd) 
			|| cell.getInputFromSide(fd) 
			|| cell.getOutputToSide(fd.getOpposite()) 
			|| cell.getInputFromSide(fd.getOpposite()))) 
			tes.setColorRGBA_F(0F, 1F, 0F, 1F);
		else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
		addQuad(x, y, 0, 2 * 16, 16, 16, rotation);
		
		fd = MiscUtils.rotn(ForgeDirection.WEST, rotation);
		if(type == 0 && (cell.getNeighbourOnSide(fd).getInputFromSide(fd.getOpposite())
			|| cell.getInputFromSide(fd)))
			tes.setColorRGBA_F(0F, 1F, 0F, 1F);
		else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
		addQuad(x, y, 7 * 16, 2 * 16, 16, 16, rotation);
		
		fd = MiscUtils.rotn(ForgeDirection.EAST, rotation);
		if(type == 0 && (cell.getNeighbourOnSide(fd).getInputFromSide(fd.getOpposite())
			|| cell.getInputFromSide(fd)))
			tes.setColorRGBA_F(0F, 1F, 0F, 1F);
		else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
		addQuad(x, y, 8 * 16, 2 * 16, 16, 16, rotation);	
	}
	
	public static void renderPartGate(PartGate gate, double x, double y, int type) 
	{
		Tessellator tes = Tessellator.instance;
		if(gate.canConnectToSide(ForgeDirection.NORTH))
		{
			if(type == 0 && (gate.getNeighbourOnSide(ForgeDirection.NORTH).getInputFromSide(ForgeDirection.SOUTH) 
				|| gate.getInputFromSide(ForgeDirection.NORTH))) tes.setColorRGBA_F(0F, 1F, 0F, 1F);
			else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
			addQuad(x, y, 2 * 16, 0, 16, 16);
		}

		if(gate.canConnectToSide(ForgeDirection.SOUTH))
		{
			if(type == 0 && (gate.getNeighbourOnSide(ForgeDirection.SOUTH).getInputFromSide(ForgeDirection.NORTH) 
				|| gate.getInputFromSide(ForgeDirection.SOUTH))) tes.setColorRGBA_F(0F, 1F, 0F, 1F);
			else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
			addQuad(x, y, 4 * 16, 0, 16, 16);
		}
		
		if(gate.canConnectToSide(ForgeDirection.WEST))
		{
			if(type == 0 && (gate.getNeighbourOnSide(ForgeDirection.WEST).getInputFromSide(ForgeDirection.EAST) 
				|| gate.getInputFromSide(ForgeDirection.WEST))) tes.setColorRGBA_F(0F, 1F, 0F, 1F);
			else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
			addQuad(x, y, 1 * 16, 0, 16, 16);
		}
		
		if(gate.canConnectToSide(ForgeDirection.EAST))
		{
			if(type == 0 && (gate.getNeighbourOnSide(ForgeDirection.EAST).getInputFromSide(ForgeDirection.WEST) 
				|| gate.getInputFromSide(ForgeDirection.EAST))) tes.setColorRGBA_F(0F, 1F, 0F, 1F);
			else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
			addQuad(x, y, 3 * 16, 0, 16, 16);
		}

		tes.setColorRGBA_F(0F, 1F, 0F, 1F);
		
		if(gate instanceof PartNANDGate) addQuad(x, y, 10 * 16, 0, 16, 16, gate.getRotation());
		else if(gate instanceof PartNORGate) addQuad(x, y, 11 * 16, 0, 16, 16, gate.getRotation());
		else if(gate instanceof PartXNORGate) addQuad(x, y, 12 * 16, 0, 16, 16, gate.getRotation());
		else if(gate instanceof PartANDGate) addQuad(x, y, 7 * 16, 0, 16, 16, gate.getRotation());
		else if(gate instanceof PartORGate) addQuad(x, y, 8 * 16, 0, 16, 16, gate.getRotation());
		else if(gate instanceof PartXORGate) addQuad(x, y, 9 * 16, 0, 16, 16, gate.getRotation());
		else if(gate instanceof PartBufferGate) addQuad(x, y, 14 * 16, 0, 16, 16, gate.getRotation());
		else if(gate instanceof PartNOTGate) addQuad(x, y, 15 * 16, 0, 16, 16, gate.getRotation());
		
		else if(gate instanceof PartMultiplexer) addQuad(x, y, 0, 16, 16, 16, gate.getRotation());
		else if(gate instanceof PartTimer) addQuad(x, y, 2 * 16, 16, 16, 16, gate.getRotation());
		else if(gate instanceof PartSequencer) addQuad(x, y, 3 * 16, 16, 16, 16, gate.getRotation());
		else if(gate instanceof PartStateCell) addQuad(x, y, 4 * 16, 16, 16, 16, gate.getRotation());
		else if(gate instanceof PartRandomizer) addQuad(x, y, 5 * 16, 16, 16, 16, gate.getRotation());
		else if(gate instanceof PartSynchronizer) addQuad(x, y, 10 * 16, 16, 16, 16, gate.getRotation());
		
		else if(gate instanceof PartRSLatch) addQuad(x, y, 7 * 16, 16, 16, 16, gate.getRotation());
		else if(gate instanceof PartToggleLatch) addQuad(x, y, 8 * 16, 16, 16, 16, gate.getRotation());
		else if(gate instanceof PartTranspartentLatch) addQuad(x, y, 9 * 16, 16, 16, 16, gate.getRotation());
		
		else if(gate instanceof PartRepeater) addQuad(x, y, 16, 16, 16, 16, gate.getRotation());
		else if(gate instanceof PartPulseFormer) addQuad(x, y, 6 * 16, 16, 16, 16, gate.getRotation());
	}
	
	public static void renderPartTorch(PartTorch torch, double x, double y, int type) 
	{
		Tessellator.instance.setColorRGBA_F(0F, 1F, 0F, 1F);
		
		int con = checkConnections(torch);
		if((con & 8) > 0) addQuad(x, y, 2 * 16, 0, 16, 16);
		if((con & 4) > 0) addQuad(x, y, 4 * 16, 0, 16, 16);
		if((con & 2) > 0) addQuad(x, y, 1 * 16, 0, 16, 16);
		if((con & 1) > 0) addQuad(x, y, 3 * 16, 0, 16, 16);
		
		addQuad(x, y, 13 * 16, 0, 16, 16);
	}
	
	/** Used for rendering **/
	public static CircuitPart createEncapsulated(Class<? extends CircuitPart> clazz)
	{
		return CircuitPart.getPart(CircuitPart.getIdFromClass(clazz)).prepare(1, 1, CurcuitRenderWrapper.instance.data);
	}
	
	public static CircuitPart createEncapsulated(Class<? extends CircuitPart> clazz, int state)
	{
		return CircuitPart.getPart(CircuitPart.getIdFromClass(clazz)).prepare(1, 1, new CurcuitRenderWrapper(state).data);
	}
	
	public static class CurcuitRenderWrapper implements ICircuit
	{
		private CircuitData data;
		public static CurcuitRenderWrapper instance = new CurcuitRenderWrapper(0);
		
		private CurcuitRenderWrapper(int state)
		{
			data = CircuitData.createShallowInstance(state, this);
		}
		
		@Override 
		public CircuitData getCircuitData() 
		{
			return data;
		}

		@Override public void setCircuitData(CircuitData data) {}
		@Override public boolean getInputFromSide(ForgeDirection dir, int frequency) { return false; }
		@Override public void setOutputToSide(ForgeDirection dir, int frequency, boolean output) {}
	}
}
