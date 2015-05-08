package moe.nightfall.vic.integratedcircuits.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import moe.nightfall.vic.integratedcircuits.Config;
import moe.nightfall.vic.integratedcircuits.ContainerPCBLayout;
import moe.nightfall.vic.integratedcircuits.client.Resources;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiCallback.Action;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IGuiCallback;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverableHandler;
import moe.nightfall.vic.integratedcircuits.ic.CircuitData;
import moe.nightfall.vic.integratedcircuits.ic.CircuitPart;
import moe.nightfall.vic.integratedcircuits.ic.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.ic.CircuitPartRenderer.CircuitRenderWrapper;
import moe.nightfall.vic.integratedcircuits.ic.part.PartNull;
import moe.nightfall.vic.integratedcircuits.ic.part.PartTunnel;
import moe.nightfall.vic.integratedcircuits.ic.part.PartWire;
import moe.nightfall.vic.integratedcircuits.ic.part.cell.PartNullCell;
import moe.nightfall.vic.integratedcircuits.ic.part.timed.IConfigurableDelay;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.misc.RenderUtils;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.net.PacketPCBCache;
import moe.nightfall.vic.integratedcircuits.net.PacketPCBChangeName;
import moe.nightfall.vic.integratedcircuits.net.PacketPCBChangePart;
import moe.nightfall.vic.integratedcircuits.net.PacketPCBClear;
import moe.nightfall.vic.integratedcircuits.net.PacketPCBIO;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityPCBLayout;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import cpw.mods.fml.client.config.GuiButtonExt;

public class GuiPCBLayout extends GuiContainer implements IGuiCallback, IHoverableHandler {
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

	// Because of private.
	public GuiPartChooser selectedChooser;
	public boolean blockMouseInput = false;
	private IHoverable hoveredElement;

	public CircuitRenderWrapper selectedPart;

	// Used by the wires
	private int sx, sy, ex, ey;
	private boolean drag;

	// Callbacks
	private GuiCallback callbackDelete;
	private GuiCheckBoxExt checkboxDelete;
	private GuiCallback callbackTimed;
	private GuiLabel labelTimed;
	private CircuitRenderWrapper timedPart;

	public GuiPCBLayout(ContainerPCBLayout container) {
		super(container);
		this.xSize = 248;
		this.ySize = 249;
		this.te = container.tileentity;

		callbackDelete = new GuiCallback(this, 150, 100, Action.OK, Action.CANCEL);
		checkboxDelete = new GuiCheckBoxExt(1, 7, 78, null, Config.showConfirmMessage.getBoolean(),
				I18n.format("gui.integratedcircuits.cad.callback.show"), callbackDelete);
		callbackDelete
			.addControl(new GuiLabel(75, 7, I18n.format("gui.integratedcircuits.cad.callback.confirm"), 0x333333, true))
			.addControl(new GuiLabel(75, 25, I18n.format("gui.integratedcircuits.cad.callback.message"), 0, true))
			.addControl(new GuiLabel(75, 63, I18n.format("gui.integratedcircuits.cad.callback.continue"), 0x333333, true))
			.addControl(checkboxDelete);

		labelTimed = new GuiLabel(80, 9, "", 0, true);
		callbackTimed = new GuiCallback(this, 160, 50).addControl(new GuiButtonExt(1, 5, 25, 36, 20, "-1s"))
			.addControl(new GuiButtonExt(2, 43, 25, 36, 20, "-50ms"))
			.addControl(new GuiButtonExt(3, 81, 25, 36, 20, "+50ms"))
			.addControl(new GuiButtonExt(4, 119, 25, 36, 20, "+1s")).addControl(labelTimed);
	}

