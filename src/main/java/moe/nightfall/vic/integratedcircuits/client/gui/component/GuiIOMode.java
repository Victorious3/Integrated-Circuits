package moe.nightfall.vic.integratedcircuits.client.gui.component;

import java.util.ArrayList;
import java.util.List;

import moe.nightfall.vic.integratedcircuits.api.gate.ISocket.EnumConnectionType;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import moe.nightfall.vic.integratedcircuits.client.gui.cad.GuiCAD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

import com.mojang.realmsclient.gui.ChatFormatting;

import cpw.mods.fml.client.config.GuiUtils;

public class GuiIOMode extends GuiButton implements IHoverable {
	private GuiCAD parent;
	private int side;
	private EnumConnectionType mode;

	public GuiIOMode(int id, int xPos, int yPos, GuiCAD parent, int side) {
		super(id, xPos, yPos, 11, 11, "");
		this.parent = parent;
		this.side = side;
	}

	@Override
	public boolean mousePressed(Minecraft mc, int x, int y) {
		boolean b = parent.blockMouseInput ? false : super.mousePressed(mc, x, y);
		if (b)
			parent.tileentity.setInputMode(side, EnumConnectionType.values()[(mode.ordinal() + 1) % 3]);
		return b;
	}

	@Override
	public void drawButton(Minecraft mc, int x, int y) {
		boolean hover = false;
		if (x >= this.xPosition && y >= this.yPosition && x < this.xPosition + this.width
				&& y < this.yPosition + this.height) {
			parent.setCurrentItem(this);
			hover = true;
		}
		GuiUtils.drawContinuousTexturedBox(buttonTextures, this.xPosition, this.yPosition, 0, 46, this.width,
				this.height, 200, 20, 2, 3, 2, 2, this.zLevel);
		String text = ChatFormatting.BOLD
				+ (mode == EnumConnectionType.BUNDLED ? "B" : mode == EnumConnectionType.ANALOG ? "A" : "S");
		int twidth = mc.fontRenderer.getStringWidth(text);
		mc.fontRenderer.drawString(text, this.xPosition + width / 2 - twidth / 2, this.yPosition + 2, hover ? 0xFFFFFF
				: 0xE0E0E0);
	}

	public void refresh() {
		mode = parent.tileentity.getCircuitData().getProperties().getModeAtSide(side);
	}

	@Override
	public List<String> getHoverInformation() {
		ArrayList<String> text = new ArrayList<String>();
		String s = "gui.integratedcircuits.cad.mode.";
		s += mode == EnumConnectionType.BUNDLED ? "bundled" : mode == EnumConnectionType.ANALOG ? "analog" : "simple";
		text.add(I18n.format(s));
		return text;
	}
}
