package moe.nightfall.vic.integratedcircuits.client.gui.component;

import java.util.Arrays;
import java.util.List;

import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverableHandler;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.config.GuiButtonExt;

public class GuiStateLabel extends GuiButtonExt implements IHoverable {
	private Vec2[] states;
	private String[] desc;
	private int state;
	private ResourceLocation loc;
	private IHoverableHandler parent;

	public GuiStateLabel(IHoverableHandler parent, int id, int xPos, int yPos, int width, int height,
			ResourceLocation loc) {
		super(id, xPos, yPos, "");
		this.width = width;
		this.height = height;
		this.loc = loc;
		this.parent = parent;
	}

	public GuiStateLabel addState(Vec2... states) {
		this.states = states;
		return this;
	}

	public GuiStateLabel addDescription(String... desc) {
		this.desc = desc;
		return this;
	}

	@Override
	public boolean mousePressed(Minecraft mc, int x, int y) {
		boolean bool = super.mousePressed(mc, x, y);
		if (bool)
			state = (state + 1) % states.length;
		return bool;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (!this.visible || states == null || state >= states.length)
			return;
		this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition
				&& mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
		if (field_146123_n) {
			parent.setCurrentItem(this);
			GL11.glColor3f(0.8F, 0.9F, 1F);
		} else
			GL11.glColor3f(1F, 1F, 1F);
		Vec2 uv = states[state];
		mc.getTextureManager().bindTexture(loc);
		drawTexturedModalRect(xPosition, yPosition, uv.x, uv.y, width, height);
	}

	@Override
	public List<String> getHoverInformation() {
		if (desc != null && state < desc.length)
			return Arrays.asList(desc[state].split("\n"));
		return null;
	}

	public int getState() {
		return state;
	}

	public GuiStateLabel setState(int state) {
		this.state = state;
		return this;
	}
}
