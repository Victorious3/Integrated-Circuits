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
import vic.mod.integratedcircuits.SubLogicPart.PartNullCell;
import vic.mod.integratedcircuits.SubLogicPart.PartORGate;
import vic.mod.integratedcircuits.SubLogicPart.PartPulseFormer;
import vic.mod.integratedcircuits.SubLogicPart.PartRSLatch;
import vic.mod.integratedcircuits.SubLogicPart.PartRandomizer;
import vic.mod.integratedcircuits.SubLogicPart.PartRepeater;
import vic.mod.integratedcircuits.SubLogicPart.PartSequencer;
import vic.mod.integratedcircuits.SubLogicPart.PartStateCell;
import vic.mod.integratedcircuits.SubLogicPart.PartSynchronizer;
import vic.mod.integratedcircuits.SubLogicPart.PartTimer;
import vic.mod.integratedcircuits.SubLogicPart.PartToggleLatch;
import vic.mod.integratedcircuits.SubLogicPart.PartTorch;
import vic.mod.integratedcircuits.SubLogicPart.PartTranspartentLatch;
import vic.mod.integratedcircuits.SubLogicPart.PartWire;
import vic.mod.integratedcircuits.SubLogicPart.PartXNORGate;
import vic.mod.integratedcircuits.SubLogicPart.PartXORGate;

