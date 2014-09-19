package vic.mod.integratedcircuits.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.ContainerPCBLayout;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.TileEntityPCBLayout;
import vic.mod.integratedcircuits.ic.CircuitData;
import vic.mod.integratedcircuits.ic.CircuitPart;
import vic.mod.integratedcircuits.ic.CircuitPart.PartANDCell;
import vic.mod.integratedcircuits.ic.CircuitPart.PartANDGate;
import vic.mod.integratedcircuits.ic.CircuitPart.PartBufferCell;
import vic.mod.integratedcircuits.ic.CircuitPart.PartBufferGate;
import vic.mod.integratedcircuits.ic.CircuitPart.PartGate;
import vic.mod.integratedcircuits.ic.CircuitPart.PartInvertCell;
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
import vic.mod.integratedcircuits.ic.CircuitPartRenderer;
import vic.mod.integratedcircuits.net.PacketPCBChangeName;
import vic.mod.integratedcircuits.net.PacketPCBChangePart;
import vic.mod.integratedcircuits.net.PacketPCBClear;
import vic.mod.integratedcircuits.net.PacketPCBIO;
import cpw.mods.fml.client.config.GuiButtonExt;

public class GuiPCBLayout extends GuiContainer
{	
	private static final ResourceLocation backgroundTexture = new ResourceLocation(IntegratedCircuits.modID, "textures/gui/pcblayout.png");
	
	private int lastX, lastY;
	private TileEntityPCBLayout te;
	
	private GuiTextField nameField;
	private GuiButtonExt buttonPlus;
	private GuiButtonExt buttonMinus;
	private GuiButtonExt buttonSize;
	
	//Because of private.
	public GuiPartChooser selectedChooser;
	public boolean blockMouseInput = false;
	public IHoverable hoveredElement;
	
	public CircuitPart selectedPart;
	
	public GuiPCBLayout(ContainerPCBLayout container) 
	{
		super(container);
		this.xSize = 248;
		this.ySize = 249;
		this.te = container.tileentity;
	}

