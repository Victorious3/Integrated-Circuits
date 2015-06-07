package moe.nightfall.vic.integratedcircuits.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import moe.nightfall.vic.integratedcircuits.Config;
import moe.nightfall.vic.integratedcircuits.ContainerPCBLayout;
import moe.nightfall.vic.integratedcircuits.client.Resources;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IGuiCallback;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverableHandler;
import moe.nightfall.vic.integratedcircuits.client.gui.component.GuiCallback;
import moe.nightfall.vic.integratedcircuits.client.gui.component.GuiCallback.Action;
import moe.nightfall.vic.integratedcircuits.client.gui.component.GuiCheckBoxExt;
import moe.nightfall.vic.integratedcircuits.client.gui.component.GuiIO;
import moe.nightfall.vic.integratedcircuits.client.gui.component.GuiIOMode;
import moe.nightfall.vic.integratedcircuits.client.gui.component.GuiIconButton;
import moe.nightfall.vic.integratedcircuits.client.gui.component.GuiLabel;
import moe.nightfall.vic.integratedcircuits.client.gui.component.GuiPartChooser;
import moe.nightfall.vic.integratedcircuits.client.gui.component.GuiRollover;
import moe.nightfall.vic.integratedcircuits.client.gui.component.GuiStateLabel;
import moe.nightfall.vic.integratedcircuits.compat.NEIAddon;
import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPart;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer.CircuitRenderWrapper;
import moe.nightfall.vic.integratedcircuits.cp.part.PartNull;
import moe.nightfall.vic.integratedcircuits.cp.part.PartTunnel;
import moe.nightfall.vic.integratedcircuits.cp.part.PartWire;
import moe.nightfall.vic.integratedcircuits.cp.part.cell.PartNullCell;
import moe.nightfall.vic.integratedcircuits.cp.part.timed.IConfigurableDelay;
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

import codechicken.lib.math.MathHelper;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import cpw.mods.fml.client.config.GuiButtonExt;

public class GuiPCBLayout extends GuiContainer implements IGuiCallback, IHoverableHandler {

	public TileEntityPCBLayout tileentity;
	
	// Because of private.
	public GuiPartChooser selectedChooser;
	public boolean blockMouseInput = false;
	private IHoverable hoveredElement;
	
	public CircuitRenderWrapper selectedPart;
	
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
	
	private GuiTextField nameField;
	private GuiButtonExt buttonPlus;
	private GuiButtonExt buttonMinus;
	private GuiButtonExt buttonSize;
	private GuiIOMode checkN;
	private GuiIOMode checkE;
	private GuiIOMode checkS;
	private GuiIOMode checkW;

	// Used by the wires
	private int startX, startY, endX, endY;
	private boolean drag;
	
	private static final float SCALE = 16f; 

	// Callbacks
	private GuiCallback<GuiPCBLayout> callbackDelete;
	private GuiCheckBoxExt checkboxDelete;
	private GuiCallback<GuiPCBLayout> callbackTimed;
	private GuiLabel labelTimed;
	private CircuitRenderWrapper timedPart;
	
	// Simulation
	private GuiStateLabel labelPlayState;

	private int callback;
	
	private static final List<Float> scales = Arrays.asList(0.17F, 0.2F, 0.25F, 0.33F, 0.5F, 0.67F, 1F, 1.5F, 2F);

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

		// GUI rollover on the left
		GuiRollover rollover = new GuiRollover(90, guiLeft + 5, guiTop + 5, height - 10, Resources.RESOURCE_GUI_CAD_BACKGROUND)
			.addCategory("Label", 0, 0)
			.addCategory("Area", 0, 16)
			.addCategory("Simulation", 0, 32,
					new GuiIconButton(90, 0, 0, 18, 18, Resources.RESOURCE_GUI_CAD_BACKGROUND).setIcon(0, 0),
					new GuiButtonExt(91, 0, 0, 18, 18, "test")
			);

		this.buttonList.add(rollover);

