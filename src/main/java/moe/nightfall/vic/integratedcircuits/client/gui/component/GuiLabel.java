package moe.nightfall.vic.integratedcircuits.client.gui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import org.lwjgl.opengl.GL11;

public class GuiLabel extends Gui {
	private String text;
	private int xCoord, yCoord;
	private int size = 6;
	private boolean shadow;
	private int color;
	private boolean centered;

	public GuiLabel(int x, int y, String text) {
		this(x, y, text, 0xFFFFFF);
	}

	public GuiLabel(int x, int y, String text, int color) {
		this(x, y, text, color, false);
	}

	public GuiLabel(int x, int y, String text, int color, boolean centered) {
		this.text = text;
		this.color = color;
		this.xCoord = x;
		this.yCoord = y;
		this.centered = centered;
	}

	public void drawLabel(Minecraft mc, int x, int y) {
		float scale = size / 6F;
		GL11.glScalef(scale, scale, 1);
		String[] list = text.split("\r\n");
		for (int i = 0; i < list.length; i++) {
			int width = mc.fontRenderer.getStringWidth(list[i]);
			int xOff = centered ? width / 2 : 0;
			if (shadow)
				mc.fontRenderer.drawStringWithShadow(list[i], xCoord - xOff, yCoord + i * (size + 3), color);
			else
				mc.fontRenderer.drawString(list[i], xCoord - xOff, yCoord + i * (size + 3), color);
		}
		GL11.glScalef(1 / scale, 1 / scale, 1);
	}

	public GuiLabel setTextSize(int size) {
		this.size = size;
		return this;
	}

	public GuiLabel setTextShadow(boolean shadow) {
		this.shadow = shadow;
		return this;
	}

	public GuiLabel setTextColor(int color) {
		this.color = color;
		return this;
	}

	public GuiLabel setCentered(boolean centered) {
		this.centered = centered;
		return this;
	}

	public String getText() {
		return text;
	}

	public GuiLabel setText(String text) {
		this.text = text;
		return this;
	}
}