public class SubLogicPartRenderer 
{
	public static void renderPart(SubLogicPart part, Gui gui, double x, double y)
	{
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(IntegratedCircuits.modID, "textures/gui/sublogicpart.png"));
		GL11.glPushMatrix();
		if(part instanceof PartWire) renderPartWire((PartWire)part, gui, x, y);
		else if(part instanceof PartNullCell) renderPartNullCell((PartNullCell)part, gui, x, y);
		else if(part instanceof PartRepeater || part instanceof PartPulseFormer) renderPart1I1O((PartGate)part, gui, x, y);
		else if(part instanceof PartGate) renderPartGate((PartGate)part, gui, x, y);
		else if(part instanceof PartTorch) renderPartTorch((PartTorch)part, gui, x, y);
		GL11.glPopMatrix();
	}

	public static void drawTexture(int u, int v, Gui gui, double x, double y)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, 0);
		gui.drawTexturedModalRect(0, 0, u, v, 16, 16);
		GL11.glPopMatrix();
	}
	
	public static void drawTexture(int u, int v, float rotation, Gui gui, double x, double y)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, 0);
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
	
	public static void renderPartWire(PartWire wire, Gui gui, double x, double y)
	{
		int color = wire.getColor();
		switch (color) {
		case 1:
			if(wire.getInput()) GL11.glColor3f(1F, 0F, 0F);
			else GL11.glColor3f(0.4F, 0F, 0F);
			break;
		case 2:
			if(wire.getInput()) GL11.glColor3f(1F, 0.4F, 0F);
			else GL11.glColor3f(0.4F, 0.2F, 0F);
			break;
		default:
			if(wire.getInput()) GL11.glColor3f(0F, 1F, 0F);
			else GL11.glColor3f(0F, 0.4F, 0F);
			break;
		}	
		
		int con = checkConnections(wire);
		if((con & 12) == 12 && (con & ~12) == 0) drawTexture(6 * 16, 0, gui, x, y);
		else if((con & 3) == 3 && (con & ~3) == 0) drawTexture(5 * 16, 0, gui, x, y);
		else 
		{
			if((con & 8) > 0) drawTexture(2 * 16, 0, gui, x, y);
			if((con & 4) > 0) drawTexture(4 * 16, 0, gui, x, y);
			if((con & 2) > 0) drawTexture(1 * 16, 0, gui, x, y);
			if((con & 1) > 0) drawTexture(3 * 16, 0, gui, x, y);
			drawTexture(0, 0, gui, x, y);
		}
	}

	public static void renderPartNullCell(PartNullCell cell, Gui gui, double x, double y) 
	{
		if(cell.getOutputToSide(ForgeDirection.NORTH) 
			|| cell.getInputFromSide(ForgeDirection.NORTH) 
			|| cell.getOutputToSide(ForgeDirection.SOUTH) 
			|| cell.getInputFromSide(ForgeDirection.SOUTH)) 
			GL11.glColor3f(0F, 1F, 0F);
		else GL11.glColor3f(0F, 0.4F, 0F);
		drawTexture(0, 2 * 16, gui, x, y);
		
		if(cell.getOutputToSide(ForgeDirection.EAST) 
			|| cell.getInputFromSide(ForgeDirection.EAST) 
			|| cell.getOutputToSide(ForgeDirection.WEST) 
			|| cell.getInputFromSide(ForgeDirection.WEST)) 
			GL11.glColor3f(0F, 1F, 0F);
		else GL11.glColor3f(0F, 0.4F, 0F);
		drawTexture(16, 2 * 16, gui, x, y);
	}
	
	public static void renderPartGate(PartGate gate, Gui gui, double x, double y) 
	{
		if(gate.getOutputToSide(ForgeDirection.NORTH) || gate.getInputFromSide(ForgeDirection.NORTH)) GL11.glColor3f(0F, 1F, 0F);
		else GL11.glColor3f(0F, 0.4F, 0F);
		drawTexture(2 * 16, 0, gui, x, y);
		
		if(gate.getOutputToSide(ForgeDirection.SOUTH) || gate.getInputFromSide(ForgeDirection.SOUTH)) GL11.glColor3f(0F, 1F, 0F);
		else GL11.glColor3f(0F, 0.4F, 0F);
		drawTexture(4 * 16, 0, gui, x, y);
		
		if(gate.getOutputToSide(ForgeDirection.WEST) || gate.getInputFromSide(ForgeDirection.WEST)) GL11.glColor3f(0F, 1F, 0F);
		else GL11.glColor3f(0F, 0.4F, 0F);
		drawTexture(1 * 16, 0, gui, x, y);
		
		if(gate.getOutputToSide(ForgeDirection.EAST) || gate.getInputFromSide(ForgeDirection.EAST)) GL11.glColor3f(0F, 1F, 0F);
		else GL11.glColor3f(0F, 0.4F, 0F);
		drawTexture(3 * 16, 0, gui, x, y);

		GL11.glColor3f(0F, 1F, 0F);
		
		if(gate instanceof PartNANDGate) drawTexture(10 * 16, 0, gate.getRotation() * 90, gui, x, y);
		else if(gate instanceof PartNORGate) drawTexture(11 * 16, 0, gate.getRotation() * 90, gui, x, y);
		else if(gate instanceof PartXNORGate) drawTexture(12 * 16, 0, gate.getRotation() * 90, gui, x, y);
		else if(gate instanceof PartANDGate) drawTexture(7 * 16, 0, gate.getRotation() * 90, gui, x, y);
		else if(gate instanceof PartORGate) drawTexture(8 * 16, 0, gate.getRotation() * 90, gui, x, y);
		else if(gate instanceof PartXORGate) drawTexture(9 * 16, 0, gate.getRotation() * 90, gui, x, y);
		else if(gate instanceof PartBufferGate) drawTexture(14 * 16, 0, gate.getRotation() * 90, gui, x, y);
		else if(gate instanceof PartNOTGate) drawTexture(15 * 16, 0, gate.getRotation() * 90, gui, x, y);
		
		else if(gate instanceof PartMultiplexer) drawTexture(0, 16, gate.getRotation() * 90, gui, x, y);
		else if(gate instanceof PartTimer) drawTexture(2 * 16, 16, gate.getRotation() * 90, gui, x, y);
		else if(gate instanceof PartSequencer) drawTexture(3 * 16, 16, gate.getRotation() * 90, gui, x, y);
		else if(gate instanceof PartStateCell) drawTexture(4 * 16, 16, gate.getRotation() * 90, gui, x, y);
		else if(gate instanceof PartRandomizer) drawTexture(5 * 16, 16, gate.getRotation() * 90, gui, x, y);
		else if(gate instanceof PartSynchronizer) drawTexture(10 * 16, 16, gate.getRotation() * 90, gui, x, y);
		
		else if(gate instanceof PartRSLatch) drawTexture(7 * 16, 16, gate.getRotation() * 90, gui, x, y);
		else if(gate instanceof PartToggleLatch) drawTexture(8 * 16, 16, gate.getRotation() * 90, gui, x, y);
		else if(gate instanceof PartTranspartentLatch) drawTexture(9 * 16, 16, gate.getRotation() * 90, gui, x, y);
	}
	
	public static void renderPartTorch(PartTorch torch, Gui gui, double x, double y) 
	{
		GL11.glColor3f(0F, 1F, 0F);
		
		int con = checkConnections(torch);
		if((con & 8) > 0) drawTexture(2 * 16, 0, gui, x, y);
		if((con & 4) > 0) drawTexture(4 * 16, 0, gui, x, y);
		if((con & 2) > 0) drawTexture(1 * 16, 0, gui, x, y);
		if((con & 1) > 0) drawTexture(3 * 16, 0, gui, x, y);
		
		drawTexture(13 * 16, 0, gui, x, y);
	}
	
	public static void renderPart1I1O(PartGate gate, Gui gui, double x, double y)
	{
		if(gate.getOutputToSide(ForgeDirection.NORTH) || gate.getInputFromSide(ForgeDirection.NORTH)) GL11.glColor3f(0F, 1F, 0F);
		else GL11.glColor3f(0F, 0.4F, 0F);
		drawTexture(2 * 16, 0, gui, x, y);
		
		if(gate.getOutputToSide(ForgeDirection.SOUTH) || gate.getInputFromSide(ForgeDirection.SOUTH)) GL11.glColor3f(0F, 1F, 0F);
		else GL11.glColor3f(0F, 0.4F, 0F);
		drawTexture(4 * 16, 0, gui, x, y);
		
		GL11.glColor3f(0F, 1F, 0F);
		
		if(gate instanceof PartRepeater) drawTexture(16, 16, gate.getRotation() * 90, gui, x, y);
		else if(gate instanceof PartPulseFormer) drawTexture(6 * 16, 16, gate.getRotation() * 90, gui, x, y);
	}
	
	//Used for rendering
	public static SubLogicPart createEncapsulated(Class<? extends SubLogicPart> clazz)
	{
		try {
			return clazz.getConstructor(int.class, int.class, ICircuit.class).newInstance(1, 1, CurcuitRenderWrapper.instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static SubLogicPart createEncapsulated(Class<? extends SubLogicPart> clazz, int state)
	{
		try {
			return clazz.getConstructor(int.class, int.class, ICircuit.class).newInstance(1, 1, new CurcuitRenderWrapper(state));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static class CurcuitRenderWrapper implements ICircuit
	{
		private int[][][] matrix;
		public static CurcuitRenderWrapper instance = new CurcuitRenderWrapper(0);
		
		private CurcuitRenderWrapper(int state)
		{
			matrix = new int[][][]
			{
				new int[][]{new int[]{0, 1, 0}, new int[]{1, 0, 1}, new int[]{0, 1, 0}}, 
				new int[][]{new int[]{0, 0, 0}, new int[]{0, state, 0}, new int[]{0, 0, 0}}
			};
		}
		
		@Override
		public int[][][] getMatrix() 
		{
			return matrix;
		}

		@Override public boolean getInputFromSide(ForgeDirection dir, int frequency) { return false; }
		@Override public void setOutputToSide(ForgeDirection dir, int frequency, boolean output) {}
	}
}
