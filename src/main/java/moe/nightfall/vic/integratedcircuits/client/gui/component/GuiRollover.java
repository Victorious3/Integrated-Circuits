package moe.nightfall.vic.integratedcircuits.client.gui.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cpw.mods.fml.client.config.GuiUtils;

public final class GuiRollover extends GuiButton implements IHoverable {

	private static final double timeToOpen = 250;
	private static final int boxHeight = 19;

	private ResourceLocation resource;
	private Map<String, List<GuiButton>> buttonMap = Maps.newLinkedHashMap();
	private Map<String, Vec2> categoryMap = Maps.newLinkedHashMap();
	private List<String> categoryList = Lists.newArrayList();

	private int currentHeight;
	private int nextHeight;
	private int selected = -1;
	private int next = -1;
	private long startTime;
	private int moving = 0;

	public GuiRollover(int id, int x, int y, int height, ResourceLocation resource) {
		super(id, x, y, "");
		this.width = 16;
		this.height = height;
		this.resource = resource;
	}

	private int offsetY() {
		return categoryMap.size() * boxHeight;
	}

	public GuiRollover addCategory(String category, int u, int v, GuiButton... buttons) {
		categoryMap.put(category, new Vec2(u, v));
		buttonMap.put(category, new ArrayList<GuiButton>());
		categoryList.add(category);
		for (GuiButton button : buttons) {
			add(button, category);
		}
		return this;
	}

	public GuiRollover add(GuiButton button, String category) {
		if (!categoryMap.containsKey(category))
			throw new RuntimeException();
		List<GuiButton> list = buttonMap.get(category);
		list.add(button);
		return this;
	}

	private int calcHeight(int index) {
		int height = 5;
		List<GuiButton> buttons = buttonMap.get(categoryList.get(index));
		for (GuiButton button : buttons) {
			height += button.height + 1;
		}
		return height;
	}

	private void moveDown(int y) {
		moving = 1;
		startTime = System.currentTimeMillis();
		selected = y;
		currentHeight = y * boxHeight;
		nextHeight = calcHeight(selected);
	}

	private void moveUp() {
		moving = -1;
		startTime = System.currentTimeMillis();
		currentHeight = nextHeight;
		nextHeight = selected * boxHeight;
	}

	@Override
	public void drawButton(Minecraft mc, int mx, int my) {

		if (!this.visible)
			return;

		// Who's bright idea was it to disable this?!
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glTranslatef(0, 0, 10);

		this.field_146123_n = mx >= this.xPosition && my >= this.yPosition && mx < this.xPosition + this.width && my < this.yPosition + this.height;
		double interpolate = MathHelper.clamp_double((System.currentTimeMillis() - startTime) / timeToOpen, 0, 1);

		int offset = boxHeight - (int) (interpolate * boxHeight) * moving;
		if (moving == -1) {
			offset -= boxHeight;
		}

		int j = 0;
		mc.renderEngine.bindTexture(buttonTextures);
		for (int i = 0; i < categoryMap.size(); i++) {
			if (i != selected) {
				int ypos = yPosition + j * boxHeight;
				if (i > selected && moving != 0)
					ypos += offset;
				int iconOffset = 66;
				if (field_146123_n && my > ypos && my < ypos + boxHeight)
					iconOffset = 86;
				GuiUtils.drawContinuousTexturedBox(xPosition, ypos, 0, iconOffset, 18, 18, 200, 20, 2, 3, 2, 2, this.zLevel);
				j++;
			}
		}

		j = 0;
		mc.renderEngine.bindTexture(resource);
		Iterator<Vec2> iterator = categoryMap.values().iterator();
		for (int i = 0; i < categoryMap.size(); i++) {
			Vec2 value = iterator.next();
			if (i != selected) {
				int ypos = yPosition + j * boxHeight;
				if (i > selected && moving != 0)
					ypos += offset;
				drawTexturedModalRect(xPosition, ypos, value.x, value.y, 16, 16);
				j++;
			}
		}

		int height = (int) (interpolate * (nextHeight - currentHeight)) + currentHeight;
		if (moving != 0 || selected != -1) {

			Vec2 value = categoryMap.get(categoryList.get(selected));
			GuiUtils.drawContinuousTexturedBox(buttonTextures, xPosition, yPosition + height, 0, 86, 18, 18, 200, 20, 2, 3, 2, 2, this.zLevel);
			mc.renderEngine.bindTexture(resource);
			drawTexturedModalRect(xPosition, yPosition + height, value.x, value.y, 16, 16);
		}

		if (selected != -1) {
			GL11.glTranslatef(0, 0, -5);

			double interpolate2 = interpolate;
			if (moving == -1) {
				interpolate2 = 1 - interpolate2;
			}

			// draw buttons
			List<GuiButton> buttons = buttonMap.get(categoryList.get(selected));
			int buttonOffset = (int) (5 * interpolate2);
			
			for (GuiButton button : buttons) {
				int i = (button.height + 1);
				buttonOffset += i * interpolate2;
				button.yPosition = xPosition + height - buttonOffset;
				button.xPosition = xPosition;

				button.drawButton(Minecraft.getMinecraft(), mx, my);
			}
			
			GL11.glTranslatef(0, 0, -5);
			if (buttons.size() > 0) {
				// draw bridge
				GuiUtils.drawContinuousTexturedBox(buttonTextures, xPosition + width / 2 - 5, yPosition + height - buttonOffset - (int) (5 * interpolate2), 0, 66, 12, buttonOffset + boxHeight, 200, 20, 2, 3, 2, 2, this.zLevel);
			}
			
			GL11.glTranslatef(0, 0, 10);
		}

		if (interpolate == 1) {
			if (moving == -1)
				selected = -1;
			if (next != -1) {
				moveDown(next);
				next = -1;
			} else {
				moving = 0;
			}
		}

		GL11.glTranslatef(0, 0, -10);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
	}

	@Override
	protected void mouseDragged(Minecraft mc, int mx, int my) {

	}

	@Override
	public void mouseReleased(int mx, int my) {

	}

	@Override
	public boolean mousePressed(Minecraft mc, int mx, int my) {
		if (!field_146123_n)
			return false;

		// Exclude clicking in-between the buttons
		float fy = (my - yPosition) / (float) boxHeight;
		if (fy - Math.floor(fy) > (16F / boxHeight))
			return false;

		int y = (int) fy;
		if (selected != -1 && y >= selected)
			y++;

		if (y != selected && y < categoryMap.size()) {
			if (selected != -1) {
				next = y;
				moveUp();
			} else {
				moveDown(y);
			}
		} else if (my > xPosition + height - 18 && selected != -1) {
			moveUp();
		}

		return false;
	}

	@Override
	public List<String> getHoverInformation() {
		return new ArrayList<String>();
	}
}
