package moe.nightfall.vic.integratedcircuits.client.gui.component;

import java.util.ArrayList;
import java.util.List;

import moe.nightfall.vic.integratedcircuits.api.gate.ISocket.EnumConnectionType;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import moe.nightfall.vic.integratedcircuits.client.gui.cad.GuiCAD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraftforge.fml.client.config.GuiUtils;

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
		boolean b = !parent.blockMouseInput && super.mousePressed(mc, x ,y);
		if (b) {
			// Get the list of supported connection types for this width...
			List<EnumConnectionType> supported = EnumConnectionType.getSupportedList(parent.getCircuitData().maximumIOSize());
			// Work out the next one in the list...
			EnumConnectionType next = supported.get((supported.indexOf(mode) + 1) % supported.size());
			// Finally, set the input mode...
			parent.tileentity.setInputMode(side, next);
		}
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
		String text = ChatFormatting.BOLD + mode.singleID();
		int twidth = mc.fontRenderer.getStringWidth(text);
		mc.fontRenderer.drawString(text, this.xPosition + width / 2 - twidth / 2, this.yPosition + 2, hover ? 0xFFFFFF
				: 0xE0E0E0);
	}

	public void refresh() {
		mode = parent.getCircuitData().getProperties().getModeAtSide(side);
	}

	public String getHoverName() {
		return I18n.format("gui.integratedcircuits.cad.mode." + mode.name().toLowerCase());
	}
	
	@Override
	public List<String> getHoverInformation() {
		ArrayList<String> text = new ArrayList<String>();
		text.add(getHoverName());
		return text;
	}
}
