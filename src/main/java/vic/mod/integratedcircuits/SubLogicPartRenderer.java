package vic.mod.integratedcircuits;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.SubLogicPart.PartANDGate;
import vic.mod.integratedcircuits.SubLogicPart.PartBufferGate;
import vic.mod.integratedcircuits.SubLogicPart.PartGate;
import vic.mod.integratedcircuits.SubLogicPart.PartMultiplexer;
import vic.mod.integratedcircuits.SubLogicPart.PartNANDGate;
import vic.mod.integratedcircuits.SubLogicPart.PartNORGate;
import vic.mod.integratedcircuits.SubLogicPart.PartNOTGate;
import vic.mod.integratedcircuits.SubLogicPart.PartORGate;
import vic.mod.integratedcircuits.SubLogicPart.PartPulseFormer;
import vic.mod.integratedcircuits.SubLogicPart.PartRSLatch;
import vic.mod.integratedcircuits.SubLogicPart.PartRandomizer;
import vic.mod.integratedcircuits.SubLogicPart.PartRepeater;
import vic.mod.integratedcircuits.SubLogicPart.PartSequencer;
import vic.mod.integratedcircuits.SubLogicPart.PartStateCell;
import vic.mod.integratedcircuits.SubLogicPart.PartTimer;
import vic.mod.integratedcircuits.SubLogicPart.PartToggleLatch;
import vic.mod.integratedcircuits.SubLogicPart.PartTorch;
import vic.mod.integratedcircuits.SubLogicPart.PartTranspartentLatch;
import vic.mod.integratedcircuits.SubLogicPart.PartWire;
import vic.mod.integratedcircuits.SubLogicPart.PartXNORGate;
import vic.mod.integratedcircuits.SubLogicPart.PartXORGate;

