package moe.nightfall.vic.integratedcircuits.client.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import com.google.common.collect.Maps;

import cpw.mods.fml.client.config.GuiUtils;

public class GuiRollover extends GuiButton implements IHoverable {

	private static final int timeToOpen = 1000;

	private ResourceLocation resource;
	private Map<String, List<GuiButton>> buttonMap = Maps.newLinkedHashMap();
	private Map<String, Vec2> categoryMap = Maps.newLinkedHashMap();

	private boolean open = false;
	private boolean moving = false;
	private int currentHeight;
	private int selected = -1;
	private long startTime;

	public GuiRollover(int id, int x, int y, int width, int height, ResourceLocation resource) {
		super(id, x, y, "");
		this.width = width;
		this.height = height;
		this.currentHeight = height;
		this.resource = resource;
	}

	private int offsetY() {
		return categoryMap.size() * 21;
	}

	public GuiRollover addCategory(String category, int u, int v) {
		categoryMap.put(category, new Vec2(u, v));
		return this;
	}

	public GuiRollover add(GuiButton button, String category) {
		if (!categoryMap.containsKey(category))
			throw new RuntimeException();
		List<GuiButton> list = buttonMap.get(category);
		list = list == null ? new ArrayList<GuiButton>() : list;
		list.add(button);
		buttonMap.put(category, list);
		return this;
	}

	@Override
	public void drawButton(Minecraft mc, int mx, int my) {

		mc.renderEngine.bindTexture(buttonTextures);
		for (int i = 0; i < categoryMap.size(); i++) {
			GuiUtils.drawContinuousTexturedBox(xPosition, yPosition + i * 21, 0, 66, 18, 18, 200, 20, 2, 3, 2, 2, this.zLevel);
		}

		mc.renderEngine.bindTexture(resource);
		Iterator<Vec2> iterator = categoryMap.values().iterator();
		for (int i = 0; i < categoryMap.size(); i++) {
			Vec2 value = iterator.next();
			drawTexturedModalRect(xPosition, yPosition + i * 21, value.x, value.y, 16, 16);
		}
	}

	@Override
	protected void mouseDragged(Minecraft mc, int mx, int my) {

	}

	@Override
	public void mouseReleased(int mx, int my) {

	}

	@Override
	public boolean mousePressed(Minecraft mc, int mx, int my) {
		return false;
	}

	@Override
	public List<String> getHoverInformation() {
		return new ArrayList<String>();
	}
}