	@Override
	public void initGui() 
	{
		this.buttonList.clear();
		int cx = (this.width - this.xSize) / 2;
		int cy = (this.height - this.ySize) / 2 - 4;
		
		GuiPartChooser c1 = new GuiPartChooser(0, cx + 220, cy + 194, 1, this);
		c1.setActive(true);
		
		buttonPlus = new GuiButtonExt(8, cx + 190, cy + 238, 10, 10, "+");
		this.buttonList.add(buttonPlus);
		buttonMinus = new GuiButtonExt(9, cx + 201, cy + 238, 10, 10, "-");
		this.buttonList.add(buttonMinus);
		
		int w = te.getCircuitData().getSize() - 2;
		this.buttonList.add(new GuiButtonExt(10, cx + 93, cy + 14, 12, 12, "+"));
		buttonSize = new GuiButtonExt(11, cx + 110, cy + 14, 38, 12, w + "x" + w);
		this.buttonList.add(buttonSize);
		
		this.buttonList.add(new GuiButtonExt(12, cx + 210, cy + 10, 10, 10, "I"));
		this.buttonList.add(new GuiButtonExt(13, cx + 210, cy + 21, 10, 10, "O"));
		
		nameField = new GuiTextField(fontRendererObj, cx + 154, cy + 15, 50, 10);
		nameField.setText(te.name);
		nameField.setMaxStringLength(7);
		nameField.setCanLoseFocus(true);
		nameField.setFocused(false);
		
		for(int i = 0; i < 16; i++)
		{
			this.buttonList.add(new GuiCircuitIO(i + 13, cx + 39 + i * 9, cy + 37, i, 0, this, te));
		}
		for(int i = 0; i < 16; i++)
		{
			this.buttonList.add(new GuiCircuitIO(i + 13 + 16, cx + 207, cy + 70 + i * 9, i, 1, this, te));
		}
		for(int i = 0; i < 16; i++)
		{
			this.buttonList.add(new GuiCircuitIO(i + 13 + 32, cx + 6, cy + 70 + i * 9, i, 3, this, te));
		}
		for(int i = 0; i < 16; i++)
		{
			this.buttonList.add(new GuiCircuitIO(i + 13 + 48, cx + 39 + i * 9, cy + 238, i, 2, this, te));
		}
		
		this.buttonList.add(c1);
		this.buttonList.add(new GuiPartChooser(1, cx + 220, cy + 215, 2, this));
		this.buttonList.add(new GuiPartChooser(2, cx + 220, cy + 131, CircuitPartRenderer.createEncapsulated(PartNullCell.class), 
			new ArrayList<CircuitPart>(Arrays.asList(
			CircuitPartRenderer.createEncapsulated(PartBufferCell.class),
			CircuitPartRenderer.createEncapsulated(PartInvertCell.class),
			CircuitPartRenderer.createEncapsulated(PartANDCell.class))), this));
		
		this.buttonList.add(new GuiPartChooser(2, cx + 220, cy + 173, CircuitPartRenderer.createEncapsulated(PartTorch.class), this));
		
		this.buttonList.add(new GuiPartChooser(3, cx + 220, cy + 152, CircuitPartRenderer.createEncapsulated(PartWire.class),
			new ArrayList<CircuitPart>(Arrays.asList(
			CircuitPartRenderer.createEncapsulated(PartWire.class, 1 << 5),
			CircuitPartRenderer.createEncapsulated(PartWire.class, 2 << 5))), this));
		
		this.buttonList.add(new GuiPartChooser(4, cx + 220, cy + 68, CircuitPartRenderer.createEncapsulated(PartToggleLatch.class),
			new ArrayList<CircuitPart>(Arrays.asList(
			CircuitPartRenderer.createEncapsulated(PartRSLatch.class),
			CircuitPartRenderer.createEncapsulated(PartTranspartentLatch.class))), this));
		
		this.buttonList.add(new GuiPartChooser(5, cx + 220, cy + 89, CircuitPartRenderer.createEncapsulated(PartANDGate.class),
			new ArrayList<CircuitPart>(Arrays.asList(
			CircuitPartRenderer.createEncapsulated(PartORGate.class),
			CircuitPartRenderer.createEncapsulated(PartXORGate.class),
			CircuitPartRenderer.createEncapsulated(PartBufferGate.class))), this));
		
		this.buttonList.add(new GuiPartChooser(6, cx + 220, cy + 110, CircuitPartRenderer.createEncapsulated(PartNANDGate.class),
			new ArrayList<CircuitPart>(Arrays.asList(
			CircuitPartRenderer.createEncapsulated(PartNORGate.class),
			CircuitPartRenderer.createEncapsulated(PartXNORGate.class),
			CircuitPartRenderer.createEncapsulated(PartNOTGate.class))), this));
		
		this.buttonList.add(new GuiPartChooser(7, cx + 220, cy + 47, CircuitPartRenderer.createEncapsulated(PartTimer.class),
			new ArrayList<CircuitPart>(Arrays.asList(
			CircuitPartRenderer.createEncapsulated(PartSequencer.class),
			CircuitPartRenderer.createEncapsulated(PartSynchronizer.class),
			CircuitPartRenderer.createEncapsulated(PartStateCell.class),
			CircuitPartRenderer.createEncapsulated(PartPulseFormer.class),
			CircuitPartRenderer.createEncapsulated(PartRandomizer.class),
			CircuitPartRenderer.createEncapsulated(PartRepeater.class),
			CircuitPartRenderer.createEncapsulated(PartMultiplexer.class))), this));

		super.initGui();
	}
	
	public void refreshUI()
	{
		int w = te.getCircuitData().getSize() - 2;
		buttonSize.displayString = w + "x" + w;
	}
	
