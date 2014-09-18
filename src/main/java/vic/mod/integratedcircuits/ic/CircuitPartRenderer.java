package vic.mod.integratedcircuits.ic;

import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.ic.CircuitPart.PartANDGate;
import vic.mod.integratedcircuits.ic.CircuitPart.PartBufferGate;
import vic.mod.integratedcircuits.ic.CircuitPart.PartGate;
import vic.mod.integratedcircuits.ic.CircuitPart.PartIOBit;
import vic.mod.integratedcircuits.ic.CircuitPart.PartMultiplexer;
import vic.mod.integratedcircuits.ic.CircuitPart.PartNANDGate;
import vic.mod.integratedcircuits.ic.CircuitPart.PartNORGate;
import vic.mod.integratedcircuits.ic.CircuitPart.PartNOTGate;
import vic.mod.integratedcircuits.ic.CircuitPart.PartNull;
import vic.mod.integratedcircuits.ic.CircuitPart.PartNullCell;
import vic.mod.integratedcircuits.ic.CircuitPart.PartORGate;
import vic.mod.integratedcircuits.ic.CircuitPart.PartPulseFormer;
import vic.mod.integratedcircuits.ic.CircuitPart.PartRSLatch;
import vic.mod.integratedcircuits.ic.CircuitPart.PartRandomizer;
import vic.mod.integratedcircuits.ic.CircuitPart.PartRepeater;
import vic.mod.integratedcircuits.ic.CircuitPart.PartSequencer;
import vic.mod.integratedcircuits.ic.CircuitPart.PartStateCell;
import vic.mod.integratedcircuits.ic.CircuitPart.PartSynchronizer;
import vic.mod.integratedcircuits.ic.CircuitPart.PartTimer;
import vic.mod.integratedcircuits.ic.CircuitPart.PartToggleLatch;
import vic.mod.integratedcircuits.ic.CircuitPart.PartTorch;
import vic.mod.integratedcircuits.ic.CircuitPart.PartTranspartentLatch;
import vic.mod.integratedcircuits.ic.CircuitPart.PartWire;
import vic.mod.integratedcircuits.ic.CircuitPart.PartXNORGate;
import vic.mod.integratedcircuits.ic.CircuitPart.PartXORGate;

public class CircuitPartRenderer 
{
	public static ResourceLocation partResource = new ResourceLocation(IntegratedCircuits.modID, "textures/gui/sublogicpart.png");
	
	public static void renderPart(CircuitPart part, double x, double y)
	{
		Minecraft.getMinecraft().getTextureManager().bindTexture(partResource);
		Tessellator tes = Tessellator.instance;
		GL11.glTranslated(x, y, 0);
		tes.startDrawingQuads();
		renderPartPayload(part, 0, 0);
		tes.draw();
		GL11.glTranslated(-x, -y, 0);
	}
	
	private static void renderPartPayload(CircuitPart part, double x, double y)
	{
		if(part instanceof PartWire) renderPartWire((PartWire)part, x, y);
		else if(part instanceof PartNullCell) renderPartNullCell((PartNullCell)part, x, y);
		else if(part instanceof PartGate) renderPartGate((PartGate)part, x, y);
		else if(part instanceof PartTorch) renderPartTorch((PartTorch)part, x, y);
		else if(part instanceof PartIOBit) renderPartIOBit((PartIOBit)part, x, y);
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
		
		boolean c3 = false; if(part.getX() > 0)
		{
			CircuitPart n = part.getNeighbourOnSide(ForgeDirection.WEST);
			c3 = part.canConnectToSide(ForgeDirection.WEST) && n.canConnectToSide(ForgeDirection.EAST) && !(n instanceof PartNull);
		}
		
		boolean c4 = false; if(part.getX() < part.getData().getSize())
		{
			CircuitPart n = part.getNeighbourOnSide(ForgeDirection.EAST);
			c4 = part.canConnectToSide(ForgeDirection.EAST) && n.canConnectToSide(ForgeDirection.WEST) && !(n instanceof PartNull);
		}
		
		return (c1 ? 1 : 0) << 3 | (c2 ? 1 : 0) << 2 | (c3 ? 1 : 0) << 1 | (c4 ? 1 : 0);
	}
	
