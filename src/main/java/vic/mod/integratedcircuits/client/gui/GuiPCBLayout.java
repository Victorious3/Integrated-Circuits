package vic.mod.integratedcircuits.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.Config;
import vic.mod.integratedcircuits.ContainerPCBLayout;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.Resources;
import vic.mod.integratedcircuits.client.gui.GuiCallback.Action;
import vic.mod.integratedcircuits.client.gui.GuiInterfaces.IGuiCallback;
import vic.mod.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import vic.mod.integratedcircuits.client.gui.GuiInterfaces.IHoverableHandler;
import vic.mod.integratedcircuits.ic.CircuitData;
import vic.mod.integratedcircuits.ic.CircuitPart;
import vic.mod.integratedcircuits.ic.CircuitPartRenderer;
import vic.mod.integratedcircuits.ic.CircuitPartRenderer.CircuitRenderWrapper;
import vic.mod.integratedcircuits.ic.part.PartCPGate;
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
import vic.mod.integratedcircuits.ic.part.timed.IConfigurableDelay;
import vic.mod.integratedcircuits.ic.part.timed.PartPulseFormer;
import vic.mod.integratedcircuits.ic.part.timed.PartRandomizer;
import vic.mod.integratedcircuits.ic.part.timed.PartRepeater;
import vic.mod.integratedcircuits.ic.part.timed.PartSequencer;
import vic.mod.integratedcircuits.ic.part.timed.PartStateCell;
import vic.mod.integratedcircuits.ic.part.timed.PartTimer;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.misc.Vec2;
import vic.mod.integratedcircuits.net.PacketPCBChangeName;
import vic.mod.integratedcircuits.net.PacketPCBChangePart;
import vic.mod.integratedcircuits.net.PacketPCBClear;
import vic.mod.integratedcircuits.net.PacketPCBIO;
import vic.mod.integratedcircuits.tile.TileEntityPCBLayout;
import cpw.mods.fml.client.config.GuiButtonExt;

public class GuiPCBLayout extends GuiContainer implements IGuiCallback, IHoverableHandler
{	
	private int lastX, lastY;
	public TileEntityPCBLayout te;
	
	private GuiTextField nameField;
	private GuiButtonExt buttonPlus;
	private GuiButtonExt buttonMinus;
	private GuiButtonExt buttonSize;
	private GuiIOMode checkN;
	private GuiIOMode checkE;
	private GuiIOMode checkS;
	private GuiIOMode checkW;
	
	//Because of private.
	public GuiPartChooser selectedChooser;
	public boolean blockMouseInput = false;
	private IHoverable hoveredElement;
	
	public CircuitRenderWrapper selectedPart;
	
	//Used by the wires
	private int sx, sy, ex, ey;
	private boolean drag;
	
	//Callbacks
	private GuiCallback callbackDelete;
	private GuiCheckBoxExt checkboxDelete;
	private GuiCallback callbackTimed;
	private GuiLabel labelTimed;
	private CircuitRenderWrapper timedPart;
	
	public GuiPCBLayout(ContainerPCBLayout container) 
	{
		super(container);
		this.xSize = 248;
		this.ySize = 249;
		this.te = container.tileentity;
		
		callbackDelete = new GuiCallback(this, 150, 100, Action.OK, Action.CANCEL);
		checkboxDelete = new GuiCheckBoxExt(1, 7, 78, null, Config.showConfirmMessage.getBoolean(), I18n.format("gui.integratedcircuits.cad.callback.show"), callbackDelete);
		callbackDelete
			.addControl(new GuiLabel(75, 7, I18n.format("gui.integratedcircuits.cad.callback.confirm"), 0x333333, true))
			.addControl(new GuiLabel(75, 25, I18n.format("gui.integratedcircuits.cad.callback.message").replaceAll("\\\\n", "\n"), 0, true))
			.addControl(new GuiLabel(75, 63, I18n.format("gui.integratedcircuits.cad.callback.continue"), 0x333333, true))
			.addControl(checkboxDelete);

		labelTimed = new GuiLabel(80, 9, "", 0, true);
		callbackTimed = new GuiCallback(this, 160, 50)
			.addControl(new GuiButtonExt(1, 5, 25, 36, 20, "-1s"))
			.addControl(new GuiButtonExt(2, 43, 25, 36, 20, "-50ms"))
			.addControl(new GuiButtonExt(3, 81, 25, 36, 20, "+50ms"))
			.addControl(new GuiButtonExt(4, 119, 25, 36, 20, "+1s"))
			.addControl(labelTimed);
	}

