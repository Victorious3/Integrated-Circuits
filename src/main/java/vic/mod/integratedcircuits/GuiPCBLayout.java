package vic.mod.integratedcircuits;

import java.util.ArrayList;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.SubLogicPart.PartGate;
import vic.mod.integratedcircuits.SubLogicPart.PartNull;
import vic.mod.integratedcircuits.SubLogicPart.PartWire;

public class GuiPCBLayout extends GuiContainer
{
	public GuiPCBLayout(ContainerPCBLayout container) 
	{
		super(container);
	}
	
	private float scale = 0.5F;
	private int offX = 0;
	private int offY = 0;

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int x, int y) 
	{		
		int[][][] matrix = ((ICircuit)inventorySlots).getMatrix();
		
		matrix[0][1][1] = 1;
		matrix[0][2][1] = 1;
		matrix[0][3][1] = 1;
		
		matrix[0][4][1] = 2;
		
		matrix[0][4][2] = 1;
		matrix[0][3][3] = 1;
		matrix[0][2][3] = 1;
		matrix[0][4][3] = 1;
		matrix[0][5][3] = 1;
		
		matrix[0][6][3] = 9;
		matrix[1][6][3] = matrix[1][6][3] | 0 << 4;
		
		matrix[0][6][2] = 1;
		matrix[0][6][1] = 1;
		matrix[0][6][4] = 2;
		matrix[0][7][3] = 1;
		
		matrix[0][10][3] = 11;
		matrix[0][10][4] = 2;
		
		SubLogicPart.getPart(4, 1, (ICircuit)inventorySlots).onPlaced();
		SubLogicPart.getPart(6, 4, (ICircuit)inventorySlots).onPlaced();
		SubLogicPart.getPart(7, 3, (ICircuit)inventorySlots).onPlaced();
		SubLogicPart.getPart(10, 4, (ICircuit)inventorySlots).onPlaced();

		SubLogicPart.getPart(6, 3, (ICircuit)inventorySlots).onUpdateTick();
		SubLogicPart.getPart(10, 3, (ICircuit)inventorySlots).onUpdateTick();
		
		
		GL11.glPushMatrix();
		GL11.glScalef(scale, scale, 1F);	
		GL11.glTranslatef(offX, offY, 0);
		
		for(int x2 = 0; x2 < matrix[0].length; x2++)
		{
			int[] i1 = matrix[0][x2];
			for(int y2 = 0; y2 < i1.length; y2++) SubLogicPartRenderer.renderPart(SubLogicPart.getPart(x2, y2, (ICircuit)inventorySlots), this);
		}
		GL11.glPopMatrix();
		
		int x3 = (int)((x + offX) / (16F * scale));
		int y3 = (int)((y + offY) / (16F * scale));
		int w = ((ICircuit)inventorySlots).getMatrix()[0].length;
		if(x3 > 0 && y3 > 0 && x3 < w && y3 < w)
		{
			SubLogicPart part = SubLogicPart.getPart(x3, y3, (ICircuit)inventorySlots);
			if(!(part instanceof PartNull || part instanceof PartWire))
			{
				ArrayList<String> text = new ArrayList<String>();
				text.add(part.getName());
				if(part instanceof PartGate) 
				{
					int rotation = ((PartGate)part).getRotation();
					ForgeDirection rot = 
							rotation == 0 ? ForgeDirection.NORTH : 
							rotation == 1 ? ForgeDirection.EAST : 
							rotation == 2 ? ForgeDirection.SOUTH : 
							ForgeDirection.WEST;
					text.add(EnumChatFormatting.DARK_GRAY + "" + EnumChatFormatting.ITALIC + rot.toString());
				}
				text.addAll(part.getInformation());
				drawHoveringText(text, x, y, this.fontRendererObj);
			}
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int flag) 
	{
		boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
		int x2 = (int)((x + offX) / (16F * scale));
		int y2 = (int)((y + offY) / (16F * scale));
		int w = ((ICircuit)inventorySlots).getMatrix()[0].length;
		
		if(x2 > 0 && y2 > 0 && x2 < w && y2 < w)
		{
			SubLogicPart part = SubLogicPart.getPart(x2, y2, (ICircuit)inventorySlots);
			part.onClick(flag, shiftDown);
		}
		
		super.mouseClicked(x, y, flag);	
	}
}
