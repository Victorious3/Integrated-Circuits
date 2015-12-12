package moe.nightfall.vic.integratedcircuits.client.gui.component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverableHandler;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public final class GuiRollover extends GuiButton implements IHoverable {

	private static final double timeToOpen = 250;
	private static final int boxHeight = 19;

	private ResourceLocation resource;
	private Map<Integer, Category> categoryMap = Maps.newLinkedHashMap();
	private List<Integer> categoryList = Lists.newArrayList();

	public static class Category {
		public List<GuiButton> buttonList;
		public Vec2 icon;
		public String tooltip;
		public boolean enabled = true;
		public int id;
	}

	private int currentHeight;
	private int nextHeight;
	private int selected = -1;
	private int next = -1;
	private long startTime;
	private int moving = 0;
	private Category hoveredCategory = null;

	// Public selected to avoid asynchronous sliding up
	private int pSelected = -1;

	public GuiRollover(int id, int x, int y, int height, ResourceLocation resource) {
		super(id, x, y, "");
		this.width = 16;
		this.height = height;
		this.resource = resource;
	}

	public GuiRollover addCategory(int id, int u, int v, GuiButton... buttons) {
		return addCategory(id, null, u, v, buttons);
	}

	public GuiRollover addCategory(int id, String tooltip, int u, int v, GuiButton... buttons) {
		Category category = new Category();
		category.id = id;
		category.icon = new Vec2(u, v);
		category.buttonList = new ArrayList<GuiButton>(Arrays.asList(buttons));
		category.tooltip = tooltip;

		categoryMap.put(id, category);
		categoryList.add(id);
		return this;
	}

	public GuiRollover add(GuiButton button, int category) {
		if (!categoryMap.containsKey(category))
			throw new RuntimeException();
		List<GuiButton> list = getButtons(category);
		list.add(button);
		return this;
	}

	private int calcHeight(int index) {
		List<GuiButton> buttons = getButtons(categoryList.get(index));
		int height = buttons.size() > 0 ? 10 : 0;
		for (GuiButton button : buttons) {
			height += button.height + 1;
		}
		return height;
	}

	private void moveDown(int y) {
		moving = 1;
		startTime = System.currentTimeMillis();
		selected = y;
		currentHeight = 0;
		nextHeight = calcHeight(selected);
	}

	private void moveUp() {
		moving = -1;
		startTime = System.currentTimeMillis();
		currentHeight = nextHeight;
		nextHeight = 0;
	}

	public int getSelected() {
		return pSelected;
	}

	public List<GuiButton> getButtons(int category) {
		return categoryMap.get(category).buttonList;
	}

	public List<Integer> getCategories() {
		return categoryList;
	}

	public Category getCategory(int category) {
		return categoryMap.get(category);
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
		int height = (int) (interpolate * (nextHeight - currentHeight)) + currentHeight;

		mc.renderEngine.bindTexture(buttonTextures);
		int i = 0;
		for (Category category : categoryMap.values()) {
			int ypos = yPosition + i * boxHeight;
			if (i >= selected)
				ypos += height;
			int iconOffset = 66;
			boolean hovered = (field_146123_n && my > ypos && my < ypos + boxHeight);
			if (hovered && mc.currentScreen instanceof IHoverableHandler && category.tooltip != null) {
				((IHoverableHandler) mc.currentScreen).setCurrentItem(this);
				hoveredCategory = category;
			}

			if (!category.enabled)
				iconOffset = 46;
			else if (i == selected || hovered)
				iconOffset = 86;

			GuiUtils.drawContinuousTexturedBox(xPosition, ypos, 0, iconOffset, 18, 18, 200, 20, 2, 3, 2, 2, this.zLevel);
			i++;
		}

		mc.renderEngine.bindTexture(resource);
		Iterator<Category> iterator = categoryMap.values().iterator();
		for (i = 0; i < categoryMap.size(); i++) {
			Category category = iterator.next();
			int ypos = yPosition + i * boxHeight;
			if (i >= selected)
				ypos += height;
			drawTexturedModalRect(xPosition, ypos, category.icon.x, category.icon.y, 16, 16);
		}

		if (selected != -1) {
			GL11.glTranslatef(0, 0, -5);

			// draw buttons
			List<GuiButton> buttons = getButtons(categoryList.get(selected));

			double interpolate2 = interpolate;
			int height2 = (moving == -1 ? currentHeight : nextHeight) - (buttons.size() - selected) * boxHeight;
			if (moving == -1) {
				interpolate2 = 1 - interpolate;
			}

			int buttonOffset = (int) (height2 - Math.floor(boxHeight * interpolate2));

			for (GuiButton button : buttons) {
				buttonOffset += (button.height + 1) * interpolate2;
				button.yPosition = yPosition + buttonOffset - 5;
				button.xPosition = xPosition + 5;
				button.drawButton(Minecraft.getMinecraft(), mx, my);
			}
			
			GL11.glTranslatef(0, 0, -5);
			if (buttons.size() > 0) {
				// draw bridge
				GuiUtils.drawContinuousTexturedBox(buttonTextures, xPosition + width / 2 - 5, yPosition + height2 - 8, 0, 66, 12, height, 200, 20, 2, 3, 2, 2, this.zLevel);
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
	public void mouseReleased(int mx, int my) {
		if (selected != -1) {
			for (GuiButton button : getButtons(categoryList.get(selected))) {
				button.mouseReleased(mx, my);
			}
		}
	}

	public void close() {
		pSelected = -1;
		moveUp();
	}

	private static final Method m_actionPerformed = ReflectionHelper.findMethod(GuiScreen.class, null,
			new String[] { "actionPerformed", "func_146284_a" }, GuiButton.class);

	@Override
	public boolean mousePressed(Minecraft mc, int mx, int my) {

		if (selected != -1) {
			for (GuiButton button : getButtons(categoryList.get(selected))) {
				if (button.mousePressed(mc, mx, my)) {
					try {
						m_actionPerformed.invoke(mc.currentScreen, button);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					return true;
				}
			}
		}

		if (!field_146123_n)
			return false;

		// Exclude clicking in-between the buttons
		float fy = (my - yPosition) / (float) boxHeight;
		if (selected != -1 && fy > selected) {
			fy = (my - yPosition - nextHeight - selected * boxHeight) / (float) boxHeight;
			fy += selected;
			if (fy < selected)
				return false;
		}

		if (fy - Math.floor(fy) > (16F / boxHeight))
			return false;

		int y = (int) fy;

		Category category = null;
		if (y != selected && y < categoryMap.size()) {
			category = getCategory(categoryList.get(y));

			// Its only a button if there are no subelements
			if (category.buttonList.size() > 0) {
				pSelected = y;
				if (selected != -1) {
					next = y;
					moveUp();
				} else {
					moveDown(y);
				}
			}
		} else if (y == selected) {
			moveUp();
			pSelected = -1;
			category = getCategory(categoryList.get(y));
		}

		if (category != null) {
			// Dirty hack to make it work with actionPerformed.
			try {
				if (category.enabled) {
					int id = this.id;
					this.id = category.id;
					m_actionPerformed.invoke(mc.currentScreen, this);
					this.id = id;
					return true;
				}
			} catch (Exception e) {
			}
		}

		return false;
	}

	@Override
	public List<String> getHoverInformation() {
		if (hoveredCategory != null)
			return Arrays.asList(MiscUtils.stringNewlineSplit(hoveredCategory.tooltip));
		else return null;
	}
}
