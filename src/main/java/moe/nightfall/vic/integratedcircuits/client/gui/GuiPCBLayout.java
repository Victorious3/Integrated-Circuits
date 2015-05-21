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
import moe.nightfall.vic.integratedcircuits.compat.NEIAddon;
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

	// The size of the GUI
	protected int guiRight;
	protected int guiBottom;

	// The size of the circuit editor
	protected int xSizeEditor;
	protected int ySizeEditor;
	protected int editorTop;
	protected int editorBottom;
	protected int editorLeft;
	protected int editorRight;

	private int lastX, lastY;
	public TileEntityPCBLayout tileentity;

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
	private int startX, startY, endX, endY;
	private boolean drag;

	// Callbacks
	private GuiCallback<GuiPCBLayout> callbackDelete;
	private GuiCheckBoxExt checkboxDelete;
	private GuiCallback<GuiPCBLayout> callbackTimed;
	private GuiLabel labelTimed;
	private CircuitRenderWrapper timedPart;

	public GuiPCBLayout(ContainerPCBLayout container) {
		super(container);
		this.tileentity = container.tileentity;

		callbackDelete = new GuiCallback<GuiPCBLayout>(this, 150, 100, Action.OK, Action.CANCEL);
		checkboxDelete = new GuiCheckBoxExt(1, 7, 78, null, Config.showConfirmMessage.getBoolean(),
				I18n.format("gui.integratedcircuits.cad.callback.show"), callbackDelete);
		callbackDelete
			.addControl(new GuiLabel(75, 7, I18n.format("gui.integratedcircuits.cad.callback.confirm"), 0x333333, true))
			.addControl(new GuiLabel(75, 25, I18n.format("gui.integratedcircuits.cad.callback.message"), 0, true))
			.addControl(new GuiLabel(75, 63, I18n.format("gui.integratedcircuits.cad.callback.continue"), 0x333333, true))
			.addControl(checkboxDelete);

		labelTimed = new GuiLabel(80, 9, "", 0, true);
		callbackTimed = new GuiCallback<GuiPCBLayout>(this, 160, 50)
			.addControl(new GuiButtonExt(1, 5, 25, 36, 20, "-1s"))
			.addControl(new GuiButtonExt(2, 43, 25, 36, 20, "-50ms"))
			.addControl(new GuiButtonExt(3, 81, 25, 36, 20, "+50ms"))
			.addControl(new GuiButtonExt(4, 119, 25, 36, 20, "+1s")).addControl(labelTimed);
	}

	@Override
	public void initGui() {
		NEIAddon.hideGUI(true);

		this.mc.thePlayer.openContainer = this.inventorySlots;

		calculateSizes();

		// Zoom buttons
		buttonPlus = new GuiButtonExt(8, guiRight - 85, guiBottom - 16, 10, 10, "+");
		this.buttonList.add(buttonPlus);
		buttonMinus = new GuiButtonExt(9, guiRight - 74, guiBottom - 16, 10, 10, "-");
		this.buttonList.add(buttonMinus);

		// Reset / New Circuit button
		this.buttonList.add(new GuiButtonExt(10, guiRight - 155, guiTop + 10, 12, 12, "+"));

		// Size Selection button
		buttonSize = new GuiButtonExt(11, guiRight - 138, guiTop + 10, 38, 12, "");
		this.buttonList.add(buttonSize);

		// Circuit Name text box
		nameField = new GuiTextField(fontRendererObj, guiRight - 95, guiTop + 11, 50, 10);
		nameField.setText(tileentity.getCircuitData().getProperties().getName());
		nameField.setMaxStringLength(7);
		nameField.setCanLoseFocus(true);
		nameField.setFocused(false);

		// Save and Load buttons
		this.buttonList.add(new GuiButtonExt(12, guiRight - 38, guiTop + 6, 10, 10, "I"));
		this.buttonList.add(new GuiButtonExt(13, guiRight - 38, guiTop + 17, 10, 10, "O"));

		// The inventory slot containing the blueprint disk
		this.inventorySlots.getSlot(0).xDisplayPosition = this.xSize - 25;
		this.inventorySlots.getSlot(0).yDisplayPosition = 8;

		// Undo and Redo buttons
		this.buttonList.add(new GuiButtonExt(84, guiRight - 28, guiTop + 28, 10, 10, "\u21B6"));
		this.buttonList.add(new GuiButtonExt(85, guiRight - 17, guiTop + 28, 10, 10, "\u21B7"));

		// Input button positions
		int yOffsetCentre = getOffsetCentre(editorTop - guiTop, guiBottom - editorBottom, guiBottom - (ySize - guiBottom), 170) + 1;
		int xOffsetCentre = getOffsetCentre(editorLeft - guiLeft, guiRight - editorRight, guiRight - (xSize - guiRight), 170) + 1;
		int topEditorOffset = editorTop - 11;
		int leftEditorOffset = editorLeft - 11;
		int rightEditorOffset = editorRight + 1;
		int bottomEditorOffset = editorBottom + 1;

		// Input Mode buttons
		checkN = new GuiIOMode(65, xOffsetCentre, topEditorOffset - 1, this, 0);
		checkE = new GuiIOMode(66, rightEditorOffset, yOffsetCentre, this, 1);
		checkS = new GuiIOMode(67, xOffsetCentre, bottomEditorOffset, this, 2);
		checkW = new GuiIOMode(68, leftEditorOffset - 1, yOffsetCentre, this, 3);

		this.buttonList.add(checkN);
		this.buttonList.add(checkE);
		this.buttonList.add(checkS);
		this.buttonList.add(checkW);

		// Input buttons
		xOffsetCentre += 13;
		yOffsetCentre += 13;
		// North / Top Input buttons
		for (int i = 0; i < 16; i++)
			this.buttonList.add(new GuiIO(i + 13, xOffsetCentre + i * 9, topEditorOffset, 15 - i, 0, this, tileentity));
		// East / Right Input buttons
		for (int i = 0; i < 16; i++)
			this.buttonList.add(new GuiIO(i + 13 + 16, rightEditorOffset + 2, yOffsetCentre + i * 9, 15 - i, 1, this, tileentity));
		// West / Left Input buttons
		for (int i = 0; i < 16; i++)
			this.buttonList.add(new GuiIO(i + 13 + 32, leftEditorOffset, yOffsetCentre + i * 9, i, 3, this, tileentity));
		// South / Bottom Input buttons
		for (int i = 0; i < 16; i++)
			this.buttonList.add(new GuiIO(i + 13 + 48, xOffsetCentre + i * 9, bottomEditorOffset + 2, i, 2, this, tileentity));


		int toolsXPosition = guiRight - 27;

		int currentPosition = guiTop + 43;
		for (CircuitPart.Category category : CircuitPart.Category.values()) {
			List<CircuitPart> parts = CircuitPart.getParts(category);
			// If the part hasn't got a category, or there are no parts in the category, do not add the button for the category.
			if (category == CircuitPart.Category.NONE || parts.size() == 0)
				continue;
			this.buttonList.add(new GuiPartChooser(7, toolsXPosition, currentPosition, GuiPartChooser.getRenderWrapperParts(parts), this));
			currentPosition += 21;
		}

		// The edit and erase buttons
		currentPosition = (guiBottom - 47);
		GuiPartChooser c1 = new GuiPartChooser(0, toolsXPosition, currentPosition, 1, this);
		c1.setActive(true);
		this.buttonList.add(c1);
		currentPosition += 21;
		this.buttonList.add(new GuiPartChooser(1, toolsXPosition, currentPosition, 2, this));

		refreshUI();
	}
	
	//TODO: The handling of the circuit board position should be refactored 
	protected double getRelativeOffX() {
		return tileentity.offX + (editorLeft + xSizeEditor / 2) / tileentity.scale - 8 * tileentity.getCircuitData().getSize();
	}
	
	protected double getRelativeOffY() {
		return tileentity.offY + (editorTop + ySizeEditor / 2) / tileentity.scale - 8 * tileentity.getCircuitData().getSize();
	}
	
	protected double getRelBoardX(double absX) {
		return (absX - guiLeft - getRelativeOffX() * tileentity.scale) / (16F * tileentity.scale);
	}
	
	protected double getRelBoardY(double absY) {
		return (absY - guiTop - getRelativeOffY() * tileentity.scale) / (16F * tileentity.scale);
	}

	protected void calculateSizes() {
		this.guiTop = 0;
		this.guiLeft = 0;

		this.guiRight = this.width;
		this.guiBottom = this.height;

		this.xSize = this.guiRight - this.guiLeft;
		this.ySize = this.guiBottom - guiTop;

		this.editorTop = this.guiTop + 44;
		this.editorLeft = this.guiLeft + 44;
		this.editorBottom = this.guiBottom - 18;
		this.editorRight = this.guiRight - 44;

		this.xSizeEditor = this.editorRight - this.editorLeft;
		this.ySizeEditor = this.editorBottom - this.editorTop;
	}

	protected static int getOffsetCentre(int topOffset, int bottomOffset, int fullLength, int thingLength) {
		return ((topOffset + fullLength - bottomOffset) / 2) - (thingLength / 2);
	}

	/** Debugging tool, used to help with positioning. **/
	private void guiInfo() {
		System.out.println();
		System.out.println("GUI Info");
		System.out.println("this.width = " + this.width + "; this.height = " + this.height + ";");
		System.out.println("this.xSize = " + this.xSize + "; this.ySize = " + this.ySize + ";");
		System.out.println("this.guiLeft = " + this.guiLeft + "; this.guiTop = " + this.guiTop + ";");
		System.out.println("this.guiRight = " + this.guiRight + "; this.guiBottom = " + this.guiBottom + ";");
		System.out.println("this.xSizeEditor = " + this.xSizeEditor + "; this.ySizeEditor = " + this.ySizeEditor + ";");
		System.out.println("this.editorLeft = " + this.editorLeft + "; this.editorTop = " + this.editorTop + ";");
		System.out.println("this.editorRight = " + this.editorRight + "; this.editorBottom = " + this.editorBottom + ";");
	}

	public void refreshIO() {
		checkN.refresh();
		checkE.refresh();
		checkS.refresh();
		checkW.refresh();
	}

	public void refreshUI() {
		int w = tileentity.getCircuitData().getSize();
		buttonSize.displayString = w + "x" + w;
		refreshIO();
		nameField.setText(tileentity.getCircuitData().getProperties().getName());
	}

	private int cb;

	@Override
	protected void actionPerformed(GuiButton button) {
		int w = tileentity.getCircuitData().getSize();
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
			CommonProxy.networkWrapper.sendToServer(new PacketPCBIO(true, tileentity.xCoord, tileentity.yCoord, tileentity.zCoord));
		else if (button.id == 12)
			CommonProxy.networkWrapper.sendToServer(new PacketPCBIO(false, tileentity.xCoord, tileentity.yCoord, tileentity.zCoord));
		else if (button.id == 84)
			CommonProxy.networkWrapper.sendToServer(new PacketPCBCache(PacketPCBCache.UNDO, tileentity.xCoord, tileentity.yCoord,
					tileentity.zCoord));
		else if (button.id == 85)
			CommonProxy.networkWrapper.sendToServer(new PacketPCBCache(PacketPCBCache.REDO, tileentity.xCoord, tileentity.yCoord,
					tileentity.zCoord));
	}

	@Override
	public void onCallback(GuiCallback gui, Action result, int id) {
		int w = tileentity.getCircuitData().getSize();
		if (result == Action.OK && gui == callbackDelete) {
			if (cb == 1)
				CommonProxy.networkWrapper.sendToServer(new PacketPCBClear((byte) w, tileentity.xCoord, tileentity.yCoord, tileentity.zCoord));
			else {
				w = w == 16 ? 32 : w == 32 ? 64 : 16;
				CommonProxy.networkWrapper.sendToServer(new PacketPCBClear((byte) w, tileentity.xCoord, tileentity.yCoord, tileentity.zCoord));
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
					tileentity.xCoord, tileentity.yCoord, tileentity.zCoord));
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
		Tessellator tessellator = Tessellator.instance;

		double mouseX = (int) getRelBoardX(x);
		double mouseY = (int) getRelBoardY(y);

		endX = (int) mouseX;
		endY = (int) mouseY;

		ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
		int guiScale = scaledresolution.getScaleFactor();

		// Draw the "border"
		drawHollowRect(guiLeft, guiTop, guiRight, guiBottom, editorLeft, editorTop, editorRight, editorBottom, 0xAA3A404A);


		CircuitData data = tileentity.getCircuitData();

		int w = data.getSize();

		mouseDrag(x, y, w, editorLeft, editorTop, editorRight, editorBottom);


		// Draw the name of the CAD
		fontRendererObj.drawString(I18n.format("gui.integratedcircuits.cad.name"), guiLeft + 8, guiTop + 12, 0xFFFFFF);

		GL11.glPushMatrix();
		GL11.glTranslated(guiLeft, guiTop, 0);
		mc.getTextureManager().bindTexture(Resources.RESOURCE_PCB);

		// Render the "board"
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor((int) ((editorLeft) * guiScale),
			this.mc.displayHeight - (int) ((editorTop) * guiScale) - ySizeEditor * guiScale,
			(int) (xSizeEditor * guiScale),
			(int) (ySizeEditor * guiScale));
		GL11.glScalef(tileentity.scale, tileentity.scale, 1F);

		CircuitPartRenderer.renderPerfboard(getRelativeOffX(), getRelativeOffY(), data);
		CircuitPartRenderer.renderParts(tileentity, getRelativeOffX(), getRelativeOffY());

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(1F, 1F, 1F, 1F);


		boolean ctrl = isCtrlKeyDown();
		// Render connections for tunnels
		tessellator.startDrawingQuads();
		for (int x3 = 0; x3 < data.getSize(); x3++) {
			for (int y3 = 0; y3 < data.getSize(); y3++) {
				if (x3 == endX && y3 == endY && data.getPart(new Vec2(x3, y3)) instanceof PartTunnel && selectedPart == null) {
					drawTunnelConnection(x3, y3);
				}
				if (drag && selectedPart == null) {
					Tessellator.instance.setColorRGBA_F(0F, 0F, 1F, 1F);
					CircuitPartRenderer.addQuad(startX * 16 + getRelativeOffX(), startY * 16 + getRelativeOffY(), 0, 0, 16, 16);
				}
				if (ctrl) {
					drawTunnelConnection(x3, y3);
				}
			}
		}
		tessellator.draw();

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(0.6F, 0.6F, 0.6F, 0.7F);

		cadCursor(x, y, tessellator, mouseX, mouseY, data, w, editorLeft, editorTop, editorRight, editorBottom);

		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		GL11.glPopMatrix();

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		tessellator.startDrawingQuads();

		// Draw inner gradient
		drawGradients(tessellator, editorLeft, editorTop, editorRight, editorBottom, 4);

		tessellator.draw();
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		nameField.drawTextBox();
		GL11.glColor3f(1, 1, 1);
	}

	private static void drawHollowRect(int outerLeft, int outerTop, int outerRight, int outerBottom, int innerLeft, int innerTop, int innerRight, int innerBottom, int colour) {
		drawRect(outerLeft, outerTop, innerRight, innerTop, colour);
		drawRect(outerLeft, innerTop, innerLeft, outerBottom, colour);
		drawRect(innerLeft, innerBottom, outerRight, outerBottom, colour);
		drawRect(innerRight, outerTop, outerRight, innerBottom, colour);
	}

	// TODO: Make this less hardcoded?
	private void drawGradients(Tessellator tessellator, int gradientLeft, int gradientTop, int gradientRight, int gradientBottom, int gradientSize) {
		// Top gradient
		tessellator.setColorRGBA_F(0, 0, 0, 0);
		tessellator.addVertex(gradientLeft, gradientTop + gradientSize, 0);
		tessellator.addVertex(gradientRight, gradientTop + gradientSize, 0);
		tessellator.setColorRGBA_F(0, 0, 0, 0.8F);
		tessellator.addVertex(gradientRight, gradientTop, 0);
		tessellator.addVertex(gradientLeft, gradientTop, 0);

		// Bottom gradient
		tessellator.setColorRGBA_F(0, 0, 0, 0.8F);
		tessellator.addVertex(gradientLeft, gradientBottom, 0);
		tessellator.addVertex(gradientRight, gradientBottom, 0);
		tessellator.setColorRGBA_F(0, 0, 0, 0);
		tessellator.addVertex(gradientRight, gradientBottom - gradientSize, 0);
		tessellator.addVertex(gradientLeft, gradientBottom - gradientSize, 0);

		// Left gradient
		tessellator.setColorRGBA_F(0, 0, 0, 0.8F);
		tessellator.addVertex(gradientLeft, gradientTop, 0);
		tessellator.addVertex(gradientLeft, gradientBottom, 0);
		tessellator.setColorRGBA_F(0, 0, 0, 0);
		tessellator.addVertex(gradientLeft + gradientSize, gradientBottom, 0);
		tessellator.addVertex(gradientLeft + gradientSize, gradientTop, 0);

		// Right gradient
		tessellator.setColorRGBA_F(0, 0, 0, 0);
		tessellator.addVertex(gradientRight - gradientSize, gradientTop, 0);
		tessellator.addVertex(gradientRight - gradientSize, gradientBottom, 0);
		tessellator.setColorRGBA_F(0, 0, 0, 0.8F);
		tessellator.addVertex(gradientRight, gradientBottom, 0);
		tessellator.addVertex(gradientRight, gradientTop, 0);


	}

	private void cadCursor(int x, int y, Tessellator tessellator, double mouseX, double mouseY, CircuitData data, int w, int left, int top, int right, int bottom) {
		if (mouseX > 0 && mouseY > 0 && mouseX < w - 1 && mouseY < w - 1 && !isShiftKeyDown() && !blockMouseInput) {
			if (!(x < left || y < top || x > right || y > bottom)) {
				if (!drag && selectedPart != null) {
					mouseX = mouseX * 16 + getRelativeOffX();
					mouseY = mouseY * 16 + getRelativeOffY();
					if (selectedPart.getPart() instanceof PartNull) {
						GL11.glColor3f(0F, 0.4F, 0F);
						GL11.glDisable(GL11.GL_TEXTURE_2D);
						tessellator.startDrawingQuads();
						CircuitPartRenderer.addQuad(mouseX, mouseY, 2 * 16, 0, 16, 16);
						tessellator.draw();
						GL11.glEnable(GL11.GL_TEXTURE_2D);
					}
					CircuitPartRenderer.renderPart(selectedPart, mouseX, mouseY);
				} else if (drag) {
					if (selectedPart == null) {
						GL11.glColor4f(0F, 0F, 1F, 1F);
						GL11.glDisable(GL11.GL_TEXTURE_2D);

						tessellator.startDrawingQuads();
						if (data.getPart(new Vec2(endX, endY)) instanceof PartTunnel) {
							RenderUtils.addLine(startX * 16 + getRelativeOffX() + 8, startY * 16 + getRelativeOffY() + 8, endX * 16 + getRelativeOffX() + 8, endY * 16 + getRelativeOffY() + 8, 4);
						} else {
							double x3 = (x - guiLeft - getRelativeOffX() * tileentity.scale) / tileentity.scale + getRelativeOffX();
							double y3 = (y - guiTop - getRelativeOffY() * tileentity.scale) / tileentity.scale + getRelativeOffY();
							RenderUtils.addLine(startX * 16 + getRelativeOffX() + 8, startY * 16 + getRelativeOffY() + 8, x3, y3, 4);
						}
						tessellator.draw();

						GL11.glEnable(GL11.GL_TEXTURE_2D);
						GL11.glColor4f(0.6F, 0.6F, 0.6F, 0.7F);
					} else if (selectedPart.getPart() instanceof PartWire) {
						PartWire wire = (PartWire) selectedPart.getPart();
						GL11.glTranslated(getRelativeOffX(), getRelativeOffY(), 0);
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

						mouseX = startX;
						mouseY = startY;

						tessellator.startDrawingQuads();
						CircuitPartRenderer.addQuad(mouseX * 16, mouseY * 16, 0, 0, 16, 16);
						if (endY > startY)
							CircuitPartRenderer.addQuad(mouseX * 16, mouseY * 16, 4 * 16, 0, 16, 16);
						else if (endY < startY)
							CircuitPartRenderer.addQuad(mouseX * 16, mouseY * 16, 2 * 16, 0, 16, 16);
						else if (endX > startX)
							CircuitPartRenderer.addQuad(mouseX * 16, mouseY * 16, 3 * 16, 0, 16, 16);
						else if (endX < startX)
							CircuitPartRenderer.addQuad(mouseX * 16, mouseY * 16, 16, 0, 16, 16);

						while (mouseX != endX || mouseY != endY) {
							if (mouseY < endY)
								mouseY++;
							else if (mouseY > endY)
								mouseY--;
							else if (mouseX < endX)
								mouseX++;
							else if (mouseX > endX)
								mouseX--;

							if (mouseY != endY)
								CircuitPartRenderer.addQuad(mouseX * 16, mouseY * 16, 6 * 16, 0, 16, 16);
							else if (mouseY == endY && mouseX == startX) {
								CircuitPartRenderer.addQuad(mouseX * 16, mouseY * 16, 0, 0, 16, 16);
								if (endY > startY)
									CircuitPartRenderer.addQuad(mouseX * 16, mouseY * 16, 2 * 16, 0, 16, 16);
								else if (endY < startY)
									CircuitPartRenderer.addQuad(mouseX * 16, mouseY * 16, 4 * 16, 0, 16, 16);
								if (endX > startX)
									CircuitPartRenderer.addQuad(mouseX * 16, mouseY * 16, 3 * 16, 0, 16, 16);
								else if (endX < startX)
									CircuitPartRenderer.addQuad(mouseX * 16, mouseY * 16, 16, 0, 16, 16);
							} else if (mouseX != endX)
								CircuitPartRenderer.addQuad(mouseX * 16, mouseY * 16, 5 * 16, 0, 16, 16);
							else if (mouseX == endX) {
								CircuitPartRenderer.addQuad(mouseX * 16, mouseY * 16, 0, 0, 16, 16);
								if (endX > startX)
									CircuitPartRenderer.addQuad(mouseX * 16, mouseY * 16, 16, 0, 16, 16);
								else if (endX < startX)
									CircuitPartRenderer.addQuad(mouseX * 16, mouseY * 16, 3 * 16, 0, 16, 16);
							}
						}
						tessellator.draw();
						GL11.glColor3f(1, 1, 1);
						GL11.glTranslated(-getRelativeOffX(), -getRelativeOffY(), 0);
					}
				}
			}
		}
	}

	private void mouseDrag(int x, int y, int w, int left, int top, int right, int bottom) {
		if (Mouse.isButtonDown(0) && (x - lastX != 0 || y - lastY != 0) && isShiftKeyDown()) {
			if (!(x < left || y < top || x > right || y > bottom)) {
				tileentity.offX += (x - lastX) / tileentity.scale;
				tileentity.offY += (y - lastY) / tileentity.scale;
			}
		}
		lastX = x;
		lastY = y;

		double maxX = ((xSizeEditor + 16) / tileentity.scale) - 16;
		double minX = (-(w * 16) + editorLeft / tileentity.scale) + 16;
		double maxY = (editorBottom / tileentity.scale) - 16;
		double minY = (-(w * 16) + editorTop / tileentity.scale) + 16;
		tileentity.offX = tileentity.offX > maxX ? maxX : tileentity.offX < minX ? minX : tileentity.offX;
		tileentity.offY = tileentity.offY > maxY ? maxY : tileentity.offY < minY ? minY : tileentity.offY;

	}

	private void drawTunnelConnection(int x, int y) {
		Vec2 pos = new Vec2(x, y);
		CircuitPart part = tileentity.getCircuitData().getPart(pos);
		if (!(part instanceof PartTunnel))
			return;

		PartTunnel pt = (PartTunnel) part;
		Vec2 pos2 = pt.getConnectedPos(pos, tileentity);

		double x3 = x * 16 + getRelativeOffX();
		double y3 = y * 16 + getRelativeOffY();

		if (pt.getInput(pos, tileentity) || pt.getProperty(pos, tileentity, pt.PROP_IN)) {
			Tessellator.instance.setColorRGBA_F(1F, 0F, 0F, 1F);
		} else {
			Tessellator.instance.setColorRGBA_F(0F, 0F, 1F, 1F);
		}

		if (pt.isConnected(pos2)) {
			double x4 = pos2.x * 16 + getRelativeOffX();
			double y4 = pos2.y * 16 + getRelativeOffY();

			RenderUtils.addLine(x3 + 8, y3 + 8, x4 + 8, y4 + 8, 4);
			CircuitPartRenderer.addQuad(x4, y4, 0, 0, 16, 16);
		}
		CircuitPartRenderer.addQuad(x3, y3, 0, 0, 16, 16);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) {
		GL11.glColor3f(1, 1, 1);
		CircuitData data = tileentity.getCircuitData();

		int x2 = (int) getRelBoardX(x);
		int y2 = (int) getRelBoardY(y);

		ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
		int guiScale = scaledresolution.getScaleFactor();

		int w = data.getSize();
		if (x2 >= 0 && y2 >= 0 && x2 < w && y2 < w && !blockMouseInput && !isShiftKeyDown()) {
			if (x >= editorLeft && x < editorRight && y >= editorTop && y < editorBottom) {
				Vec2 pos = new Vec2(x2, y2);
				CircuitPart part = data.getPart(pos);
				if (!(part instanceof PartNull || part instanceof PartWire || part instanceof PartNullCell)) {
					ArrayList<String> text = Lists.newArrayList();
					text.add(part.getLocalizedName(pos, tileentity));
					text.addAll(part.getInformation(pos, tileentity, selectedPart == null, isCtrlKeyDown()));
					drawHoveringText(text, x - guiLeft, y - guiTop, this.fontRendererObj);
				}
			}
		}
		if (hoveredElement != null)
			drawHoveringText(hoveredElement.getHoverInformation(), x - guiLeft, y - guiTop, this.fontRendererObj);
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		fontRendererObj.drawString((int) (tileentity.scale * 100) + "%", this.xSize - 62, this.ySize - 15, 0xFFFFFF);
		GL11.glColor3f(1, 1, 1);
	}

	@Override
	protected void mouseClicked(int x, int y, int flag) {
		nameField.mouseClicked(x, y, flag);
		if (blockMouseInput) {
			super.mouseClicked(x, y, flag);
			return;
		}

		if (x < editorLeft || y < editorTop || x > editorRight || y > editorBottom) {
			super.mouseClicked(x, y, flag);
			return;
		}

		CircuitData data = tileentity.getCircuitData();

		boolean ctrlDown = isCtrlKeyDown();
		int x2 = (int) getRelBoardX(x);
		int y2 = (int) getRelBoardY(y);
		int w = data.getSize();

		drag = false;
		if (x2 > 0 && y2 > 0 && x2 < w - 1 && y2 < w - 1 && !isShiftKeyDown()) {
			if (selectedPart == null) {
				Vec2 pos = new Vec2(x2, y2);
				CircuitPart cp = data.getPart(pos);
				if (cp instanceof IConfigurableDelay && ctrlDown) {
					timedPart = new CircuitRenderWrapper(tileentity.getCircuitData(), cp, pos);
					labelTimed.setText(String.format("Current delay: %s ticks",
							((IConfigurableDelay) cp).getConfigurableDelay(pos, tileentity)));
					callbackTimed.display();
				} else if (cp instanceof PartTunnel) {
					startX = x2;
					startY = y2;
					drag = true;
				} else {
					CommonProxy.networkWrapper.sendToServer(new PacketPCBChangePart(x2, y2, flag, ctrlDown, tileentity.xCoord,
							tileentity.yCoord, tileentity.zCoord));
				}
			} else if (selectedPart.getPart() instanceof PartWire) {
				startX = x2;
				startY = y2;
				drag = true;
			} else {
				int newID = CircuitPart.getId(selectedPart.getPart());
				if (newID != tileentity.getCircuitData().getID(new Vec2(x2, y2))) {
					CommonProxy.networkWrapper.sendToServer(new PacketPCBChangePart(new int[] { x2, y2,
							newID, selectedPart.getState() },
							!(selectedPart.getPart() instanceof PartNull), tileentity.xCoord, tileentity.yCoord, tileentity.zCoord));
				}
			}
		}

		super.mouseClicked(x, y, flag);
	}

	@Override
	protected void mouseClickMove(int x, int y, int par3, long par4) {
		super.mouseClickMove(x, y, par3, par4);

		if (selectedPart != null && selectedPart.getPart() instanceof PartNull) {
			int x2 = (int) getRelBoardX(x);
			int y2 = (int) getRelBoardY(y);
			int w = tileentity.getCircuitData().getSize();
			boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

			if (x2 > 0 && y2 > 0 && x2 < w - 1 && y2 < w - 1 && !shiftDown) {
				Vec2 pos = new Vec2(x2, y2);
				if (!(tileentity.getCircuitData().getPart(pos) instanceof PartNull)) {
					CommonProxy.networkWrapper.sendToServer(new PacketPCBChangePart(new int[] { x2, y2, 0, 0 }, false,
							tileentity.xCoord, tileentity.yCoord, tileentity.zCoord));
				}
			}
		}
	}

	private void scale(int i) {
		int w = tileentity.getCircuitData().getSize();

		double ow = w * 16 * tileentity.scale;
		float oldScale = tileentity.scale;

		int index = scales.indexOf(tileentity.scale);

		if (i > 0 && index + 1 < scales.size())
			tileentity.scale = scales.get(index + 1);
		if (i < 0 && index - 1 >= 0)
			tileentity.scale = scales.get(index - 1);

		if (i != 0) {
			buttonMinus.enabled = true;
			buttonPlus.enabled = true;

			if (tileentity.scale == 0.17F)
				buttonMinus.enabled = false;
			if (tileentity.scale == 2F)
				buttonPlus.enabled = false;
		}

		double change = (double) tileentity.scale / (double) oldScale;
		double nw = w * 16 * tileentity.scale;

		if (i > 0) {
			tileentity.offX = tileentity.offX / change;
			tileentity.offY = tileentity.offY / change;
			tileentity.offX -= (nw - ow) / 2 / tileentity.scale;
			tileentity.offY -= (nw - ow) / 2 / tileentity.scale;
		} else if (i < 0) {
			tileentity.offX -= (nw - ow) / 2 / oldScale;
			tileentity.offY -= (nw - ow) / 2 / oldScale;
			tileentity.offX = tileentity.offX / change;
			tileentity.offY = tileentity.offY / change;
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
			CommonProxy.networkWrapper.sendToServer(new PacketPCBCache(PacketPCBCache.SNAPSHOT, tileentity.xCoord, tileentity.yCoord, tileentity.zCoord));
		else if (button != -1 && drag) {
			int w = tileentity.getCircuitData().getSize();

			if (endX > 0 && endY > 0 && endX < w - 1 && endY < w - 1 && !blockMouseInput) {
				if (selectedPart == null) {
					PartTunnel pt = CircuitPart.getPart(PartTunnel.class);
					
					Vec2 first = new Vec2(startX, startY);
					Vec2 second = new Vec2(endX, endY);
					
					if (tileentity.getCircuitData().getPart(second) instanceof PartTunnel && !first.equals(second)) {
						
						List<Integer> data = Lists.newArrayList();

						if (pt.isConnected(pt.getConnectedPos(first, tileentity))) {
							Vec2 part = pt.getConnectedPos(first, tileentity);
							data.add(part.x);
							data.add(part.y);
							data.add(CircuitPart.getId(pt));
							data.add(pt.setConnectedPos(tileentity.getCircuitData().getMeta(part), new Vec2(255, 255)));
						}

						data.add(first.x);
						data.add(first.y);
						data.add(CircuitPart.getId(pt));
						data.add(pt.setConnectedPos(tileentity.getCircuitData().getMeta(first), second));
						
						if (pt.isConnected(pt.getConnectedPos(second, tileentity))) {
							Vec2 part = pt.getConnectedPos(second, tileentity);
							data.add(part.x);
							data.add(part.y);
							data.add(CircuitPart.getId(pt));
							data.add(pt.setConnectedPos(tileentity.getCircuitData().getMeta(part), new Vec2(255, 255)));
						}
						
						data.add(second.x);
						data.add(second.y);
						data.add(CircuitPart.getId(pt));
						data.add(pt.setConnectedPos(tileentity.getCircuitData().getMeta(second), first));

						CommonProxy.networkWrapper.sendToServer(new PacketPCBChangePart(Ints.toArray(data), true, tileentity.xCoord, tileentity.yCoord, tileentity.zCoord));
					}
				} else if(selectedPart.getPart() instanceof PartWire) {

					int id = CircuitPart.getId(selectedPart.getPart());
					int state = selectedPart.getState();

					ArrayList<Vec2> list = new ArrayList<Vec2>();
					list.add(new Vec2(startX, startY));
					while (startX != endX || startY != endY) {
						if (startY < endY)
							startY++;
						else if (startY > endY)
							startY--;
						else if (startX < endX)
							startX++;
						else if (startX > endX)
							startX--;
						list.add(new Vec2(startX, startY));
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
					CommonProxy.networkWrapper.sendToServer(new PacketPCBChangePart(data, true, tileentity.xCoord, tileentity.yCoord, tileentity.zCoord));
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
			CommonProxy.networkWrapper.sendToServer(new PacketPCBCache(PacketPCBCache.UNDO, tileentity.xCoord, tileentity.yCoord,
					tileentity.zCoord));
		else if (par2 == Keyboard.KEY_Y && isCtrlKeyDown())
			CommonProxy.networkWrapper.sendToServer(new PacketPCBCache(PacketPCBCache.REDO, tileentity.xCoord, tileentity.yCoord,
					tileentity.zCoord));
		else
			super.keyTyped(par1, par2);

		if (!oname.equals(nameField.getText()))
			CommonProxy.networkWrapper.sendToServer(new PacketPCBChangeName(MiscUtils.thePlayer(), nameField.getText(),
					tileentity.xCoord, tileentity.yCoord, tileentity.zCoord));
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Config.showConfirmMessage.set(checkboxDelete.isChecked());
		Config.save();

		NEIAddon.hideGUI(false);
	}

	@Override
	public void setCurrentItem(IHoverable hoverable) {
		this.hoveredElement = hoverable;
	}
}