	@Override
	public void initGui() {
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

		this.buttonList.add(new GuiButtonExt(84, cx + 221, cy + 32, 10, 10, "\u21B6"));
		this.buttonList.add(new GuiButtonExt(85, cx + 232, cy + 32, 10, 10, "\u21B7"));

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

		for (int i = 0; i < 16; i++)
			this.buttonList.add(new GuiIO(i + 13, cx + 39 + i * 9, cy + 37, 15 - i, 0, this, te));
		for (int i = 0; i < 16; i++)
			this.buttonList.add(new GuiIO(i + 13 + 16, cx + 207, cy + 70 + i * 9, 15 - i, 1, this, te));
		for (int i = 0; i < 16; i++)
			this.buttonList.add(new GuiIO(i + 13 + 32, cx + 6, cy + 70 + i * 9, i, 3, this, te));
		for (int i = 0; i < 16; i++)
			this.buttonList.add(new GuiIO(i + 13 + 48, cx + 39 + i * 9, cy + 238, i, 2, this, te));

		int currentPosition = cy + 47;
		for (CircuitPart.Category category : CircuitPart.Category.values()) {
			List<CircuitPart> parts = CircuitPart.getParts(category);
			// If the part hasn't got a category, or there are no parts in the category, do not add the button for the category.
			if (category == CircuitPart.Category.NONE || parts.size() == 0)
				continue;
			this.buttonList.add(new GuiPartChooser(7, cx + 220, currentPosition, GuiPartChooser.getRenderWrapperParts(parts), this));
			currentPosition += 21;
		}

		// The edit and erase buttons
		this.buttonList.add(c1);
		this.buttonList.add(new GuiPartChooser(1, cx + 220, cy + 215, 2, this));

		refreshUI();
		super.initGui();
	}

	public void refreshIO() {
		checkN.refresh();
		checkE.refresh();
		checkS.refresh();
		checkW.refresh();
	}

	public void refreshUI() {
		int w = te.getCircuitData().getSize();
		buttonSize.displayString = w + "x" + w;
		refreshIO();
		nameField.setText(te.getCircuitData().getProperties().getName());
	}

	private int cb;

	@Override
	protected void actionPerformed(GuiButton button) {
		int w = te.getCircuitData().getSize();
		if (button.id == 8)
			scale(1);
		else if (button.id == 9)
			scale(-1);
		else if (button.id == 10) {
			cb = 1;
			if (checkboxDelete.isChecked())
				callbackDelete.display();
			else
				onCallback(callbackDelete, Action.OK, 0);
		} else if (button.id == 11) {
			cb = 2;
			if (checkboxDelete.isChecked())
				callbackDelete.display();
			else
				onCallback(callbackDelete, Action.OK, 0);
		} else if (button.id == 13)
			CommonProxy.networkWrapper.sendToServer(new PacketPCBIO(true, te.xCoord, te.yCoord, te.zCoord));
		else if (button.id == 12)
			CommonProxy.networkWrapper.sendToServer(new PacketPCBIO(false, te.xCoord, te.yCoord, te.zCoord));
		else if (button.id == 84)
			CommonProxy.networkWrapper.sendToServer(new PacketPCBCache(PacketPCBCache.UNDO, te.xCoord, te.yCoord,
					te.zCoord));
		else if (button.id == 85)
			CommonProxy.networkWrapper.sendToServer(new PacketPCBCache(PacketPCBCache.REDO, te.xCoord, te.yCoord,
					te.zCoord));
	}