	@Override
	public void initGui()
	{
		int cx = (this.width - this.xSize) / 2;
		int cy = (this.height - this.ySize) / 2 - 4;
		
		GuiPartChooser c1 = new GuiPartChooser(0, cx + 220, cy + 194, 1, this);
		c1.setActive(true);
		
		buttonPlus = new GuiButtonExt(8, cx + 190, cy + 238, 10, 10, "+");
		this.buttonList.add(buttonPlus);
		buttonMinus = new GuiButtonExt(9, cx + 201, cy + 238, 10, 10, "-");
		this.buttonList.add(buttonMinus);
		
		this.buttonList.add(new GuiButtonExt(10, cx + 93, cy + 14, 12, 12, "+"));
		buttonSize = new GuiButtonExt(11, cx + 110, cy + 14, 38, 12, "");
		this.buttonList.add(buttonSize);
		
		this.buttonList.add(new GuiButtonExt(12, cx + 210, cy + 10, 10, 10, "I"));
		this.buttonList.add(new GuiButtonExt(13, cx + 210, cy + 21, 10, 10, "O"));
		
		checkN = new GuiIOMode(65, cx + 26, cy + 35, this, 0);
		checkE = new GuiIOMode(66, cx + 206, cy + 57, this, 1);
		checkS = new GuiIOMode(67, cx + 26, cy + 237, this, 2);
		checkW = new GuiIOMode(68, cx + 4, cy + 57, this, 3);
		
		this.buttonList.add(checkN);
		this.buttonList.add(checkE);
		this.buttonList.add(checkS);
		this.buttonList.add(checkW);
		
		nameField = new GuiTextField(fontRendererObj, cx + 154, cy + 15, 50, 10);
		nameField.setText(te.getCircuitData().getProperties().getName());
		nameField.setMaxStringLength(7);
		nameField.setCanLoseFocus(true);
		nameField.setFocused(false);
		
		for(int i = 0; i < 16; i++)
			this.buttonList.add(new GuiIO(i + 13, cx + 39 + i * 9, cy + 37, 15 - i, 0, this, te));
		for(int i = 0; i < 16; i++)
			this.buttonList.add(new GuiIO(i + 13 + 16, cx + 207, cy + 70 + i * 9, 15 - i, 1, this, te));
		for(int i = 0; i < 16; i++)
			this.buttonList.add(new GuiIO(i + 13 + 32, cx + 6, cy + 70 + i * 9, i, 3, this, te));
		for(int i = 0; i < 16; i++)
			this.buttonList.add(new GuiIO(i + 13 + 48, cx + 39 + i * 9, cy + 238, i, 2, this, te));
		
		this.buttonList.add(c1);
		this.buttonList.add(new GuiPartChooser(1, cx + 220, cy + 215, 2, this));
		this.buttonList.add(new GuiPartChooser(2, cx + 220, cy + 131, new CircuitRenderWrapper(PartNullCell.class), Arrays.asList(
			new CircuitRenderWrapper(PartBufferCell.class),
			new CircuitRenderWrapper(PartInvertCell.class),
			new CircuitRenderWrapper(PartANDCell.class)), this));

		this.buttonList.add(new GuiPartChooser(2, cx + 220, cy + 173, new CircuitRenderWrapper(PartTorch.class), this));
		
		this.buttonList.add(new GuiPartChooser(3, cx + 220, cy + 152, new CircuitRenderWrapper(PartWire.class), Arrays.asList(
			new CircuitRenderWrapper(PartWire.class, 1 << 5),
			new CircuitRenderWrapper(PartWire.class, 2 << 5)), this));
		
		this.buttonList.add(new GuiPartChooser(4, cx + 220, cy + 68, new CircuitRenderWrapper(PartToggleLatch.class), Arrays.asList(
			new CircuitRenderWrapper(PartRSLatch.class),
			new CircuitRenderWrapper(PartTransparentLatch.class)), this));
		
		this.buttonList.add(new GuiPartChooser(5, cx + 220, cy + 89, new CircuitRenderWrapper(PartANDGate.class), Arrays.asList(
			new CircuitRenderWrapper(PartORGate.class),
			new CircuitRenderWrapper(PartXORGate.class),
			new CircuitRenderWrapper(PartBufferGate.class)), this));
		
		this.buttonList.add(new GuiPartChooser(6, cx + 220, cy + 110, new CircuitRenderWrapper(PartNANDGate.class), Arrays.asList(
			new CircuitRenderWrapper(PartNORGate.class),
			new CircuitRenderWrapper(PartXNORGate.class),
			new CircuitRenderWrapper(PartNOTGate.class)), this));
		
		this.buttonList.add(new GuiPartChooser(7, cx + 220, cy + 47, new CircuitRenderWrapper(PartTimer.class), Arrays.asList(
			new CircuitRenderWrapper(PartSequencer.class),
			new CircuitRenderWrapper(PartSynchronizer.class),
			new CircuitRenderWrapper(PartStateCell.class),
			new CircuitRenderWrapper(PartPulseFormer.class),
			new CircuitRenderWrapper(PartRandomizer.class),
			new CircuitRenderWrapper(PartRepeater.class),
			new CircuitRenderWrapper(PartMultiplexer.class)), this));

		refreshUI();
		super.initGui();
	}
	
