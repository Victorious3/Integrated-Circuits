package moe.nightfall.vic.integratedcircuits.client.gui;

import java.util.Arrays;

import moe.nightfall.vic.integratedcircuits.client.Part7SegmentRenderer;
import moe.nightfall.vic.integratedcircuits.client.Resources;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverableHandler;
import moe.nightfall.vic.integratedcircuits.gate.Gate7Segment;
import moe.nightfall.vic.integratedcircuits.net.Packet7SegmentChangeMode;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class Gui7Segment extends GuiScreen implements IHoverableHandler {
	private Gate7Segment part;
	private int xSize, ySize, guiLeft, guiTop;
	private IHoverable hoverable;
	private GuiDropdown dropdown;
	private GuiCheckBoxExt cbMaster, cbSlave;

	public Gui7Segment(Gate7Segment part) {
		this.part = part;
		this.xSize = 150;
		this.ySize = 100;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;

		buttonList
			.add(cbMaster = (new GuiCheckBoxExt(1, guiLeft + 15, guiTop + 50, I18n
				.format("gui.integratedcircuits.7segment.master"), false, null, this).setColor(0)
				.setDropShadow(false)));
		buttonList.add(cbSlave = (new GuiCheckBoxExt(2, guiLeft + 15, guiTop + 70, I18n
			.format("gui.integratedcircuits.7segment.slave"), true, null, this).setColor(0).setDropShadow(false)));

		buttonList.add(dropdown = (new GuiDropdown(0, guiLeft + 45, guiTop + 23, 90, 15, Arrays.asList(
				I18n.format("gui.integratedcircuits.7segment.mode.simple"),
				I18n.format("gui.integratedcircuits.7segment.mode.analog"),
				I18n.format("gui.integratedcircuits.7segment.mode.short.signed"),
				I18n.format("gui.integratedcircuits.7segment.mode.short.unsigned"),
				I18n.format("gui.integratedcircuits.7segment.mode.float"),
				I18n.format("gui.integratedcircuits.7segment.mode.binary"),
				I18n.format("gui.integratedcircuits.7segment.mode.manual")), this).setTooltips(Arrays.asList(
				I18n.format("gui.integratedcircuits.7segment.mode.simple.tooltip"),
				I18n.format("gui.integratedcircuits.7segment.mode.analog.tooltip"),
				I18n.format("gui.integratedcircuits.7segment.mode.short.signed.tooltip"),
				I18n.format("gui.integratedcircuits.7segment.mode.short.unsigned.tooltip"),
				I18n.format("gui.integratedcircuits.7segment.mode.float.tooltip"),
				I18n.format("gui.integratedcircuits.7segment.mode.binary.tooltip"),
				I18n.format("gui.integratedcircuits.7segment.mode.manual.tooltip")))));

		refreshUI();
	}

	public void refreshUI() {
		cbSlave.setIsChecked(part.isSlave);
		cbMaster.setIsChecked(!part.isSlave);

		dropdown.setEnabled(!cbSlave.isChecked());
		dropdown.setSelected(part.mode);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id > 0) {
			GuiCheckBoxExt box = (GuiCheckBoxExt) button;
			GuiCheckBoxExt other = button.id == 1 ? cbSlave : cbMaster;
			other.setIsChecked(!box.isChecked());
			boolean isSlave = cbSlave.isChecked();
			if (isSlave) {
				dropdown.setSelected(0);
				dropdown.setEnabled(false);
			} else
				dropdown.setEnabled(true);
		}
		CommonProxy.networkWrapper.sendToServer(new Packet7SegmentChangeMode(part.getProvider(), dropdown
			.getSelectedElement(), cbSlave.isChecked()));
	}

	@Override
	public void drawScreen(int x, int y, float par3) {
		GL11.glColor3f(1, 1, 1);

		hoverable = null;
		drawDefaultBackground();

		this.mc.getTextureManager().bindTexture(Resources.RESOURCE_GUI_7SEGMENT_BACKGROUND);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if (!(x >= guiLeft + 119 && x < guiLeft + 119 + 23 && y >= guiTop + 51 && y < guiTop + 51 + 33 && !dropdown
			.isOpen()))
			drawTexturedModalRect(guiLeft + 119, guiTop + 51, 150, 0, 23, 33);

		int display = part.digit;
		for (int i = 0; i < 8; i++) {
			int off = part.digit >> i & 1;
			drawTexturedModalRect(guiLeft + 86 + i * 4, guiTop + 94, 150 + off, 33, 1, 5);
		}

		String title = I18n.format("gui.integratedcircuits.7segment.name");
		fontRendererObj.drawString(title, guiLeft + xSize / 2 - fontRendererObj.getStringWidth(title) / 2, guiTop + 8,
				0x333333);

		this.mc.getTextureManager().bindTexture(this.mc.getTextureManager().getResourceLocation(0));
		GL11.glPushMatrix();
		GL11.glTranslatef(guiLeft + 119, guiTop + 51, 0);
		GL11.glRotatef(-90, 1, 0, 0);
		Part7SegmentRenderer.render7Segment(part.digit, 1, part.color);
		GL11.glPopMatrix();

		String label = I18n.format("gui.integratedcircuits.7segment.mode");
		fontRendererObj.drawString(label, guiLeft + 40 - fontRendererObj.getStringWidth(label), guiTop + 27, 0);

		super.drawScreen(x, y, par3);

		if (hoverable != null)
			drawHoveringText(hoverable.getHoverInformation(), x, y, this.fontRendererObj);

		GL11.glColor3f(1, 1, 1);
	}

	@Override
	protected void keyTyped(char ch, int keycode) {
		if (keycode == Keyboard.KEY_ESCAPE || keycode == Keyboard.KEY_E) {
			this.mc.displayGuiScreen(null);
			this.mc.setIngameFocus();
		}
	}

	@Override
	public void setCurrentItem(IHoverable hoverable) {
		this.hoverable = hoverable;
	}
}
