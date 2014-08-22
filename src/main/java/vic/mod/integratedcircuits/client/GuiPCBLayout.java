package vic.mod.integratedcircuits.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.ContainerPCBLayout;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.SubLogicPart;
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
import vic.mod.integratedcircuits.SubLogicPart.PartSynchronizer;
import vic.mod.integratedcircuits.SubLogicPart.PartTimer;
import vic.mod.integratedcircuits.SubLogicPart.PartToggleLatch;
import vic.mod.integratedcircuits.SubLogicPart.PartTorch;
import vic.mod.integratedcircuits.SubLogicPart.PartTranspartentLatch;
import vic.mod.integratedcircuits.SubLogicPart.PartWire;
import vic.mod.integratedcircuits.SubLogicPart.PartXNORGate;
import vic.mod.integratedcircuits.SubLogicPart.PartXORGate;
import vic.mod.integratedcircuits.SubLogicPartRenderer;
import vic.mod.integratedcircuits.TileEntityPCBLayout;
import vic.mod.integratedcircuits.net.PacketPCBChangeName;
import vic.mod.integratedcircuits.net.PacketPCBChangePart;
import vic.mod.integratedcircuits.net.PacketPCBIO;
import vic.mod.integratedcircuits.net.PacketPCBReload;
import cpw.mods.fml.client.config.GuiButtonExt;

public class GuiPCBLayout extends GuiContainer
{	
	private static final ResourceLocation backgroundTexture = new ResourceLocation(IntegratedCircuits.modID, "textures/gui/pcblayout.png");
	
	private float scale = 0.33F;
	private double offX = 63;
	private double offY = 145;
	private int lastX, lastY;
	private boolean offsetChanged;
	