	public void refreshIO()
	{
		checkN.refresh();
		checkE.refresh();
		checkS.refresh();
		checkW.refresh();
	}
	
	public void refreshUI()
	{
		int w = te.getCircuitData().getSize();
		buttonSize.displayString = w + "x" + w;
		refreshIO();
		nameField.setText(te.getCircuitData().getProperties().getName());
	}
	
	private int cb;
	
	@Override
	protected void actionPerformed(GuiButton button) 
	{
		int w = te.getCircuitData().getSize();
		if(button.id == 8) scale(1);
		else if(button.id == 9) scale(-1);
		else if(button.id == 10)
		{
			cb = 1;
			if(checkboxDelete.isChecked()) callbackDelete.display();
			else onCallback(callbackDelete, Action.OK, 0);
		}
		else if(button.id == 11)
		{
			cb = 2;
			if(checkboxDelete.isChecked()) callbackDelete.display();
			else onCallback(callbackDelete, Action.OK, 0);
		}
		else if(button.id == 13)
			IntegratedCircuits.networkWrapper.sendToServer(new PacketPCBIO(true, te.xCoord, te.yCoord, te.zCoord));
		else if(button.id == 12)
			IntegratedCircuits.networkWrapper.sendToServer(new PacketPCBIO(false, te.xCoord, te.yCoord, te.zCoord));
	}
	
