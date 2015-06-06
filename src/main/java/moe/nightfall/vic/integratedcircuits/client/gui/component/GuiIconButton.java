package moe.nightfall.vic.integratedcircuits.client.gui.component;

import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.client.config.GuiButtonExt;

public class GuiIconButton extends GuiButtonExt {

	private ResourceLocation resource;
	private Vec2 size;
	private Vec2 pos;

	public GuiIconButton(int id, int xPos, int yPos, int width, int height, ResourceLocation resource) {
		super(id, xPos, yPos, width, height, "");
		this.resource = resource;
		size = new Vec2(width, height);
		pos = new Vec2(0, 0);
	}

	public GuiIconButton setIcon(int xPos, int yPos) {
		pos = new Vec2(xPos, yPos);
		return this;
	}

	public GuiIconButton setIcon(int xPos, int yPos, int width, int height) {
		size = new Vec2(width, height);
		return setIcon(xPos, yPos);
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		super.drawButton(mc, mouseX, mouseY);
		mc.renderEngine.bindTexture(resource);
		drawTexturedModalRect(xPosition, yPosition, pos.x, pos.y, size.x, size.y);
	}
}