	private GuiTextField nameField;
	private GuiButtonExt buttonPlus;
	private GuiButtonExt buttonMinus;
	
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
				SubLogicPartRenderer.createEncapsulated(PartSynchronizer.class),
				SubLogicPartRenderer.createEncapsulated(PartStateCell.class),
				SubLogicPartRenderer.createEncapsulated(PartPulseFormer.class),
				SubLogicPartRenderer.createEncapsulated(PartRandomizer.class),
				SubLogicPartRenderer.createEncapsulated(PartRepeater.class),
				SubLogicPartRenderer.createEncapsulated(PartMultiplexer.class))), this));
		
		buttonPlus = new GuiButtonExt(8, cx + 190, cy + 238, 10, 10, "+");
		this.buttonList.add(buttonPlus);
		buttonMinus = new GuiButtonExt(9, cx + 201, cy + 238, 10, 10, "-");
		this.buttonList.add(buttonMinus);
		
		TileEntityPCBLayout te = ((ContainerPCBLayout)inventorySlots).tileentity;
		int w = te.getMatrix()[0].length - 2;
		this.buttonList.add(new GuiButtonExt(10, cx + 93, cy + 14, 12, 12, "+"));
		this.buttonList.add(new GuiButtonExt(11, cx + 110, cy + 14, 38, 12, w + "x" + w));
		
		this.buttonList.add(new GuiButtonExt(12, cx + 210, cy + 10, 10, 10, "I"));
		this.buttonList.add(new GuiButtonExt(13, cx + 210, cy + 21, 10, 10, "O"));
		
		nameField = new GuiTextField(fontRendererObj, cx + 154, cy + 15, 50, 10);
		nameField.setText(te.name);
		nameField.setMaxStringLength(7);
		nameField.setCanLoseFocus(true);
		nameField.setFocused(false);
		
		super.initGui();
	}
	
	@Override
	protected void actionPerformed(GuiButton button) 
	{
		TileEntityPCBLayout te = ((ContainerPCBLayout)inventorySlots).tileentity;
		int w = te.getMatrix()[0].length;
		if(button.id == 8) scale(1);
		else if(button.id == 9) scale(-1);
		else if(button.id == 10)
		{
			IntegratedCircuits.networkWrapper.sendToServer(new PacketPCBReload((byte)w, te.xCoord, te.yCoord, te.zCoord));
		}
		else if(button.id == 11)
		{
			w = w == 18 ? 34 : w == 34 ? 66 : 18;
			IntegratedCircuits.networkWrapper.sendToServer(new PacketPCBReload((byte)w, te.xCoord, te.yCoord, te.zCoord));
		}
		else if(button.id == 13)
			IntegratedCircuits.networkWrapper.sendToServer(new PacketPCBIO(true, te.xCoord, te.yCoord, te.zCoord));	
		else if(button.id == 12)
			IntegratedCircuits.networkWrapper.sendToServer(new PacketPCBIO(false, te.xCoord, te.yCoord, te.zCoord));	
	}
	
	public List getButtonList()
	{
		return buttonList;
	}
	
	@Override
	public void updateScreen() 
	{
		nameField.updateCursorCounter();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int x, int y) 
	{
		hoveredChooser = null;
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(backgroundTexture);
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
		int guiScale = scaledresolution.getScaleFactor();
		
		int[][][] matrix = ((ContainerPCBLayout)inventorySlots).tileentity.getMatrix();
		int w = matrix[0].length;
		boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
		
		if(Mouse.isButtonDown(0) && (Math.abs(x - lastX) > 0 || Math.abs(y - lastY) > 0) && shiftDown)
		{
			if(!(x < guiLeft + 17 || y < guiTop + 44 || x > guiLeft + 17 + 187 || y > guiTop + 44 + 187))
			{
				offX += (x - lastX) / scale;
				offY += (y - lastY) / scale;
			}
		}
		lastX = x;
		lastY = y;
		
		double mx = (204 / scale) - 16;
		double ix = (-(w * 16) + 17 / scale) + 16;
		double my = (231 / scale) - 16;
		double iy = (-(w * 16) + 44 / scale) + 16;
		offX = offX > mx ? mx : offX < ix ? ix : offX;
		offY = offY > my ? my : offY < iy ? iy : offY;
		
		int j = this.mc.displayWidth;
		int k = this.mc.displayHeight;
		
		fontRendererObj.drawString("PCB Layout CAD", guiLeft + 8, guiTop + 12, 0x333333);
		
		GL11.glPushMatrix();
		GL11.glTranslated(guiLeft, guiTop, 0);
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor((int)((guiLeft + 17) * guiScale), k - (int)((guiTop + 44) * guiScale) - 374 / 2 * guiScale, (int)(374 * guiScale / 2), (int)(374 * guiScale / 2));
		GL11.glScalef(scale, scale, 1F);
		GL11.glTranslated(offX, offY, 0);
		
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
				SubLogicPart part = SubLogicPart.getPart(x2, y2, ((ContainerPCBLayout)inventorySlots).tileentity);
				SubLogicPartRenderer.renderPart(part, this, x2 * 16, y2 * 16);
			}
		}
		
		GL11.glTranslated(-offX, -offY, 0);
		GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glColor4f(0.6F, 0.6F, 0.6F, 0.7F);
		
		double x2 = (int)((x - guiLeft - offX * scale) / 16F / scale);
		double y2 = (int)((y - guiTop - offY * scale) / 16F / scale);
		
		if(x2 > 0 && y2 > 0 && x2 < w - 1 && y2 < w - 1 && !shiftDown && !blockMouseInput)
		{
			if(!(x < guiLeft + 17 || y < guiTop + 44 || x > guiLeft + 17 + 187 || y > guiTop + 44 + 187))
			{
				x2 = x2 * 16 + offX;
				y2 = y2 * 16 + offY;
				SubLogicPartRenderer.renderPart(selectedPart, this, x2, y2);
			}		
		}
		
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		GL11.glPopMatrix();
		
		nameField.drawTextBox();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) 
	{		
		int x2 = (int)((x - guiLeft - offX * scale) / (16F * scale));
		int y2 = (int)((y - guiTop - offY * scale) / (16F * scale));
		int w = ((ContainerPCBLayout)inventorySlots).tileentity.getMatrix()[0].length;
		if(x2 > 0 && y2 > 0 && x2 < w && y2 < w)
		{
			SubLogicPart part = SubLogicPart.getPart(x2, y2,((ContainerPCBLayout)inventorySlots).tileentity);
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
		GL11.glDisable(GL11.GL_LIGHTING);
		fontRendererObj.drawString((int)(scale * 100) + "%", 217, 235, 0x333333);
	}

	@Override
	protected void mouseClicked(int x, int y, int flag) 
	{
		nameField.mouseClicked(x, y, flag);
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
		
		int[][][] matrix = ((ContainerPCBLayout)inventorySlots).tileentity.getMatrix();
		boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
		boolean crtlDown = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
		int x2 = (int)((x - guiLeft - offX * scale) / (16F * scale));
		int y2 = (int)((y - guiTop - offY * scale) / (16F * scale));
		int w = matrix[0].length;
		
		if(x2 > 0 && y2 > 0 && x2 < w - 1 && y2 < w - 1 && !shiftDown)
		{
			int pid = matrix[0][x2][y2];
			int pdata = matrix[1][x2][y2];
			
			if(selectedPart == null)
			{
				SubLogicPart part = SubLogicPart.getPart(x2, y2, ((ContainerPCBLayout)inventorySlots).tileentity);
				part.onClick(flag, crtlDown);
			}
			else SubLogicPart.setPart(x2, y2, ((ContainerPCBLayout)inventorySlots).tileentity, selectedPart);
			
			int cid = matrix[0][x2][y2];
			int cdata = matrix[1][x2][y2];
			
			if(pid != cid || pdata != cdata)
			{
				TileEntityPCBLayout te = ((ContainerPCBLayout)inventorySlots).tileentity;
				IntegratedCircuits.networkWrapper.sendToServer(new PacketPCBChangePart(x2, y2, cid, cdata, te.xCoord, te.yCoord, te.zCoord));
			}			
		}
		
		super.mouseClicked(x, y, flag);
	}
	
	private void scale(int i)
	{
		int index = scales.indexOf(scale);
		
		if(i > 0 && index + 1 < scales.size()) scale = scales.get(index + 1);
		if(i < 0 && index - 1 >= 0) scale = scales.get(index - 1);
		
		if(i != 0)
		{
			buttonMinus.enabled = true;
			buttonPlus.enabled = true;
			
			if(scale == 0.17F) buttonMinus.enabled = false;
			if(scale == 2F) buttonPlus.enabled = false;
		}	
	}
	
	@Override
	public void handleMouseInput() 
	{
		int w = ((ContainerPCBLayout)inventorySlots).tileentity.getMatrix()[0].length;
		double ow = w * 16 * scale;	
		int i = Mouse.getEventDWheel();
		float oldScale = scale;
		
		scale(i);
		
		double change = (double)scale / (double)oldScale;
		double nw = w * 16 * scale;
				
		if(i > 0)
		{
			offX = offX / change;
			offY = offY / change;
			offX -= (nw - ow) / 2 / scale;
			offY -= (nw - ow) / 2 / scale;
		}
		else if (i < 0)
		{
			offX -= (nw - ow) / 2 / oldScale;
			offY -= (nw - ow) / 2 / oldScale;
			offX = offX / change;
			offY = offY / change;
		}
		
		super.handleMouseInput();
	}
	
	private static List<Float> scales = Arrays.asList(0.17F, 0.2F, 0.25F, 0.33F, 0.5F, 0.67F, 1F, 1.5F, 2F);

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

	@Override
	protected void keyTyped(char par1, int par2) 
	{
		String oname = nameField.getText();
		if(nameField.isFocused()) nameField.textboxKeyTyped(par1, par2);
		else super.keyTyped(par1, par2);
		
		if(!oname.equals(nameField.getText()))
		{
			TileEntityPCBLayout te = ((ContainerPCBLayout)inventorySlots).tileentity;
			IntegratedCircuits.networkWrapper.sendToServer(new PacketPCBChangeName(nameField.getText(), te.xCoord, te.yCoord, te.zCoord));
		}		
	}
	
	
}