		refreshUI();
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 8)
			scale(tileentity.offX, tileentity.offY, 1);
		else if (button.id == 9)
			scale(tileentity.offX, tileentity.offY, -1);
		else if (button.id == 10) {
			callback = 1;
			if (checkboxDelete.isChecked())
				callbackDelete.display();
			else
				onCallback(callbackDelete, Action.OK, 0);
		} else if (button.id == 11) {
			callback = 2;
			if (checkboxDelete.isChecked())
				callbackDelete.display();
			else
				onCallback(callbackDelete, Action.OK, 0);
		} else if (button.id == 13)
			CommonProxy.networkWrapper.sendToServer(new PacketPCBIO(true, tileentity.xCoord, tileentity.yCoord, tileentity.zCoord));
		else if (button.id == 12)
			CommonProxy.networkWrapper.sendToServer(new PacketPCBIO(false, tileentity.xCoord, tileentity.yCoord, tileentity.zCoord));
		else if (button.id == 84)
			CommonProxy.networkWrapper.sendToServer(new PacketPCBCache(PacketPCBCache.UNDO, tileentity.xCoord, tileentity.yCoord, tileentity.zCoord));
		else if (button.id == 85)
			CommonProxy.networkWrapper.sendToServer(new PacketPCBCache(PacketPCBCache.REDO, tileentity.xCoord, tileentity.yCoord, tileentity.zCoord));
	}

	//Functions to convert between screen coordinates and circuit board coordinates
	private double boardAbs2RelX(double absX) {
		return (absX - getAbsBoardOffsetX()) / getScaleFactor() + getBoardSize()/2.0;
	}
	
	private double boardAbs2RelY(double absY) {
		return (absY - getAbsBoardOffsetY()) / getScaleFactor() + getBoardSize()/2.0;
	}
	
	private double boardRel2AbsX(double relX) {
		return (relX - getBoardSize()/2.0) * getScaleFactor() + getAbsBoardOffsetX();
	}
	
	private double boardRel2AbsY(double relY) {
		return (relY - getBoardSize()/2.0) * getScaleFactor() + getAbsBoardOffsetY();
	}
	
	private float getScaleFactor() {
		return SCALE * tileentity.scale;
	}
	
	private int getBoardSize() {
		return tileentity.getCircuitData().getSize();
	}
	
	private double getAbsBoardSize() {
		return getScaleFactor() * getBoardSize();
	}
	
	private double getAbsBoardOffsetX() {
		return editorLeft + xSizeEditor/2.0 + tileentity.offX;
	}
	
	private double getAbsBoardOffsetY() {
		return editorTop + ySizeEditor/2.0 + tileentity.offY;
	}
	
	private double getBoardLeft() {
		return boardRel2AbsX(0);
	}
	
	private double getBoardRight() {
		return boardRel2AbsX(getBoardSize());
	}
	
	private double getBoardTop() {
		return boardRel2AbsY(0);
	}
	
	private double getBoardBottom() {
		return boardRel2AbsY(getBoardSize());
	}

	private void calculateSizes() {
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

	private static int getOffsetCentre(int topOffset, int bottomOffset, int fullLength, int thingLength) {
		return (topOffset + fullLength - bottomOffset - thingLength) / 2;
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
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int x, int y) {
		hoveredElement = null;
		
		double relX = boardAbs2RelX(x);
		double relY = boardAbs2RelY(y);

		endX = (int) relX;
		endY = (int) relY;

		mouseDrag(x, y, editorLeft, editorTop, editorRight, editorBottom);
		
		ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
		int guiScale = scaledresolution.getScaleFactor();
		
		// Draw the "border"
		drawHollowRect(guiLeft, guiTop, guiRight, guiBottom, editorLeft, editorTop, editorRight, editorBottom, 0xAA3A404A);

		// Draw the name of the CAD
		fontRendererObj.drawString(I18n.format("gui.integratedcircuits.cad.name"), guiLeft + 45, guiTop + 12, 0xFFFFFF);

		renderCircuitBoard(relX, relY, guiScale);
		//Draw inner gradient
		drawGradients(editorLeft, editorTop, editorRight, editorBottom, 4);

		nameField.drawTextBox();
		GL11.glColor3f(1, 1, 1);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		GL11.glColor3f(1, 1, 1);
		CircuitData data = tileentity.getCircuitData();

		int gridX = (int) boardAbs2RelX(mouseX);
		int gridY = (int) boardAbs2RelY(mouseY);

		int w = data.getSize();
		if (gridX >= 0 && gridY >= 0 && gridX < w && gridY < w && !blockMouseInput && !isShiftKeyDown()) {
			if (mouseX >= editorLeft && mouseX < editorRight && mouseY >= editorTop && mouseY < editorBottom) {
				Vec2 pos = new Vec2(gridX, gridY);
				CircuitPart part = data.getPart(pos);
				if (!(part instanceof PartNull || part instanceof PartWire || part instanceof PartNullCell)) {
					ArrayList<String> text = Lists.newArrayList();
					text.add(part.getLocalizedName(pos, tileentity));
					text.addAll(part.getInformation(pos, tileentity, selectedPart == null, isCtrlKeyDown()));
					drawHoveringText(text, mouseX - guiLeft, mouseY - guiTop, this.fontRendererObj);
				}
			}
		}
		if (hoveredElement != null)
			drawHoveringText(hoveredElement.getHoverInformation(), mouseX - guiLeft, mouseY - guiTop, this.fontRendererObj);
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		fontRendererObj.drawString((int) (tileentity.scale * 100) + "%", this.xSize - 62, this.ySize - 15, 0xFFFFFF);
		GL11.glColor3f(1, 1, 1);
	}
	
	private static void drawHollowRect(int outerLeft, int outerTop, int outerRight, int outerBottom, int innerLeft, int innerTop, int innerRight, int innerBottom, int colour) {
		drawRect(outerLeft, outerTop, innerRight, innerTop, colour);
		drawRect(outerLeft, innerTop, innerLeft, outerBottom, colour);
		drawRect(innerLeft, innerBottom, outerRight, outerBottom, colour);
		drawRect(innerRight, outerTop, outerRight, innerBottom, colour);
	}

	private void drawGradients(int gradientLeft, int gradientTop, int gradientRight, int gradientBottom, int gradientSize) {
		Tessellator tes = Tessellator.instance;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		tes.startDrawingQuads();
		// Top gradient
		tes.setColorRGBA_F(0, 0, 0, 0);
		tes.addVertex(gradientLeft, gradientTop + gradientSize, 0);
		tes.addVertex(gradientRight, gradientTop + gradientSize, 0);
		tes.setColorRGBA_F(0, 0, 0, 0.8F);
		tes.addVertex(gradientRight, gradientTop, 0);
		tes.addVertex(gradientLeft, gradientTop, 0);

		// Bottom gradient
		tes.setColorRGBA_F(0, 0, 0, 0.8F);
		tes.addVertex(gradientLeft, gradientBottom, 0);
		tes.addVertex(gradientRight, gradientBottom, 0);
		tes.setColorRGBA_F(0, 0, 0, 0);
		tes.addVertex(gradientRight, gradientBottom - gradientSize, 0);
		tes.addVertex(gradientLeft, gradientBottom - gradientSize, 0);

		// Left gradient
		tes.setColorRGBA_F(0, 0, 0, 0.8F);
		tes.addVertex(gradientLeft, gradientTop, 0);
		tes.addVertex(gradientLeft, gradientBottom, 0);
		tes.setColorRGBA_F(0, 0, 0, 0);
		tes.addVertex(gradientLeft + gradientSize, gradientBottom, 0);
		tes.addVertex(gradientLeft + gradientSize, gradientTop, 0);

		// Right gradient
		tes.setColorRGBA_F(0, 0, 0, 0);
		tes.addVertex(gradientRight - gradientSize, gradientTop, 0);
		tes.addVertex(gradientRight - gradientSize, gradientBottom, 0);
		tes.setColorRGBA_F(0, 0, 0, 0.8F);
		tes.addVertex(gradientRight, gradientBottom, 0);
		tes.addVertex(gradientRight, gradientTop, 0);

		tes.draw();
		
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	private void renderCircuitBoard(double mouseX, double mouseY, int guiScale) {
		CircuitData data = tileentity.getCircuitData();
		
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(editorLeft * guiScale, this.mc.displayHeight - editorBottom * guiScale,
				xSizeEditor * guiScale, ySizeEditor * guiScale);
		
		GL11.glPushMatrix();
		//Scale and translate to the center of the screen
		GL11.glTranslated(getAbsBoardOffsetX(), getAbsBoardOffsetY(), 0);
		GL11.glScalef(getScaleFactor(), getScaleFactor(), 1F);
		GL11.glTranslated(-getBoardSize()/2.0, -getBoardSize()/2.0, 0);

		//Render the circuit board
		CircuitPartRenderer.renderPerfboard(data);
		CircuitPartRenderer.renderParts(tileentity);
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		if(!isShiftKeyDown())
			renderTunnelConnections(data, isCtrlKeyDown());
		renderCadCursor(mouseX, mouseY, data, data.getSize());
		GL11.glDisable(GL11.GL_BLEND);

		GL11.glPopMatrix();
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}
	
	private void renderTunnelConnections(CircuitData data, boolean ctrl) {
		Tessellator tes = Tessellator.instance;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		tes.startDrawingQuads();
		for (int x = 0; x < data.getSize(); x++)
			for (int y = 0; y < data.getSize(); y++) {
				if (ctrl || x == endX && y == endY && data.getPart(new Vec2(x, y)) instanceof PartTunnel && selectedPart == null)
					drawTunnelConnection(x, y);
				if (drag && selectedPart == null) {
					tes.setColorRGBA_F(0, 0, 1, 1);
					CircuitPartRenderer.addQuad(startX, startY, 0, 0, 1, 1);
				}
			}
		tes.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	private void drawTunnelConnection(int firstX, int firstY) {
		Vec2 pos = new Vec2(firstX, firstY);
		CircuitPart part = tileentity.getCircuitData().getPart(pos);
		if (!(part instanceof PartTunnel))
			return;

		PartTunnel pt = (PartTunnel) part;
		Vec2 pos2 = pt.getConnectedPos(pos, tileentity);

		if (pt.getInput(pos, tileentity) || pt.getProperty(pos, tileentity, pt.PROP_IN)) {
			Tessellator.instance.setColorRGBA_F(1F, 0F, 0F, 1F);
		} else {
			Tessellator.instance.setColorRGBA_F(0F, 0F, 1F, 1F);
		}

		if (pt.isConnected(pos2)) {
			double secondX = pos2.x;
			double secondY = pos2.y;

			RenderUtils.addLine(firstX + 0.5, firstY + 0.5, secondX + 0.5, secondY + 0.5, 0.25);
			CircuitPartRenderer.addQuad(secondX, secondY, 0, 0, 1, 1);
		}
		CircuitPartRenderer.addQuad(firstX, firstY, 0, 0, 1, 1);
	}

	private void renderCadCursor(double mouseX, double mouseY, CircuitData data, int size) {
		Tessellator tes = Tessellator.instance;
		int gridX = (int) mouseX;
		int gridY = (int) mouseY;
		if (gridX > 0 && gridY > 0 && gridX < size - 1 && gridY < size - 1 && !isShiftKeyDown() && !blockMouseInput) {
			if (!drag && selectedPart != null) {
				if (selectedPart.getPart() instanceof PartNull) {
					GL11.glColor3f(0F, 0.4F, 0F);
					GL11.glDisable(GL11.GL_TEXTURE_2D);
					tes.startDrawingQuads();
					CircuitPartRenderer.addQuad(gridX, gridY, 0, 0, 1, 1);
					tes.draw();
					GL11.glEnable(GL11.GL_TEXTURE_2D);
				}
				
				final int PART_SIZE = CircuitPartRenderer.PART_SIZE;
				GL11.glPushMatrix();
				GL11.glScaled(1F / PART_SIZE, 1F / PART_SIZE, 1);
				CircuitPartRenderer.renderPart(selectedPart, gridX * PART_SIZE, gridY * PART_SIZE);
				GL11.glPopMatrix();
			} else if (drag) {
				if (selectedPart == null) {
					GL11.glColor4f(0F, 0F, 1F, 1F);
					GL11.glDisable(GL11.GL_TEXTURE_2D);

					tes.startDrawingQuads();
					if (data.getPart(new Vec2(endX, endY)) instanceof PartTunnel) {
						RenderUtils.addLine(startX + 0.5, startY + 0.5, endX + 0.5, endY + 0.5, 0.25);
					} else {
						RenderUtils.addLine(startX + 0.5, startY + 0.5, mouseX, mouseY, 0.25);
					}
					tes.draw();

					GL11.glEnable(GL11.GL_TEXTURE_2D);
					GL11.glColor4f(0.6F, 0.6F, 0.6F, 0.7F);
				} else if (selectedPart.getPart() instanceof PartWire) {
					PartWire wire = (PartWire) selectedPart.getPart();
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
					renderDraggedWire();
					GL11.glColor3f(1, 1, 1);
				}
			}
		}
	}
	
	private void renderDraggedWire() {
		int x = startX;
		int y = startY;

		Tessellator.instance.startDrawingQuads();
		CircuitPartRenderer.addQuad(x, y, 0, 0, 1, 1, 1, 1, 16, 16, 0);
		if (endY > startY)
			CircuitPartRenderer.addQuad(x, y, 4, 0, 1, 1, 1, 1, 16, 16, 0);
		else if (endY < startY)
			CircuitPartRenderer.addQuad(x, y, 2, 0, 1, 1, 1, 1, 16, 16, 0);
		else if (endX > startX)
			CircuitPartRenderer.addQuad(x, y, 3, 0, 1, 1, 1, 1, 16, 16, 0);
		else if (endX < startX)
			CircuitPartRenderer.addQuad(x, y, 1, 0, 1, 1, 1, 1, 16, 16, 0);

		while (x != endX || y != endY) {
			if (y < endY)
				y++;
			else if (y > endY)
				y--;
			else if (x < endX)
				x++;
			else if (x > endX)
				x--;

			if (y != endY)
				CircuitPartRenderer.addQuad(x, y, 6, 0, 1, 1, 1, 1, 16, 16, 0);
			else if (y == endY && x == startX) {
				CircuitPartRenderer.addQuad(x, y, 0, 0, 1, 1, 1, 1, 16, 16, 0);
				if (endY > startY)
					CircuitPartRenderer.addQuad(x, y, 2, 0, 1, 1, 1, 1, 16, 16, 0);
				else if (endY < startY)
					CircuitPartRenderer.addQuad(x, y, 4, 0, 1, 1, 1, 1, 16, 16, 0);
				if (endX > startX)
					CircuitPartRenderer.addQuad(x, y, 3, 0, 1, 1, 1, 1, 16, 16, 0);
				else if (endX < startX)
					CircuitPartRenderer.addQuad(x, y, 1, 0, 1, 1, 1, 1, 16, 16, 0);
			} else if (x != endX)
				CircuitPartRenderer.addQuad(x, y, 5, 0, 1, 1, 1, 1, 16, 16, 0);
			else if (x == endX) {
				CircuitPartRenderer.addQuad(x, y, 0, 0, 1, 1, 1, 1, 16, 16, 0);
				if (endX > startX)
					CircuitPartRenderer.addQuad(x, y, 1, 0, 1, 1, 1, 1, 16, 16, 0);
				else if (endX < startX)
					CircuitPartRenderer.addQuad(x, y, 3, 0, 1, 1, 1, 1, 16, 16, 0);
			}
		}
		Tessellator.instance.draw();
	}
	
	@Override
	public void handleMouseInput() {
		int wheelD = Mouse.getEventDWheel();
		if (wheelD != 0) {
			int mouseX = Mouse.getEventX() * this.width/this.mc.displayWidth;
			int mouseY = this.height - 1 - Mouse.getEventY() * this.height/this.mc.displayHeight;
			if (mouseX >= getBoardLeft() && mouseX >= editorLeft && mouseX < getBoardRight() && mouseX < editorRight
					&& mouseY >= getBoardTop() && mouseY >= editorTop && mouseY < getBoardBottom() && mouseY < editorBottom)
				scale(mouseX - (editorLeft + xSizeEditor/2.0), mouseY - (editorTop + ySizeEditor/2.0), wheelD);
			else
				scale(tileentity.offX, tileentity.offY, wheelD);
		}
		super.handleMouseInput();
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int flag) {
		nameField.mouseClicked(mouseX, mouseY, flag);
		if (blockMouseInput) {
			super.mouseClicked(mouseX, mouseY, flag);
			return;
		}

		if (mouseX < editorLeft || mouseY < editorTop || mouseX > editorRight || mouseY > editorBottom) {
			super.mouseClicked(mouseX, mouseY, flag);
			return;
		}

		CircuitData data = tileentity.getCircuitData();

		boolean ctrlDown = isCtrlKeyDown();
		int gridX = (int) boardAbs2RelX(mouseX);
		int gridY = (int) boardAbs2RelY(mouseY);
		int w = data.getSize();

		drag = false;
		if (gridX > 0 && gridY > 0 && gridX < w - 1 && gridY < w - 1 && !isShiftKeyDown()) {
			if (selectedPart == null) {
				Vec2 pos = new Vec2(gridX, gridY);
				CircuitPart cp = data.getPart(pos);
				if (cp instanceof IConfigurableDelay && ctrlDown) {
					timedPart = new CircuitRenderWrapper(tileentity.getCircuitData(), cp, pos);
					labelTimed.setText(String.format("Current delay: %s ticks",
							((IConfigurableDelay) cp).getConfigurableDelay(pos, tileentity)));
					callbackTimed.display();
				} else if (cp instanceof PartTunnel) {
					startX = gridX;
					startY = gridY;
					drag = true;
				} else {
					CommonProxy.networkWrapper.sendToServer(new PacketPCBChangePart(gridX, gridY, flag, ctrlDown, tileentity.xCoord,
							tileentity.yCoord, tileentity.zCoord));
				}
			} else if (selectedPart.getPart() instanceof PartWire) {
				startX = gridX;
				startY = gridY;
				drag = true;
			} else {
				int newID = CircuitPart.getId(selectedPart.getPart());
				if (newID != tileentity.getCircuitData().getID(new Vec2(gridX, gridY))) {
					CommonProxy.networkWrapper.sendToServer(new PacketPCBChangePart(new int[] { gridX, gridY,
							newID, selectedPart.getState() },
							!(selectedPart.getPart() instanceof PartNull), tileentity.xCoord, tileentity.yCoord, tileentity.zCoord));
				}
			}
		}

		super.mouseClicked(mouseX, mouseY, flag);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceClick) {
		super.mouseClickMove(mouseX, mouseY, button, timeSinceClick);

		if (selectedPart != null && selectedPart.getPart() instanceof PartNull) {
			int boardX = (int) boardAbs2RelX(mouseX);
			int boardY = (int) boardAbs2RelY(mouseY);
			int w = tileentity.getCircuitData().getSize();
			boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

			if (boardX > 0 && boardY > 0 && boardX < w - 1 && boardY < w - 1 && !shiftDown) {
				Vec2 pos = new Vec2(boardX, boardY);
				if (!(tileentity.getCircuitData().getPart(pos) instanceof PartNull)) {
					CommonProxy.networkWrapper.sendToServer(new PacketPCBChangePart(new int[] { boardX, boardY, 0, 0 }, false,
							tileentity.xCoord, tileentity.yCoord, tileentity.zCoord));
				}
			}
		}
	}

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
						
						if (pt.isConnected(pt.getConnectedPos(second, tileentity))) {
							Vec2 part = pt.getConnectedPos(second, tileentity);
							data.add(part.x);
							data.add(part.y);
							data.add(CircuitPart.getId(pt));
							data.add(pt.setConnectedPos(tileentity.getCircuitData().getMeta(part), new Vec2(255, 255)));
						}

						data.add(first.x);
						data.add(first.y);
						data.add(CircuitPart.getId(pt));
						data.add(pt.setConnectedPos(tileentity.getCircuitData().getMeta(first), second));
						
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
	
	private void mouseDrag(int x, int y, int left, int top, int right, int bottom) {
		if (Mouse.isButtonDown(0) && (x - lastX != 0 || y - lastY != 0) && isShiftKeyDown()) {
			if (!(x < left || y < top || x > right || y > bottom)) {
				tileentity.offX += (x - lastX);
				tileentity.offY += (y - lastY);
				clipOffsets();
			}
		}
		
		lastX = x;
		lastY = y;
	}
	
	private void scale(double centerX, double centerY, int i) {
		int index = scales.indexOf(tileentity.scale);

		if (i > 0 && index + 1 < scales.size())
			scaleAround(centerX, centerY, tileentity.scale, scales.get(index + 1));
		if (i < 0 && index - 1 >= 0)
			scaleAround(centerX, centerY, tileentity.scale, scales.get(index - 1));

		buttonMinus.enabled = true;
		buttonPlus.enabled = true;

		if (tileentity.scale == scales.get(0))
			buttonMinus.enabled = false;
		if (tileentity.scale == scales.get(scales.size()-1))
			buttonPlus.enabled = false;
	}
	
	private void scaleAround(double centerX, double centerY, float from, float to) {
		tileentity.scale = to;
		double factor = to / from;
		if (centerX != tileentity.offX)
			tileentity.offX = centerX + factor * (tileentity.offX - centerX);
		if (centerY != tileentity.offY)
			tileentity.offY = centerY + factor * (tileentity.offY - centerY);
		clipOffsets();
	}
	
	private void clipOffsets() {
		double innerSize = getScaleFactor() * (getBoardSize() - 2);
		double limitX = (xSizeEditor + innerSize)/2.0;
		double limitY = (ySizeEditor + innerSize)/2.0;
		
		tileentity.offX = MathHelper.clip(tileentity.offX, -limitX, limitX);
		tileentity.offY = MathHelper.clip(tileentity.offY, -limitY, limitY);
	}

	@Override
	public void onCallback(GuiCallback gui, Action result, int id) {
		int w = tileentity.getCircuitData().getSize();
		if (result == Action.OK && gui == callbackDelete) {
			if (callback == 1)
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

	public List getButtonList() {
		return buttonList;
	}

	@Override
	public void updateScreen() {
		nameField.updateCursorCounter();
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