	@Override
	public void onCallback(GuiCallback gui, Action result, int id) 
	{
		int w = te.getCircuitData().getSize();
		if(result == Action.OK && gui == callbackDelete)
		{
			if(cb == 1) IntegratedCircuits.networkWrapper.sendToServer(new PacketPCBClear((byte)w, te.xCoord, te.yCoord, te.zCoord));
			else
			{
				w = w == 16 ? 32 : w == 32 ? 64 : 16;
				IntegratedCircuits.networkWrapper.sendToServer(new PacketPCBClear((byte)w, te.xCoord, te.yCoord, te.zCoord));
			}
		}
		else if(gui == callbackTimed && result == Action.CUSTOM)
		{
			IConfigurableDelay conf = (IConfigurableDelay)timedPart;
			int delay = conf.getConfigurableDelay(timedPart.getPos(), timedPart);
			switch (id) {
			case 1 : delay -= 20; break;
			case 2 : delay -= 1; break;
			case 3 : delay += 1; break;
			case 4 : delay += 20; break;
			}
			delay = delay < 2 ? 2 : delay > 255 ? 255 : delay;
			conf.setConfigurableDelay(timedPart.getPos(), timedPart, delay);
			labelTimed.setText(I18n.format("gui.integratedcitcuits.cad.callback.delay", conf.getConfigurableDelay(timedPart.getPos(), timedPart)));
			IntegratedCircuits.networkWrapper.sendToServer(
				new PacketPCBChangePart(new int[]{timedPart.getPos().x, timedPart.getPos().y, CircuitPart.getId(timedPart.getPart()), timedPart.getCircuitData().getMeta(timedPart.getPos())}, -1, false, false, te.xCoord, te.yCoord, te.zCoord));
		}
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
		mc.getTextureManager().bindTexture(Resources.RESOURCE_GUI_CAD_BACKGROUND);
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

		fontRendererObj.drawString(I18n.format("gui.integratedcircuits.cad.name"), guiLeft + 8, guiTop + 12, 0x333333);
		
		GL11.glPushMatrix();
		GL11.glTranslated(guiLeft, guiTop, 0);
		mc.getTextureManager().bindTexture(Resources.RESOURCE_PCB);
		
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor((int)((guiLeft + 17) * guiScale), this.mc.displayHeight - (int)((guiTop + 44) * guiScale) - 374 / 2 * guiScale, (int)(374 * guiScale / 2), (int)(374 * guiScale / 2));
		GL11.glScalef(te.scale, te.scale, 1F);
		
		CircuitPartRenderer.renderPerfboard(te.offX, te.offY, data);
		CircuitPartRenderer.renderParts(new CircuitRenderWrapper(te.getCircuitData()), te.offX, te.offY);
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glColor4f(0.6F, 0.6F, 0.6F, 0.7F);
		
		double x2 = (int)((x - guiLeft - te.offX * te.scale) / 16F / te.scale);
		double y2 = (int)((y - guiTop - te.offY * te.scale) / 16F / te.scale);
		
		if(selectedPart != null && selectedPart.getPart() instanceof PartWire && drag)
		{
			ex = (int)x2;
			ey = (int)y2;
		}
		
		if(x2 > 0 && y2 > 0 && x2 < w - 1 && y2 < w - 1 && !shiftDown && !blockMouseInput)
		{
			if(!(x < guiLeft + 17 || y < guiTop + 44 || x > guiLeft + 17 + 187 || y > guiTop + 44 + 187) && selectedPart != null)
			{
				if(!(selectedPart.getPart() instanceof PartWire) || !drag)
				{
					x2 = x2 * 16 + te.offX;
					y2 = y2 * 16 + te.offY;
					if(selectedPart.getPart() instanceof PartNull)
					{
						GL11.glColor3f(0F, 0.4F, 0F);
						GL11.glDisable(GL11.GL_TEXTURE_2D);
						Tessellator.instance.startDrawingQuads();
						CircuitPartRenderer.addQuad(x2, y2, 2 * 16, 0, 16, 16);
						Tessellator.instance.draw();
						GL11.glEnable(GL11.GL_TEXTURE_2D);
					}
					CircuitPartRenderer.renderPart(selectedPart, x2, y2);
				}
				else
				{
					PartWire wire = (PartWire)selectedPart.getPart();
					GL11.glTranslated(te.offX, te.offY, 0);
					switch (wire.getColor(selectedPart.getPos(), selectedPart)) {
					case 1: GL11.glColor3f(0.4F, 0F, 0F); break;
					case 2: GL11.glColor3f(0.4F, 0.2F, 0F); break;
					default: GL11.glColor3f(0F, 0.4F, 0F); break;
					}
					
					x2 = sx; y2 = sy;
					
					Tessellator.instance.startDrawingQuads();
					CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 0, 0, 16, 16);
					if(ey > sy) CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 4 * 16, 0, 16, 16);
					else if(ey < sy) CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 2 * 16, 0, 16, 16);
					else if(ex > sx) CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 3 * 16, 0, 16, 16);
					else if(ex < sx) CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 16, 0, 16, 16);
					
					while(x2 != ex || y2 != ey)
					{
						if(y2 < ey) y2++;
						else if(y2 > ey) y2--; 
						else if(x2 < ex)
						{
							x2++;
						}
						else if(x2 > ex) 
						{
							x2--;
						}
						if(y2 != ey) CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 6 * 16, 0, 16, 16);
						else if(y2 == ey && x2 == sx) 
						{
							CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 0, 0, 16, 16);
							if(ey > sy) CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 2 * 16, 0, 16, 16);
							else if(ey < sy) CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 4 * 16, 0, 16, 16);
							if(ex > sx) CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 3 * 16, 0, 16, 16);
							else if(ex < sx) CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 16, 0, 16, 16);
						}
						else if(x2 != ex) CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 5 * 16, 0, 16, 16);
						else if(x2 == ex) 
						{
							CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 0, 0, 16, 16);
							if(ex > sx) CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 16, 0, 16, 16);
							else if(ex < sx) CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 3 * 16, 0, 16, 16);
						}
					}
					Tessellator.instance.draw();
					GL11.glColor3f(1, 1, 1);
					GL11.glTranslated(-te.offX, -te.offY, 0);
				}
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
		GL11.glColor3f(1, 1, 1);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) 
	{
		GL11.glColor3f(1, 1, 1);
		CircuitData data = te.getCircuitData();
		
		int x2 = (int)((x - guiLeft - te.offX * te.scale) / (16F * te.scale));
		int y2 = (int)((y - guiTop - te.offY * te.scale) / (16F * te.scale));
		
		int w = data.getSize();
		if(x2 >= 0 && y2 >= 0 && x2 < w && y2 < w && !blockMouseInput)
		{
			if(!(x < guiLeft + 17 || y < guiTop + 44 || x > guiLeft + 17 + 187 || y > guiTop + 44 + 187))
			{
				Vec2 pos = new Vec2(x2, y2);
				CircuitPart part = data.getPart(pos);
				if(!(part instanceof PartNull || part instanceof PartWire || part instanceof PartNullCell))
				{
					ArrayList<String> text = new ArrayList<String>();
					text.add(part.getLocalizedName(pos, te));
					if(part instanceof PartCPGate) 
					{
						int rotation = ((PartCPGate)part).getRotation(pos, te);
						ForgeDirection rot = 
							rotation == 0 ? ForgeDirection.NORTH :
							rotation == 1 ? ForgeDirection.EAST :
							rotation == 2 ? ForgeDirection.SOUTH :
							ForgeDirection.WEST;
						text.add(EnumChatFormatting.DARK_GRAY + "" + EnumChatFormatting.ITALIC + MiscUtils.getLocalizedDirection(rot));
					}
					text.addAll(part.getInformation(pos, te));
					drawHoveringText(text, x - guiLeft, y - guiTop, this.fontRendererObj);
				}
			}
		}
		if(hoveredElement != null)
			drawHoveringText(hoveredElement.getHoverInformation(), x - guiLeft, y - guiTop, this.fontRendererObj);
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		fontRendererObj.drawString((int)(te.scale * 100) + "%", 217, 235, 0x333333);
		GL11.glColor3f(1, 1, 1);
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
		
		drag = false;
		if(x2 > 0 && y2 > 0 && x2 < w - 1 && y2 < w - 1 && !shiftDown)
		{
			if(selectedPart == null)
			{
				Vec2 pos = new Vec2(x2, y2);
				CircuitPart cp = data.getPart(pos);
				if(cp instanceof IConfigurableDelay && ctrlDown) 
				{
					timedPart = new CircuitRenderWrapper(te.getCircuitData()).setPart(cp);
					labelTimed.setText(String.format("Current delay: %s ticks", ((IConfigurableDelay)timedPart).getConfigurableDelay(pos, te)));
					callbackTimed.display();
				}
				else IntegratedCircuits.networkWrapper.sendToServer(
					new PacketPCBChangePart(new int[]{x2, y2, 0, 0}, flag, ctrlDown, te.xCoord, te.yCoord, te.zCoord));
			}	
			else if(selectedPart.getPart() instanceof PartWire)
			{
				sx = x2;
				sy = y2;
				drag = true;
			}
			else
			{
				IntegratedCircuits.networkWrapper.sendToServer(
					new PacketPCBChangePart(new int[]{x2, y2, CircuitPart.getId(selectedPart.getPart()), selectedPart.getState()}, -1, false, te.xCoord, te.yCoord, te.zCoord));
			}
		}
		
		super.mouseClicked(x, y, flag);
	}

	@Override
	protected void mouseClickMove(int x, int y, int par3, long par4) 
	{
		super.mouseClickMove(x, y, par3, par4);
		
		if(selectedPart != null && selectedPart.getPart() instanceof PartNull)
		{
			int x2 = (int)((x - guiLeft - te.offX * te.scale) / (16F * te.scale));
			int y2 = (int)((y - guiTop - te.offY * te.scale) / (16F * te.scale));
			int w = te.getCircuitData().getSize();
			boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
			
			if(x2 > 0 && y2 > 0 && x2 < w - 1 && y2 < w - 1 && !shiftDown)
			{
				Vec2 pos = new Vec2(x2, y2);
				if(!(te.getCircuitData().getPart(pos) instanceof PartNull))
				{
					IntegratedCircuits.networkWrapper.sendToServer(
						new PacketPCBChangePart(new int[]{x2, y2, CircuitPart.getId(selectedPart.getPart()), selectedPart.getState()}, -1, false, te.xCoord, te.yCoord, te.zCoord));
				}
			}
		}
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
		if(button != -1 && drag && selectedPart != null)
		{
			int id = CircuitPart.getId(selectedPart.getPart());
			int state = selectedPart.getState();
			int w = te.getCircuitData().getSize();
			
			if(ex > 0 && ey > 0 && ex < w - 1 && ey < w - 1 && !blockMouseInput)
			{
				ArrayList<Vec2> list = new ArrayList<Vec2>();
				list.add(new Vec2(sx, sy));
				while(sx != ex || sy != ey)
				{
					if(sy < ey) sy++;
					else if(sy > ey) sy--;
					else if(sx < ex) sx++;
					else if(sx > ex) sx--;
					list.add(new Vec2(sx, sy));
				}
				int[] data = new int[list.size() * 4];
				for(int i = 0; i < list.size(); i++)
				{
					Vec2 pt = list.get(i);
					int index = i * 4;
					data[index] = pt.x;
					data[index + 1] = pt.y;
					data[index + 2] = id;
					data[index + 3] = state;
				}
				IntegratedCircuits.networkWrapper.sendToServer(
					new PacketPCBChangePart(data, -1, false, te.xCoord, te.yCoord, te.zCoord));
			}
		}
		drag = false;
	}

	@Override
	protected void keyTyped(char par1, int par2) 
	{
		String oname = nameField.getText();
		if(nameField.isFocused()) nameField.textboxKeyTyped(par1, par2);
		else super.keyTyped(par1, par2);
		
		if(!oname.equals(nameField.getText()))
			IntegratedCircuits.networkWrapper.sendToServer(new PacketPCBChangeName(MiscUtils.thePlayer(), nameField.getText(), te.xCoord, te.yCoord, te.zCoord));
	}

	@Override
	public void onGuiClosed() 
	{
		super.onGuiClosed();
		Config.showConfirmMessage.set(checkboxDelete.isChecked());
		Config.save();
	}

	@Override
	public void setCurrentItem(IHoverable hoverable) 
	{
		this.hoveredElement = hoverable;
	}
}