	private static void addQuad(double x, double y, double u, double v, double w, double h)
	{
		addQuad(x, y, u, v, w, h, 0);
	}
	
	private static void addQuad(double x, double y, double u, double v, double w, double h, double rotation)
	{
		double d1, d2, d3, d4;
		double scale = 1 / 256D;
		Tessellator tes = Tessellator.instance;
		
		d1 = u + 0;
		d2 = u + w;
		
		if(rotation == 1)
		{
			d3 = v + h;
			d4 = v + 0;
			
			tes.addVertexWithUV(x + w, y + h, 0, d1 * scale, d4 * scale);
			tes.addVertexWithUV(x + w, y + 0, 0, d2 * scale, d4 * scale);
			tes.addVertexWithUV(x + 0, y + 0, 0, d2 * scale, d3 * scale);
			tes.addVertexWithUV(x + 0, y + h, 0, d1 * scale, d3 * scale);
		}
		else if(rotation == 2)
		{
			d3 = v + h;
			d4 = v + 0;
			
			tes.addVertexWithUV(x + 0, y + h, 0, d1 * scale, d4 * scale);
			tes.addVertexWithUV(x + w, y + h, 0, d2 * scale, d4 * scale);
			tes.addVertexWithUV(x + w, y + 0, 0, d2 * scale, d3 * scale);
			tes.addVertexWithUV(x + 0, y + 0, 0, d1 * scale, d3 * scale);
		}
		else if(rotation == 3)
		{
			d3 = v + 0;
			d4 = v + h;
			
			tes.addVertexWithUV(x + w, y + h, 0, d1 * scale, d4 * scale);
			tes.addVertexWithUV(x + w, y + 0, 0, d2 * scale, d4 * scale);
			tes.addVertexWithUV(x + 0, y + 0, 0, d2 * scale, d3 * scale);
			tes.addVertexWithUV(x + 0, y + h, 0, d1 * scale, d3 * scale);
		}
		else
		{
			d3 = v + 0;
			d4 = v + h;
			
			tes.addVertexWithUV(x + 0, y + h, 0, d1 * scale, d4 * scale);
			tes.addVertexWithUV(x + w, y + h, 0, d2 * scale, d4 * scale);
			tes.addVertexWithUV(x + w, y + 0, 0, d2 * scale, d3 * scale);
			tes.addVertexWithUV(x + 0, y + 0, 0, d1 * scale, d3 * scale);
		}
	}
	
	public static void renderPCB(double offX, double offY, CircuitData data)
	{
		Minecraft.getMinecraft().getTextureManager().bindTexture(partResource);
		Tessellator tes = Tessellator.instance;
		int w = data.getSize();
		
		GL11.glTranslated(offX, offY, 0);
		tes.startDrawingQuads();
		tes.setColorRGBA_F(1F, 1F, 1F, 1F);
		for(int x2 = 0; x2 < data.getSize(); x2++)
			for(int y2 = 0; y2 < data.getSize(); y2++)
				if(x2 == 0 || y2 == 0 || x2 == w - 1 || y2 == w - 1) 
					addQuad(x2 * 16, y2 * 16, 16, 15 * 16, 16, 16);
				else addQuad(x2 * 16, y2 * 16, 0, 15 * 16, 16, 16);
		tes.draw();
		
		tes.startDrawingQuads();
		for(int x2 = 0; x2 < data.getSize(); x2++)
			for(int y2 = 0; y2 < data.getSize(); y2++)
				renderPartPayload(data.getPart(x2, y2), x2 * 16, y2 * 16);
		tes.draw();
		GL11.glTranslated(-offX, -offY, 0);
	}
	
	public static void renderPartIOBit(PartIOBit bit, double x, double y)
	{
		int freq = bit.getFrequency();
		int rot = bit.getRotation();
		Tessellator tes = Tessellator.instance;
		tes.setColorRGBA_F(1F, 1F, 1F, 1F);
		addQuad(x, y, 2 * 16, 2 * 16, 16, 16, rot);
		if(bit.isPowered()) tes.setColorRGBA_F(0F, 1F, 0F, 1F);
		else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
		addQuad(x, y, 4 * 16, 2 * 16, 16, 16, rot);
		tes.setColorRGBA_I(MapColor.getMapColorForBlockColored(freq).colorValue, 255);
		addQuad(x, y, 3 * 16, 2 * 16, 16, 16, rot);
	}