	@Override
	protected void actionPerformed(GuiButton button) 
	{
		int w = te.getCircuitData().getSize();
		if(button.id == 8) scale(1);
		else if(button.id == 9) scale(-1);
		else if(button.id == 10)
		{
			IntegratedCircuits.networkWrapper.sendToServer(new PacketPCBClear((byte)w, te.xCoord, te.yCoord, te.zCoord));
		}
		else if(button.id == 11)
		{
			w = w == 18 ? 34 : w == 34 ? 66 : 18;
			IntegratedCircuits.networkWrapper.sendToServer(new PacketPCBClear((byte)w, te.xCoord, te.yCoord, te.zCoord));
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
		hoveredElement = null;
		
		GL11.glColor3f(1F, 1F, 1F);
		mc.getTextureManager().bindTexture(backgroundTexture);
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);	
		
		ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
		int guiScale = scaledresolution.getScaleFactor();
		
		CircuitData data = te.getCircuitData();
		
		int w = data.getSize();
		boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
		
		if(Mouse.isButtonDown(0) && (Math.abs(x - lastX) > 0 || Math.abs(y - lastY) > 0) && shiftDown)
		{
			if(!(x < guiLeft + 17 || y < guiTop + 44 || x > guiLeft + 17 + 187 || y > guiTop + 44 + 187))
			{
				te.offX += (x - lastX) / te.scale;
				te.offY += (y - lastY) / te.scale;
			}
		}
		lastX = x;
		lastY = y;
		
		double mx = (204 / te.scale) - 16;
		double ix = (-(w * 16) + 17 / te.scale) + 16;
		double my = (231 / te.scale) - 16;
		double iy = (-(w * 16) + 44 / te.scale) + 16;
		te.offX = te.offX > mx ? mx : te.offX < ix ? ix : te.offX;
		te.offY = te.offY > my ? my : te.offY < iy ? iy : te.offY;
		
		int j = this.mc.displayWidth;
		int k = this.mc.displayHeight;
		
		fontRendererObj.drawString("PCB Layout CAD", guiLeft + 8, guiTop + 12, 0x333333);
		
		GL11.glPushMatrix();
		GL11.glTranslated(guiLeft, guiTop, 0);
		mc.getTextureManager().bindTexture(new ResourceLocation(IntegratedCircuits.modID, "textures/gui/sublogicpart.png"));
		
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor((int)((guiLeft + 17) * guiScale), k - (int)((guiTop + 44) * guiScale) - 374 / 2 * guiScale, (int)(374 * guiScale / 2), (int)(374 * guiScale / 2));
		GL11.glScalef(te.scale, te.scale, 1F);
		
		CircuitPartRenderer.renderPCB(te.offX, te.offY, data);
		
		GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glColor4f(0.6F, 0.6F, 0.6F, 0.7F);
		
		double x2 = (int)((x - guiLeft - te.offX * te.scale) / 16F / te.scale);
		double y2 = (int)((y - guiTop - te.offY * te.scale) / 16F / te.scale);
		
		if(x2 > 0 && y2 > 0 && x2 < w - 1 && y2 < w - 1 && !shiftDown && !blockMouseInput)
		{
			if(!(x < guiLeft + 17 || y < guiTop + 44 || x > guiLeft + 17 + 187 || y > guiTop + 44 + 187))
			{
				x2 = x2 * 16 + te.offX;
				y2 = y2 * 16 + te.offY;
				CircuitPartRenderer.renderPart(selectedPart, x2, y2);
			}		
		}
		
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		GL11.glPopMatrix();
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glShadeModel(GL11.GL_SMOOTH); 
		Tessellator tes = Tessellator.instance;
		tes.startDrawingQuads();
		
		int i1 = guiLeft + 17, i2 = guiTop + 44, i3 = 187, i4 = 4;
		
		tes.setColorRGBA_F(0, 0, 0, 0);
		tes.addVertex(i1, i2 + i4, 0);
		tes.addVertex(i1 + i3, i2 + i4, 0);
		tes.setColorRGBA_F(0, 0, 0, 0.8F);
		tes.addVertex(i1 + i3, i2, 0);
		tes.addVertex(i1, i2, 0);
		
		tes.setColorRGBA_F(0, 0, 0, 0.8F);
		tes.addVertex(i1, i2 + i3, 0);
		tes.addVertex(i1 + i3, i2 + i3, 0);
		tes.setColorRGBA_F(0, 0, 0, 0);
		tes.addVertex(i1 + i3, i2 + i3 - i4, 0);
		tes.addVertex(i1, i2 + i3 - i4, 0);
		
		tes.setColorRGBA_F(0, 0, 0, 0.8F);
		tes.addVertex(i1, i2, 0);
		tes.addVertex(i1, i2 + i3, 0);
		tes.setColorRGBA_F(0, 0, 0, 0);
		tes.addVertex(i1 + i4, i2 + i3, 0);
		tes.addVertex(i1 + i4, i2, 0);
		
		tes.setColorRGBA_F(0, 0, 0, 0);
		tes.addVertex(i1 + i3 - i4, i2, 0);
		tes.addVertex(i1 + i3 - i4, i2 + i3, 0);
		tes.setColorRGBA_F(0, 0, 0, 0.8F);
		tes.addVertex(i1 + i3, i2 + i3, 0);
		tes.addVertex(i1 + i3, i2, 0);
		
		tes.draw();
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		nameField.drawTextBox();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) 
	{		
		CircuitData data = te.getCircuitData();
		
		int x2 = (int)((x - guiLeft - te.offX * te.scale) / (16F * te.scale));
		int y2 = (int)((y - guiTop - te.offY * te.scale) / (16F * te.scale));
		
		int w = data.getSize();
		if(x2 >= 0 && y2 >= 0 && x2 < w && y2 < w && !blockMouseInput)
		{
			if(!(x < guiLeft + 17 || y < guiTop + 44 || x > guiLeft + 17 + 187 || y > guiTop + 44 + 187))
			{
				CircuitPart part = data.getPart(x2, y2);
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
		}
		if(hoveredElement != null)
			drawHoveringText(hoveredElement.getHoverInformation(), x - guiLeft, y - guiTop, this.fontRendererObj);
		GL11.glDisable(GL11.GL_LIGHTING);
		fontRendererObj.drawString((int)(te.scale * 100) + "%", 217, 235, 0x333333);
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
		
		CircuitData data = te.getCircuitData();
		
		boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
		boolean ctrlDown = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
		int x2 = (int)((x - guiLeft - te.offX * te.scale) / (16F * te.scale));
		int y2 = (int)((y - guiTop - te.offY * te.scale) / (16F * te.scale));
		int w = data.getSize();
		
		if(x2 > 0 && y2 > 0 && x2 < w - 1 && y2 < w - 1 && !shiftDown)
		{
			if(selectedPart == null)
			{
				IntegratedCircuits.networkWrapper.sendToServer(new PacketPCBChangePart(x2, y2, 0, 0, flag, ctrlDown, te.xCoord, te.yCoord, te.zCoord));			
			}
			else IntegratedCircuits.networkWrapper.sendToServer(new PacketPCBChangePart(x2, y2, CircuitPart.getId(selectedPart), selectedPart.getState(), -1, false, te.xCoord, te.yCoord, te.zCoord));			
		}
		
		super.mouseClicked(x, y, flag);
	}
	
	private void scale(int i)
	{
		int w = te.getCircuitData().getSize();
		
		double ow = w * 16 * te.scale;	
		float oldScale = te.scale;
		
		int index = scales.indexOf(te.scale);
		
		if(i > 0 && index + 1 < scales.size()) te.scale = scales.get(index + 1);
		if(i < 0 && index - 1 >= 0) te.scale = scales.get(index - 1);
		
		if(i != 0)
		{
			buttonMinus.enabled = true;
			buttonPlus.enabled = true;
			
			if(te.scale == 0.17F) buttonMinus.enabled = false;
			if(te.scale == 2F) buttonPlus.enabled = false;
		}
		
		double change = (double)te.scale / (double)oldScale;
		double nw = w * 16 * te.scale;
				
		if(i > 0)
		{
			te.offX = te.offX / change;
			te.offY = te.offY / change;
			te.offX -= (nw - ow) / 2 / te.scale;
			te.offY -= (nw - ow) / 2 / te.scale;
		}
		else if (i < 0)
		{
			te.offX -= (nw - ow) / 2 / oldScale;
			te.offY -= (nw - ow) / 2 / oldScale;
			te.offX = te.offX / change;
			te.offY = te.offY / change;
		}
	}
	
	@Override
	public void handleMouseInput() 
	{
		scale(Mouse.getEventDWheel());
		
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
			IntegratedCircuits.networkWrapper.sendToServer(new PacketPCBChangeName(nameField.getText(), te.xCoord, te.yCoord, te.zCoord));
		}		
	}
}
