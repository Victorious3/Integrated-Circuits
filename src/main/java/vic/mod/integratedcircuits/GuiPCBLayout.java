package vic.mod.integratedcircuits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.SubLogicPart.PartANDGate;
import vic.mod.integratedcircuits.SubLogicPart.PartBufferGate;
import vic.mod.integratedcircuits.SubLogicPart.PartGate;
import vic.mod.integratedcircuits.SubLogicPart.PartMultiplexer;
import vic.mod.integratedcircuits.SubLogicPart.PartNANDGate;
import vic.mod.integratedcircuits.SubLogicPart.PartNORGate;
import vic.mod.integratedcircuits.SubLogicPart.PartNOTGate;
import vic.mod.integratedcircuits.SubLogicPart.PartNull;
import vic.mod.integratedcircuits.SubLogicPart.PartNullCell;
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

public class GuiPCBLayout extends GuiContainer
{	
	private static final ResourceLocation backgroundTexture = new ResourceLocation(IntegratedCircuits.modID, "textures/gui/pcblayout.png");
	
	private float scale = 1F;
	private float offX = 20;
	private float offY = 20;
	private int lastX, lastY;
	private boolean offsetChanged;
	
	//Because of private.
	public GuiPartChooser selectedChooser;
	public boolean blockMouseInput = false;
	public GuiPartChooser hoveredChooser;
	
	public SubLogicPart selectedPart;
	
	public GuiPCBLayout(ContainerPCBLayout container) 
	{
		super(container);
		this.xSize = 248;
		this.ySize = 249;
	}

	@Override
	public void initGui() 
	{
		this.buttonList.clear();
		int cx = (this.width - this.xSize) / 2;
		int cy = (this.height - this.ySize) / 2 - 4;
		
		GuiPartChooser c1 = new GuiPartChooser(0, cx + 220, cy + 194, 1, this);
		c1.setActive(true);
		this.buttonList.add(c1);
		this.buttonList.add(new GuiPartChooser(1, cx + 220, cy + 215, 2, this));
		this.buttonList.add(new GuiPartChooser(2, cx + 220, cy + 152, SubLogicPartRenderer.createEncapsulated(PartNullCell.class), this));
		this.buttonList.add(new GuiPartChooser(2, cx + 220, cy + 173, SubLogicPartRenderer.createEncapsulated(PartTorch.class), this));
		
		this.buttonList.add(new GuiPartChooser(3, cx + 220, cy + 131, SubLogicPartRenderer.createEncapsulated(PartWire.class),
			new ArrayList<SubLogicPart>(Arrays.asList(
			SubLogicPartRenderer.createEncapsulated(PartWire.class, 1 << 5), 
			SubLogicPartRenderer.createEncapsulated(PartWire.class, 2 << 5))), this));
		
		this.buttonList.add(new GuiPartChooser(4, cx + 220, cy + 68, SubLogicPartRenderer.createEncapsulated(PartToggleLatch.class),
				new ArrayList<SubLogicPart>(Arrays.asList(
				SubLogicPartRenderer.createEncapsulated(PartRSLatch.class), 
				SubLogicPartRenderer.createEncapsulated(PartTranspartentLatch.class))), this));
		
		this.buttonList.add(new GuiPartChooser(5, cx + 220, cy + 89, SubLogicPartRenderer.createEncapsulated(PartANDGate.class),
				new ArrayList<SubLogicPart>(Arrays.asList(
				SubLogicPartRenderer.createEncapsulated(PartORGate.class), 
				SubLogicPartRenderer.createEncapsulated(PartXORGate.class),
				SubLogicPartRenderer.createEncapsulated(PartBufferGate.class))), this));
		
		this.buttonList.add(new GuiPartChooser(6, cx + 220, cy + 110, SubLogicPartRenderer.createEncapsulated(PartNANDGate.class),
				new ArrayList<SubLogicPart>(Arrays.asList(
				SubLogicPartRenderer.createEncapsulated(PartNORGate.class), 
				SubLogicPartRenderer.createEncapsulated(PartXNORGate.class),
				SubLogicPartRenderer.createEncapsulated(PartNOTGate.class))), this));
		
		this.buttonList.add(new GuiPartChooser(7, cx + 220, cy + 47, SubLogicPartRenderer.createEncapsulated(PartTimer.class),
				new ArrayList<SubLogicPart>(Arrays.asList(
				SubLogicPartRenderer.createEncapsulated(PartSequencer.class), 
				SubLogicPartRenderer.createEncapsulated(PartStateCell.class),
				SubLogicPartRenderer.createEncapsulated(PartPulseFormer.class),
				SubLogicPartRenderer.createEncapsulated(PartRandomizer.class),
				SubLogicPartRenderer.createEncapsulated(PartRepeater.class),
				SubLogicPartRenderer.createEncapsulated(PartMultiplexer.class))), this));
		
		super.initGui();
	}
	