public class SubLogicPartRenderer 
{
	public static void renderPart(SubLogicPart part, Gui gui)
	{
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(IntegratedCircuits.modID, "textures/gui/sublogicpart.png"));
		GL11.glPushMatrix();
		if(part instanceof PartWire) renderPartWire((PartWire)part, gui);
		else if(part instanceof PartRepeater || part instanceof PartPulseFormer) renderPart1I1O((PartGate)part, gui);
		else if(part instanceof PartGate) renderPartGate((PartGate)part, gui);
		else if(part instanceof PartTorch) renderPartTorch((PartTorch)part, gui);
		GL11.glPopMatrix();
	}

	public static void drawTexture(int u, int v, Gui gui, SubLogicPart part)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef(part.getX() * 16F, part.getY() * 16F, 0);
		gui.drawTexturedModalRect(0, 0, u, v, 16, 16);
		GL11.glPopMatrix();
	}
	
	public static void drawTexture(int u, int v, float rotation, Gui gui, SubLogicPart part)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef(part.getX() * 16F, part.getY() * 16F, 0);
		GL11.glTranslatef(8F, 8F, 0);
		GL11.glRotatef(rotation, 0, 0, 1);
		GL11.glTranslatef(-8F, -8F, 0);
		gui.drawTexturedModalRect(0, 0, u, v, 16, 16);
		GL11.glPopMatrix();
	}
	
	private static int checkConnections(SubLogicPart part)
	{
		boolean c1 = part.getY() > 0 ? part.canConnectToSide(ForgeDirection.NORTH) && part.getNeighbourOnSide(ForgeDirection.NORTH).canConnectToSide(ForgeDirection.SOUTH) : false;
		boolean c2 = part.getY() < part.getParent().getMatrix()[0].length ? part.canConnectToSide(ForgeDirection.SOUTH) && part.getNeighbourOnSide(ForgeDirection.SOUTH).canConnectToSide(ForgeDirection.NORTH) : false;
		boolean c3 = part.getX() > 0 ? part.canConnectToSide(ForgeDirection.WEST) && part.getNeighbourOnSide(ForgeDirection.WEST).canConnectToSide(ForgeDirection.EAST) : false;
		boolean c4 = part.getX() < part.getParent().getMatrix()[0].length ? part.canConnectToSide(ForgeDirection.EAST) && part.getNeighbourOnSide(ForgeDirection.EAST).canConnectToSide(ForgeDirection.WEST) : false;
		return (c1 ? 1 : 0) << 3 | (c2 ? 1 : 0) << 2 | (c3 ? 1 : 0) << 1 | (c4 ? 1 : 0);
	}
	
	public static void renderPartWire(PartWire wire, Gui gui)
	{
		if(wire.getInput()) GL11.glColor3f(0F, 1F, 0F);
		else GL11.glColor3f(0F, 0.4F, 0F);
		int con = checkConnections(wire);
		if((con & 12) == 12 && (con & ~12) == 0) drawTexture(6 * 16, 0, gui, wire);
		else if((con & 3) == 3 && (con & ~3) == 0) drawTexture(5 * 16, 0, gui, wire);
		else 
		{
			if((con & 8) > 0) drawTexture(2 * 16, 0, gui, wire);
			if((con & 4) > 0) drawTexture(4 * 16, 0, gui, wire);
			if((con & 2) > 0) drawTexture(1 * 16, 0, gui, wire);
			if((con & 1) > 0) drawTexture(3 * 16, 0, gui, wire);
			drawTexture(0, 0, gui, wire);
		}
	}
	
	public static void renderPartGate(PartGate gate, Gui gui) 
	{
		if(gate.getOutputToSide(ForgeDirection.NORTH) || gate.getInputFromSide(ForgeDirection.NORTH)) GL11.glColor3f(0F, 1F, 0F);
		else GL11.glColor3f(0F, 0.4F, 0F);
		drawTexture(2 * 16, 0, gui, gate);
		
		if(gate.getOutputToSide(ForgeDirection.SOUTH) || gate.getInputFromSide(ForgeDirection.SOUTH)) GL11.glColor3f(0F, 1F, 0F);
		else GL11.glColor3f(0F, 0.4F, 0F);
		drawTexture(4 * 16, 0, gui, gate);
		
		if(gate.getOutputToSide(ForgeDirection.WEST) || gate.getInputFromSide(ForgeDirection.WEST)) GL11.glColor3f(0F, 1F, 0F);
		else GL11.glColor3f(0F, 0.4F, 0F);
		drawTexture(1 * 16, 0, gui, gate);
		
		if(gate.getOutputToSide(ForgeDirection.EAST) || gate.getInputFromSide(ForgeDirection.EAST)) GL11.glColor3f(0F, 1F, 0F);
		else GL11.glColor3f(0F, 0.4F, 0F);
		drawTexture(3 * 16, 0, gui, gate);

		GL11.glColor3f(0F, 1F, 0F);
		
		if(gate instanceof PartNANDGate) drawTexture(10 * 16, 0, gate.getRotation() * 90, gui, gate);
		else if(gate instanceof PartNORGate) drawTexture(11 * 16, 0, gate.getRotation() * 90, gui, gate);
		else if(gate instanceof PartXNORGate) drawTexture(12 * 16, 0, gate.getRotation() * 90, gui, gate);
		else if(gate instanceof PartANDGate) drawTexture(7 * 16, 0, gate.getRotation() * 90, gui, gate);
		else if(gate instanceof PartORGate) drawTexture(8 * 16, 0, gate.getRotation() * 90, gui, gate);
		else if(gate instanceof PartXORGate) drawTexture(9 * 16, 0, gate.getRotation() * 90, gui, gate);
		else if(gate instanceof PartBufferGate) drawTexture(14 * 16, 0, gate.getRotation() * 90, gui, gate);
		else if(gate instanceof PartNOTGate) drawTexture(15 * 16, 0, gate.getRotation() * 90, gui, gate);
		
		else if(gate instanceof PartMultiplexer) drawTexture(0, 16, gate.getRotation() * 90, gui, gate);
		else if(gate instanceof PartTimer) drawTexture(2 * 16, 16, gate.getRotation() * 90, gui, gate);
		else if(gate instanceof PartSequencer) drawTexture(3 * 16, 16, gate.getRotation() * 90, gui, gate);
		else if(gate instanceof PartStateCell) drawTexture(4 * 16, 16, gate.getRotation() * 90, gui, gate);
		else if(gate instanceof PartRandomizer) drawTexture(5 * 16, 16, gate.getRotation() * 90, gui, gate);
		
		else if(gate instanceof PartRSLatch) drawTexture(7 * 16, 16, gate.getRotation() * 90, gui, gate);
		else if(gate instanceof PartToggleLatch) drawTexture(8 * 16, 16, gate.getRotation() * 90, gui, gate);
		else if(gate instanceof PartTranspartentLatch) drawTexture(9 * 16, 16, gate.getRotation() * 90, gui, gate);
	}
	
	public static void renderPartTorch(PartTorch torch, Gui gui) 
	{
		GL11.glColor3f(0F, 1F, 0F);
		
		int con = checkConnections(torch);
		if((con & 8) > 0) drawTexture(2 * 16, 0, gui, torch);
		if((con & 4) > 0) drawTexture(4 * 16, 0, gui, torch);
		if((con & 2) > 0) drawTexture(1 * 16, 0, gui, torch);
		if((con & 1) > 0) drawTexture(3 * 16, 0, gui, torch);
		
		drawTexture(13 * 16, 0, gui, torch);
	}
	
	public static void renderPart1I1O(PartGate gate, Gui gui)
	{
		if(gate.getOutputToSide(ForgeDirection.NORTH) || gate.getInputFromSide(ForgeDirection.NORTH)) GL11.glColor3f(0F, 1F, 0F);
		else GL11.glColor3f(0F, 0.4F, 0F);
		drawTexture(2 * 16, 0, gui, gate);
		
		if(gate.getOutputToSide(ForgeDirection.SOUTH) || gate.getInputFromSide(ForgeDirection.SOUTH)) GL11.glColor3f(0F, 1F, 0F);
		else GL11.glColor3f(0F, 0.4F, 0F);
		drawTexture(4 * 16, 0, gui, gate);
		
		GL11.glColor3f(0F, 1F, 0F);
		
		if(gate instanceof PartRepeater) drawTexture(16, 16, gate.getRotation() * 90, gui, gate);
		else if(gate instanceof PartPulseFormer) drawTexture(6 * 16, 16, gate.getRotation() * 90, gui, gate);
	}
}
