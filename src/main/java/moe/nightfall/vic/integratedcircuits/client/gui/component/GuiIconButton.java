package moe.nightfall.vic.integratedcircuits.client.gui.component;

import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.client.config.GuiButtonExt;

public class GuiIconButton extends GuiButtonExt {

	private ResourceLocation resource;
	private Vec2 size;
	private Vec2 pos;

	private boolean isToggleable;
	private boolean isUntoggleDisabled;
	private boolean isToggled;

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

	public GuiIconButton setToggleable(boolean isToggleable) {
		return setToggleable(isToggleable, false);
	}

	public GuiIconButton setToggleable(boolean isToggleable, boolean isUntoggleDisabled) {
		this.isToggleable = isToggleable;
		this.isUntoggleDisabled = isUntoggleDisabled;
		return this;
	}

	public GuiIconButton setToggled(boolean isToggled) {
		this.isToggled = isToggled;
		return this;
	}

	public boolean isToggled() {
		return isToggled;
	}

	@Override
	public int getHoverState(boolean hover) {
		if (isToggleable && isToggled())
			return 2;
		return super.getHoverState(hover);
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		super.drawButton(mc, mouseX, mouseY);
		mc.renderEngine.bindTexture(resource);
		drawTexturedModalRect(xPosition, yPosition, pos.x, pos.y, size.x, size.y);
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mx, int my) {
		boolean pressed = super.mousePressed(mc, mx, my);
		if (pressed && enabled && isToggleable) {
			if (isToggled && isUntoggleDisabled)
				return false;
			return isToggled = !isToggled;
		}
		return pressed;
	}
}
