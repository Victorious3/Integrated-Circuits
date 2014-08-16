package vic.mod.integratedcircuits;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.SubLogicPart.PartGate;
import vic.mod.integratedcircuits.SubLogicPart.PartNull;
import vic.mod.integratedcircuits.SubLogicPart.PartNullCell;
import vic.mod.integratedcircuits.SubLogicPart.PartRepeater;
import vic.mod.integratedcircuits.SubLogicPart.PartWire;

public class GuiPCBLayout extends GuiContainer
{
	public GuiPCBLayout(ContainerPCBLayout container) 
	{
		super(container);
	}
	
	private float scale = 1F;
	private int offX = 20;
	private int offY = 20;
	
	private SubLogicPart selectedPart = SubLogicPartRenderer.createEncapsulated(PartRepeater.class);

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
		
		matrix[0][6][3] = 22;
		
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
			for(int y2 = 0; y2 < i1.length; y2++)
			{
				GL11.glColor3f(1.0F, 1.0F, 1.0F);
				Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(IntegratedCircuits.modID, "textures/gui/sublogicpart.png"));
				SubLogicPartRenderer.drawTexture(0, 15 * 16, this, x2 * 16, y2 * 16);
				SubLogicPart part = SubLogicPart.getPart(x2, y2, (ICircuit)inventorySlots);
				SubLogicPartRenderer.renderPart(part, this, x2 * 16, y2 * 16);
			}
		}
		
		GL11.glTranslatef(-offX, -offY, 0);
		GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glColor4f(0.6F, 0.6F, 0.6F, 0.7F);
		
		int x2 = (int)((x - offX * scale) / 16F * 1 / scale);
		int y2 = (int)((y - offY * scale) / 16F * 1 / scale);
		int w = ((ICircuit)inventorySlots).getMatrix()[0].length;

		if(x2 <= 0 || y2 <= 0 || x2 >= w - 1 || y2 >= w - 1)
		{
			x2 = (int)((x - 8 * scale) * (1 / scale));
			y2 = (int)((y - 8 * scale) * (1 / scale));
		}
		else
		{
			x2 = x2 * 16 + offX;
			y2 = y2 * 16 + offY;
		}
		SubLogicPartRenderer.renderPart(selectedPart, this, x2, y2);
		GL11.glPopMatrix();
		
		int x3 = (int)((x - offX * scale) / (16F * scale));
		int y3 = (int)((y - offY * scale) / (16F * scale));
		if(x3 > 0 && y3 > 0 && x3 < w && y3 < w)
		{
			SubLogicPart part = SubLogicPart.getPart(x3, y3, (ICircuit)inventorySlots);
			if(!(part instanceof PartNull || part instanceof PartWire || part instanceof PartNullCell))
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
		int x2 = (int)((x - offX * scale) / (16F * scale));
		int y2 = (int)((y - offY * scale) / (16F * scale));
		int w = ((ICircuit)inventorySlots).getMatrix()[0].length;
		
		if(x2 > 0 && y2 > 0 && x2 < w && y2 < w)
		{
			if(selectedPart == null)
			{
				SubLogicPart part = SubLogicPart.getPart(x2, y2, (ICircuit)inventorySlots);
				part.onClick(flag, shiftDown);
			}
			else SubLogicPart.setPart(x2, y2, (ICircuit)inventorySlots, selectedPart.getClass());
		}
		
		super.mouseClicked(x, y, flag);	
	}
	
	@Override
	public void handleMouseInput() 
	{
		int i = Mouse.getEventDWheel();
		if(i > 0) scale += scale * 0.1F;
		else if(i < 0) scale -= scale * 0.1F;
		scale = MathHelper.clamp_float(scale, 0.25F, 2F);
		
		int xchange = Mouse.getDX();
		int ychange = Mouse.getDY();
		
		System.out.println(xchange + " " + ychange);
		
		super.handleMouseInput();
	}
}
