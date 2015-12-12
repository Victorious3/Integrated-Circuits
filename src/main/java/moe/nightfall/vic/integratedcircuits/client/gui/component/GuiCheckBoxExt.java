package moe.nightfall.vic.integratedcircuits.client.gui.component;

import java.util.Arrays;
import java.util.List;

import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverableHandler;
import net.minecraft.client.Minecraft;

import com.google.common.collect.Lists;

import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.client.config.GuiUtils;

public class GuiCheckBoxExt extends GuiCheckBox implements IHoverable {
	private String hoverInfo;
	private IHoverableHandler parent;
	private int boxWidth = 11;
	private boolean dropShadow = true;

	public GuiCheckBoxExt(int id, int xPos, int yPos, String displayString, boolean isChecked, String hoverInfo,
			IHoverableHandler parent) {
		super(id, xPos, yPos, displayString, isChecked);
		this.hoverInfo = hoverInfo;
		this.parent = parent;
		this.packedFGColour = -1;
		this.width = 11;
	}

	public GuiCheckBoxExt setColor(int color) {
		this.packedFGColour = color;
		return this;
	}

	public GuiCheckBoxExt setDropShadow(boolean dropShadow) {
		this.dropShadow = dropShadow;
		return this;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (visible) {
			field_146123_n = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + boxWidth
					&& mouseY < yPosition + height;
			GuiUtils.drawContinuousTexturedBox(buttonTextures, xPosition, yPosition, 0, 46, boxWidth, height, 200, 20,
					2, 3, 2, 2, zLevel);
			mouseDragged(mc, mouseX, mouseY);
			int color = 0xE0E0E0;

			if (packedFGColour >= 0)
				color = packedFGColour;
			else if (!this.enabled)
				color = 0xA0A0A0;

			if (isChecked())
				this.drawCenteredString(mc.fontRenderer, "x", xPosition + boxWidth / 2 + 1, yPosition + 1, 0xE0E0E0);

			mc.fontRenderer.drawString(displayString, xPosition + boxWidth + 2, yPosition + 2, color, dropShadow);
		}
		if (field_146123_n)
			parent.setCurrentItem(this);
	}

	@Override
	public List<String> getHoverInformation() {
		if (hoverInfo != null)
			return Arrays.asList(hoverInfo.split("%n"));
		else
			return Lists.newArrayList();
	}
}