	@Override
	public void onCallback(GuiCallback gui, Action result, int id) {
		int w = te.getCircuitData().getSize();
		if (result == Action.OK && gui == callbackDelete) {
			if (cb == 1)
				CommonProxy.networkWrapper.sendToServer(new PacketPCBClear((byte) w, te.xCoord, te.yCoord, te.zCoord));
			else {
				w = w == 16 ? 32 : w == 32 ? 64 : 16;
				CommonProxy.networkWrapper.sendToServer(new PacketPCBClear((byte) w, te.xCoord, te.yCoord, te.zCoord));
			}
		} else if (gui == callbackTimed && result == Action.CUSTOM) {
			IConfigurableDelay conf = (IConfigurableDelay) timedPart.getPart();
			int delay = conf.getConfigurableDelay(timedPart.getPos(), timedPart);
			switch (id) {
				case 1:
					delay -= 20;
					break;
				case 2:
					delay -= 1;
					break;
				case 3:
					delay += 1;
					break;
				case 4:
					delay += 20;
					break;
			}
			delay = delay < 2 ? 2 : delay > 255 ? 255 : delay;
			conf.setConfigurableDelay(timedPart.getPos(), timedPart, delay);
			labelTimed.setText(I18n.format("gui.integratedcitcuits.cad.callback.delay",
					conf.getConfigurableDelay(timedPart.getPos(), timedPart)));
			CommonProxy.networkWrapper.sendToServer(new PacketPCBChangePart(new int[] { timedPart.getPos().x,
					timedPart.getPos().y, CircuitPart.getId(timedPart.getPart()), timedPart.getState() }, false,
					te.xCoord, te.yCoord, te.zCoord));
		}
	}

	public List getButtonList() {
		return buttonList;
	}