	public static void renderPartWire(PartWire wire, double x, double y)
	{
		int color = wire.getColor();
		Tessellator tes = Tessellator.instance;
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
		
		int con = checkConnections(wire);
		if((con & 12) == 12 && (con & ~12) == 0) addQuad(x, y, 6 * 16, 0, 16, 16);
		else if((con & 3) == 3 && (con & ~3) == 0) addQuad(x, y, 5 * 16, 0, 16, 16);
		else 
		{
			if((con & 8) > 0) addQuad(x, y, 2 * 16, 0, 16, 16);
			if((con & 4) > 0) addQuad(x, y, 4 * 16, 0, 16, 16);
			if((con & 2) > 0) addQuad(x, y, 1 * 16, 0, 16, 16);
			if((con & 1) > 0) addQuad(x, y, 3 * 16, 0, 16, 16);
			addQuad(x, y, 0, 0, 16, 16);
		}
	}

	public static void renderPartNullCell(PartNullCell cell, double x, double y) 
	{
		Tessellator tes = Tessellator.instance;
		
		if(cell.getOutputToSide(ForgeDirection.NORTH) 
			|| cell.getInputFromSide(ForgeDirection.NORTH) 
			|| cell.getOutputToSide(ForgeDirection.SOUTH) 
			|| cell.getInputFromSide(ForgeDirection.SOUTH)) 
			tes.setColorRGBA_F(0F, 1F, 0F, 1F);
		else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
		addQuad(x, y, 0, 2 * 16, 16, 16);
		
		if(cell.getOutputToSide(ForgeDirection.EAST) 
			|| cell.getInputFromSide(ForgeDirection.EAST) 
			|| cell.getOutputToSide(ForgeDirection.WEST) 
			|| cell.getInputFromSide(ForgeDirection.WEST)) 
			tes.setColorRGBA_F(0F, 1F, 0F, 1F);
		else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
		addQuad(x, y, 16, 2 * 16, 16, 16);
	}
	
	public static void renderPartGate(PartGate gate, double x, double y) 
	{
		Tessellator tes = Tessellator.instance;
		if(gate.canConnectToSide(ForgeDirection.NORTH))
		{
			if(gate.getNeighbourOnSide(ForgeDirection.NORTH).getInputFromSide(ForgeDirection.SOUTH) || gate.getInputFromSide(ForgeDirection.NORTH)) tes.setColorRGBA_F(0F, 1F, 0F, 1F);
			else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
			addQuad(x, y, 2 * 16, 0, 16, 16);
		}

		if(gate.canConnectToSide(ForgeDirection.SOUTH))
		{
			if(gate.getNeighbourOnSide(ForgeDirection.SOUTH).getInputFromSide(ForgeDirection.NORTH) || gate.getInputFromSide(ForgeDirection.SOUTH)) tes.setColorRGBA_F(0F, 1F, 0F, 1F);
			else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
			addQuad(x, y, 4 * 16, 0, 16, 16);
		}
		
		if(gate.canConnectToSide(ForgeDirection.WEST))
		{
			if(gate.getNeighbourOnSide(ForgeDirection.WEST).getInputFromSide(ForgeDirection.EAST) || gate.getInputFromSide(ForgeDirection.WEST)) tes.setColorRGBA_F(0F, 1F, 0F, 1F);
			else tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
			addQuad(x, y, 1 * 16, 0, 16, 16);
		}
		
		if(gate.canConnectToSide(ForgeDirection.EAST))
		{
			if(gate.getNeighbourOnSide(ForgeDirection.EAST).getInputFromSide(ForgeDirection.WEST) || gate.getInputFromSide(ForgeDirection.EAST)) tes.setColorRGBA_F(0F, 1F, 0F, 1F);
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
	
	public static void renderPartTorch(PartTorch torch, double x, double y) 
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