	public List getButtonList()
	{
		return buttonList;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int x, int y) 
	{
		hoveredChooser = null;
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(backgroundTexture);
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
		float guiScale = scaledresolution.getScaleFactor() * 0.5F;
		
		int[][][] matrix = ((ICircuit)inventorySlots).getMatrix();
		boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
		
		if(Mouse.isButtonDown(0) && (Math.abs(x - lastX) > 0 || Math.abs(y - lastY) > 0) && shiftDown)
		{
			offX += ((float)(x - lastX)) / scale;
			offY += ((float)(y - lastY)) / scale;
		}
		lastX = x;
		lastY = y;
		
		int j = Minecraft.getMinecraft().displayWidth;
		int k = Minecraft.getMinecraft().displayHeight;
		
		drawCenteredString(fontRendererObj, "PCB Layout CAD", width / 2, guiTop + 10, 0xFFFFFF);
		
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(j / 2 - (int)(213 * guiScale), k / 2 - (int)(213 * guiScale), (int)(374 * guiScale), (int)(374 * guiScale));
		GL11.glScalef(scale, scale, 1F);
		GL11.glTranslatef(offX, offY, 0);
		
		for(int x2 = 0; x2 < matrix[0].length; x2++)
		{
			int[] i1 = matrix[0][x2];
			for(int y2 = 0; y2 < i1.length; y2++)
			{
				GL11.glColor3f(1.0F, 1.0F, 1.0F);
				mc.getTextureManager().bindTexture(new ResourceLocation(IntegratedCircuits.modID, "textures/gui/sublogicpart.png"));
				if(x2 == 0 || y2 == 0 || x2 == matrix[0].length - 1 || y2 == i1.length - 1) 
					SubLogicPartRenderer.drawTexture(16, 15 * 16, this, x2 * 16, y2 * 16);
				else SubLogicPartRenderer.drawTexture(0, 15 * 16, this, x2 * 16, y2 * 16);
				SubLogicPart part = SubLogicPart.getPart(x2, y2, (ICircuit)inventorySlots);
				SubLogicPartRenderer.renderPart(part, this, x2 * 16, y2 * 16);
			}
		}
		
		GL11.glTranslatef(-offX, -offY, 0);
		GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glColor4f(0.6F, 0.6F, 0.6F, 0.7F);
		
		int x2 = (int)((x - offX * scale) / 16F / scale);
		int y2 = (int)((y - offY * scale) / 16F / scale);
		int w = ((ICircuit)inventorySlots).getMatrix()[0].length;
		
		if(x2 > 0 && y2 > 0 && x2 < w - 1 && y2 < w - 1 && !shiftDown && !blockMouseInput)
		{
			if(!(x < guiLeft + 17 || y < guiTop + 44 || x > guiLeft + 17 + 187 || y > guiTop + 44 + 187))
			{
				x2 = (int)(x2 * 16 + offX);
				y2 = (int)(y2 * 16 + offY);
				SubLogicPartRenderer.renderPart(selectedPart, this, x2, y2);
			}		
		}
		
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		GL11.glPopMatrix();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) 
	{		
		int x2 = (int)((x - offX * scale) / (16F * scale));
		int y2 = (int)((y - offY * scale) / (16F * scale));
		int w = ((ICircuit)inventorySlots).getMatrix()[0].length;
		if(x2 > 0 && y2 > 0 && x2 < w && y2 < w)
		{
			SubLogicPart part = SubLogicPart.getPart(x2, y2, (ICircuit)inventorySlots);
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
				drawHoveringText(text, x - guiLeft, y - guiTop, this.fontRendererObj);
			}
		}
		if(hoveredChooser != null)
		{
			String text = "";
			if(hoveredChooser.current != null) text = hoveredChooser.current.getName();
			else if(hoveredChooser.mode == 1) text = "Edit";
			else if(hoveredChooser.mode == 2) text = "Erase";
			
			drawCreativeTabHoveringText(text, x - guiLeft, y - guiTop);
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int flag) 
	{
		if(blockMouseInput) 
		{
			super.mouseClicked(x, y, flag);
			return;
		}	
		if(x < guiLeft + 17 || y < guiTop + 44 || x > guiLeft + 17 + 187 || y > guiTop + 44 + 187)
		{
			super.mouseClicked(x, y, flag);
			return;
		}		
		
		boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
		boolean crtlDown = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
		int x2 = (int)((x - offX * scale) / (16F * scale));
		int y2 = (int)((y - offY * scale) / (16F * scale));
		int w = ((ICircuit)inventorySlots).getMatrix()[0].length;
		
		if(x2 > 0 && y2 > 0 && x2 < w - 1 && y2 < w - 1 && !shiftDown)
		{
			if(selectedPart == null)
			{
				SubLogicPart part = SubLogicPart.getPart(x2, y2, (ICircuit)inventorySlots);
				part.onClick(flag, crtlDown);
			}
			else SubLogicPart.setPart(x2, y2, (ICircuit)inventorySlots, selectedPart);
		}
		
		super.mouseClicked(x, y, flag);
	}
	
	@Override
	public void handleMouseInput() 
	{
		int i = Mouse.getEventDWheel();
		float change = scale * 0.1F;
		if(i > 0) scale += change;
		else if(i < 0) scale -= change;
		scale = MathHelper.clamp_float(scale, 0.25F, 2F);

		super.handleMouseInput();
	}

	@Override
	protected void mouseMovedOrUp(int x, int y, int button) 
	{
	    super.mouseMovedOrUp(x, y, button);
		if(this.selectedChooser != null && button == 0)
	    {
			this.selectedChooser.mouseReleased(x, y);
			this.selectedChooser = null;
	    }
	}
}