	@Override
	public void updateScreen() {
		nameField.updateCursorCounter();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int x, int y) {
		hoveredElement = null;
		Tessellator tes = Tessellator.instance;

		double x2 = (int) ((x - guiLeft - te.offX * te.scale) / 16F / te.scale);
		double y2 = (int) ((y - guiTop - te.offY * te.scale) / 16F / te.scale);

		ex = (int) x2;
		ey = (int) y2;

		GL11.glColor3f(1F, 1F, 1F);
		mc.getTextureManager().bindTexture(Resources.RESOURCE_GUI_CAD_BACKGROUND);
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
		int guiScale = scaledresolution.getScaleFactor();

		CircuitData data = te.getCircuitData();

		int w = data.getSize();
		if (Mouse.isButtonDown(0) && (x - lastX != 0 || y - lastY != 0) && isShiftKeyDown()) {
			if (!(x < guiLeft + 17 || y < guiTop + 44 || x > guiLeft + 17 + 187 || y > guiTop + 44 + 187)) {
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
		GL11.glScissor((int) ((guiLeft + 17) * guiScale), this.mc.displayHeight - (int) ((guiTop + 44) * guiScale)
				- 374 / 2 * guiScale, (int) (374 * guiScale / 2), (int) (374 * guiScale / 2));
		GL11.glScalef(te.scale, te.scale, 1F);

		CircuitPartRenderer.renderPerfboard(te.offX, te.offY, data);
		CircuitPartRenderer.renderParts(te, te.offX, te.offY);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(1F, 1F, 1F, 1F);

		boolean ctrl = isCtrlKeyDown();
		// Render connections for tunnels
		tes.startDrawingQuads();
		for (int x3 = 0; x3 < data.getSize(); x3++) {
			for (int y3 = 0; y3 < data.getSize(); y3++) {
				if (x3 == ex && y3 == ey && data.getPart(new Vec2(x3, y3)) instanceof PartTunnel && selectedPart == null) {
					drawTunnelConnection(x3, y3);
				}
				if (drag && selectedPart == null) {
					Tessellator.instance.setColorRGBA_F(0F, 0F, 1F, 1F);
					CircuitPartRenderer.addQuad(sx * 16 + te.offX, sy * 16 + te.offY, 0, 0, 16, 16);
				}
				if (ctrl) {
					drawTunnelConnection(x3, y3);
				}
			}
		}
		tes.draw();

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(0.6F, 0.6F, 0.6F, 0.7F);

		if (x2 > 0 && y2 > 0 && x2 < w - 1 && y2 < w - 1 && !isShiftKeyDown() && !blockMouseInput) {
			if (!(x < guiLeft + 17 || y < guiTop + 44 || x > guiLeft + 17 + 187 || y > guiTop + 44 + 187)) {
				if (!drag && selectedPart != null) {
					x2 = x2 * 16 + te.offX;
					y2 = y2 * 16 + te.offY;
					if (selectedPart.getPart() instanceof PartNull) {
						GL11.glColor3f(0F, 0.4F, 0F);
						GL11.glDisable(GL11.GL_TEXTURE_2D);
						tes.startDrawingQuads();
						CircuitPartRenderer.addQuad(x2, y2, 2 * 16, 0, 16, 16);
						tes.draw();
						GL11.glEnable(GL11.GL_TEXTURE_2D);
					}
					CircuitPartRenderer.renderPart(selectedPart, x2, y2);
				} else if (drag) {
					if (selectedPart == null) {
						GL11.glColor4f(0F, 0F, 1F, 1F);
						GL11.glDisable(GL11.GL_TEXTURE_2D);

						tes.startDrawingQuads();
						if (data.getPart(new Vec2(ex, ey)) instanceof PartTunnel) {
							RenderUtils.addLine(sx * 16 + te.offX + 8, sy * 16 + te.offY + 8, ex * 16 + te.offX + 8, ey * 16 + te.offY + 8, 4);
						} else {
							double x3 = (x - guiLeft - te.offX * te.scale) / te.scale + te.offX;
							double y3 = (y - guiTop - te.offY * te.scale) / te.scale + te.offY;
							RenderUtils.addLine(sx * 16 + te.offX + 8, sy * 16 + te.offY + 8, x3, y3, 4);
						}
						tes.draw();

						GL11.glEnable(GL11.GL_TEXTURE_2D);
						GL11.glColor4f(0.6F, 0.6F, 0.6F, 0.7F);
					} else if (selectedPart.getPart() instanceof PartWire) {
						PartWire wire = (PartWire) selectedPart.getPart();
						GL11.glTranslated(te.offX, te.offY, 0);
						switch (wire.getColor(selectedPart.getPos(), selectedPart)) {
							case 1:
								GL11.glColor3f(0.4F, 0F, 0F);
								break;
							case 2:
								GL11.glColor3f(0.4F, 0.2F, 0F);
								break;
							default:
								GL11.glColor3f(0F, 0.4F, 0F);
								break;
						}

						x2 = sx;
						y2 = sy;

						tes.startDrawingQuads();
						CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 0, 0, 16, 16);
						if (ey > sy)
							CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 4 * 16, 0, 16, 16);
						else if (ey < sy)
							CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 2 * 16, 0, 16, 16);
						else if (ex > sx)
							CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 3 * 16, 0, 16, 16);
						else if (ex < sx)
							CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 16, 0, 16, 16);

						while (x2 != ex || y2 != ey) {
							if (y2 < ey)
								y2++;
							else if (y2 > ey)
								y2--;
							else if (x2 < ex)
								x2++;
							else if (x2 > ex)
								x2--;

							if (y2 != ey)
								CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 6 * 16, 0, 16, 16);
							else if (y2 == ey && x2 == sx) {
								CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 0, 0, 16, 16);
								if (ey > sy)
									CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 2 * 16, 0, 16, 16);
								else if (ey < sy)
									CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 4 * 16, 0, 16, 16);
								if (ex > sx)
									CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 3 * 16, 0, 16, 16);
								else if (ex < sx)
									CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 16, 0, 16, 16);
							} else if (x2 != ex)
								CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 5 * 16, 0, 16, 16);
							else if (x2 == ex) {
								CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 0, 0, 16, 16);
								if (ex > sx)
									CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 16, 0, 16, 16);
								else if (ex < sx)
									CircuitPartRenderer.addQuad(x2 * 16, y2 * 16, 3 * 16, 0, 16, 16);
							}
						}
						tes.draw();
						GL11.glColor3f(1, 1, 1);
						GL11.glTranslated(-te.offX, -te.offY, 0);
					}
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
		tes.startDrawingQuads();

		int i1 = guiLeft + 17, i2 = guiTop + 44, i3 = 187, i4 = 4;

		// Draw inner gradient
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

	private void drawTunnelConnection(int x, int y) {
		Vec2 pos = new Vec2(x, y);
		CircuitPart part = te.getCircuitData().getPart(pos);
		if (!(part instanceof PartTunnel))
			return;

		PartTunnel pt = (PartTunnel) part;
		Vec2 pos2 = pt.getConnectedPos(pos, te);

		double x3 = x * 16 + te.offX;
		double y3 = y * 16 + te.offY;

		if (pt.getInput(pos, te) || pt.getProperty(pos, te, pt.PROP_IN)) {
			Tessellator.instance.setColorRGBA_F(1F, 0F, 0F, 1F);
		} else {
			Tessellator.instance.setColorRGBA_F(0F, 0F, 1F, 1F);
		}

		if (pt.isConnected(pos2)) {
			double x4 = pos2.x * 16 + te.offX;
			double y4 = pos2.y * 16 + te.offY;

			RenderUtils.addLine(x3 + 8, y3 + 8, x4 + 8, y4 + 8, 4);
			CircuitPartRenderer.addQuad(x4, y4, 0, 0, 16, 16);
		}
		CircuitPartRenderer.addQuad(x3, y3, 0, 0, 16, 16);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) {
		GL11.glColor3f(1, 1, 1);
		CircuitData data = te.getCircuitData();

		int x2 = (int) ((x - guiLeft - te.offX * te.scale) / (16F * te.scale));
		int y2 = (int) ((y - guiTop - te.offY * te.scale) / (16F * te.scale));

		ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
		int guiScale = scaledresolution.getScaleFactor();

		int w = data.getSize();
		if (x2 >= 0 && y2 >= 0 && x2 < w && y2 < w && !blockMouseInput && !isShiftKeyDown()) {
			if (!(x < guiLeft + 17 || y < guiTop + 44 || x > guiLeft + 17 + 187 || y > guiTop + 44 + 187)) {
				Vec2 pos = new Vec2(x2, y2);
				CircuitPart part = data.getPart(pos);
				if (!(part instanceof PartNull || part instanceof PartWire || part instanceof PartNullCell)) {
					ArrayList<String> text = Lists.newArrayList();
					text.add(part.getLocalizedName(pos, te));
					text.addAll(part.getInformation(pos, te, selectedPart == null, isCtrlKeyDown()));
					drawHoveringText(text, x - guiLeft, y - guiTop, this.fontRendererObj);
				}
			}
		}
		if (hoveredElement != null)
			drawHoveringText(hoveredElement.getHoverInformation(), x - guiLeft, y - guiTop, this.fontRendererObj);
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		fontRendererObj.drawString((int) (te.scale * 100) + "%", 217, 235, 0x333333);
		GL11.glColor3f(1, 1, 1);
	}

	@Override
	protected void mouseClicked(int x, int y, int flag) {
		nameField.mouseClicked(x, y, flag);
		if (blockMouseInput) {
			super.mouseClicked(x, y, flag);
			return;
		}
		if (x < guiLeft + 17 || y < guiTop + 44 || x > guiLeft + 17 + 187 || y > guiTop + 44 + 187) {
			super.mouseClicked(x, y, flag);
			return;
		}

		CircuitData data = te.getCircuitData();

		boolean ctrlDown = isCtrlKeyDown();
		int x2 = (int) ((x - guiLeft - te.offX * te.scale) / (16F * te.scale));
		int y2 = (int) ((y - guiTop - te.offY * te.scale) / (16F * te.scale));
		int w = data.getSize();

		drag = false;
		if (x2 > 0 && y2 > 0 && x2 < w - 1 && y2 < w - 1 && !isShiftKeyDown()) {
			if (selectedPart == null) {
				Vec2 pos = new Vec2(x2, y2);
				CircuitPart cp = data.getPart(pos);
				if (cp instanceof IConfigurableDelay && ctrlDown) {
					timedPart = new CircuitRenderWrapper(te.getCircuitData(), cp, pos);
					labelTimed.setText(String.format("Current delay: %s ticks",
							((IConfigurableDelay) cp).getConfigurableDelay(pos, te)));
					callbackTimed.display();
				} else if (cp instanceof PartTunnel) {
					sx = x2;
					sy = y2;
					drag = true;
				} else {
					CommonProxy.networkWrapper.sendToServer(new PacketPCBChangePart(x2, y2, flag, ctrlDown, te.xCoord,
							te.yCoord, te.zCoord));
				}
			} else if (selectedPart.getPart() instanceof PartWire) {
				sx = x2;
				sy = y2;
				drag = true;
			} else {
				int newID = CircuitPart.getId(selectedPart.getPart());
				if (newID != te.getCircuitData().getID(new Vec2(x2, y2))) {
					CommonProxy.networkWrapper.sendToServer(new PacketPCBChangePart(new int[] { x2, y2,
							newID, selectedPart.getState() },
							!(selectedPart.getPart() instanceof PartNull), te.xCoord, te.yCoord, te.zCoord));
				}
			}
		}

		super.mouseClicked(x, y, flag);
	}

	@Override
	protected void mouseClickMove(int x, int y, int par3, long par4) {
		super.mouseClickMove(x, y, par3, par4);

		if (selectedPart != null && selectedPart.getPart() instanceof PartNull) {
			int x2 = (int) ((x - guiLeft - te.offX * te.scale) / (16F * te.scale));
			int y2 = (int) ((y - guiTop - te.offY * te.scale) / (16F * te.scale));
			int w = te.getCircuitData().getSize();
			boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

			if (x2 > 0 && y2 > 0 && x2 < w - 1 && y2 < w - 1 && !shiftDown) {
				Vec2 pos = new Vec2(x2, y2);
				if (!(te.getCircuitData().getPart(pos) instanceof PartNull)) {
					CommonProxy.networkWrapper.sendToServer(new PacketPCBChangePart(new int[] { x2, y2, 0, 0 }, false,
							te.xCoord, te.yCoord, te.zCoord));
				}
			}
		}
	}

	private void scale(int i) {
		int w = te.getCircuitData().getSize();

		double ow = w * 16 * te.scale;
		float oldScale = te.scale;

		int index = scales.indexOf(te.scale);

		if (i > 0 && index + 1 < scales.size())
			te.scale = scales.get(index + 1);
		if (i < 0 && index - 1 >= 0)
			te.scale = scales.get(index - 1);

		if (i != 0) {
			buttonMinus.enabled = true;
			buttonPlus.enabled = true;

			if (te.scale == 0.17F)
				buttonMinus.enabled = false;
			if (te.scale == 2F)
				buttonPlus.enabled = false;
		}

		double change = (double) te.scale / (double) oldScale;
		double nw = w * 16 * te.scale;

		if (i > 0) {
			te.offX = te.offX / change;
			te.offY = te.offY / change;
			te.offX -= (nw - ow) / 2 / te.scale;
			te.offY -= (nw - ow) / 2 / te.scale;
		} else if (i < 0) {
			te.offX -= (nw - ow) / 2 / oldScale;
			te.offY -= (nw - ow) / 2 / oldScale;
			te.offX = te.offX / change;
			te.offY = te.offY / change;
		}
	}

	@Override
	public void handleMouseInput() {
		scale(Mouse.getEventDWheel());

		super.handleMouseInput();
	}

	private static List<Float> scales = Arrays.asList(0.17F, 0.2F, 0.25F, 0.33F, 0.5F, 0.67F, 1F, 1.5F, 2F);

	@Override
	protected void mouseMovedOrUp(int x, int y, int button) {
		super.mouseMovedOrUp(x, y, button);
		if (this.selectedChooser != null && button == 0) {
			this.selectedChooser.mouseReleased(x, y);
			this.selectedChooser = null;
		}
		if (button != -1 && selectedPart != null && selectedPart.getPart() instanceof PartNull)
			CommonProxy.networkWrapper.sendToServer(new PacketPCBCache(PacketPCBCache.SNAPSHOT, te.xCoord, te.yCoord, te.zCoord));
		else if (button != -1 && drag) {
			int w = te.getCircuitData().getSize();

			if (ex > 0 && ey > 0 && ex < w - 1 && ey < w - 1 && !blockMouseInput) {
				if (selectedPart == null) {
					PartTunnel pt = CircuitPart.getPart(PartTunnel.class);
					
					Vec2 first = new Vec2(sx, sy);
					Vec2 second = new Vec2(ex, ey);
					
					if (te.getCircuitData().getPart(second) instanceof PartTunnel) {
						
						List<Integer> data = Lists.newArrayList();

						if (pt.isConnected(pt.getConnectedPos(first, te))) {
							Vec2 part = pt.getConnectedPos(first, te);
							data.add(part.x);
							data.add(part.y);
							data.add(CircuitPart.getId(pt));
							data.add(pt.setConnectedPos(te.getCircuitData().getMeta(part), new Vec2(255, 255)));
						}

						data.add(first.x);
						data.add(first.y);
						data.add(CircuitPart.getId(pt));
						data.add(pt.setConnectedPos(te.getCircuitData().getMeta(first), second));
						
						if (pt.isConnected(pt.getConnectedPos(second, te))) {
							Vec2 part = pt.getConnectedPos(second, te);
							data.add(part.x);
							data.add(part.y);
							data.add(CircuitPart.getId(pt));
							data.add(pt.setConnectedPos(te.getCircuitData().getMeta(part), new Vec2(255, 255)));
						}
						
						data.add(second.x);
						data.add(second.y);
						data.add(CircuitPart.getId(pt));
						data.add(pt.setConnectedPos(te.getCircuitData().getMeta(second), first));

						CommonProxy.networkWrapper.sendToServer(new PacketPCBChangePart(Ints.toArray(data), true, te.xCoord, te.yCoord, te.zCoord));
					}
				} else if(selectedPart.getPart() instanceof PartWire) {

					int id = CircuitPart.getId(selectedPart.getPart());
					int state = selectedPart.getState();

					ArrayList<Vec2> list = new ArrayList<Vec2>();
					list.add(new Vec2(sx, sy));
					while (sx != ex || sy != ey) {
						if (sy < ey)
							sy++;
						else if (sy > ey)
							sy--;
						else if (sx < ex)
							sx++;
						else if (sx > ex)
							sx--;
						list.add(new Vec2(sx, sy));
					}
					int[] data = new int[list.size() * 4];
					for (int i = 0; i < list.size(); i++) {
						Vec2 pt = list.get(i);
						int index = i * 4;
						data[index] = pt.x;
						data[index + 1] = pt.y;
						data[index + 2] = id;
						data[index + 3] = state;
					}
					CommonProxy.networkWrapper.sendToServer(new PacketPCBChangePart(data, true, te.xCoord, te.yCoord, te.zCoord));
				}
			}
		}
		drag = false;
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		String oname = nameField.getText();
		if (nameField.isFocused())
			nameField.textboxKeyTyped(par1, par2);
		else if (par2 == Keyboard.KEY_Z && isCtrlKeyDown())
			CommonProxy.networkWrapper.sendToServer(new PacketPCBCache(PacketPCBCache.UNDO, te.xCoord, te.yCoord,
					te.zCoord));
		else if (par2 == Keyboard.KEY_Y && isCtrlKeyDown())
			CommonProxy.networkWrapper.sendToServer(new PacketPCBCache(PacketPCBCache.REDO, te.xCoord, te.yCoord,
					te.zCoord));
		else
			super.keyTyped(par1, par2);

		if (!oname.equals(nameField.getText()))
			CommonProxy.networkWrapper.sendToServer(new PacketPCBChangeName(MiscUtils.thePlayer(), nameField.getText(),
					te.xCoord, te.yCoord, te.zCoord));
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Config.showConfirmMessage.set(checkboxDelete.isChecked());
		Config.save();
	}

	@Override
	public void setCurrentItem(IHoverable hoverable) {
		this.hoveredElement = hoverable;
	}
}
