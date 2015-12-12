package moe.nightfall.vic.integratedcircuits.client.gui.component;

import java.util.Arrays;
import java.util.List;

import moe.nightfall.vic.integratedcircuits.client.Resources;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverableHandler;
import moe.nightfall.vic.integratedcircuits.misc.RenderUtils;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraftforge.fml.client.config.GuiUtils;

public class GuiDropdown extends GuiButton implements IHoverable {
	private ImmutableList<String> elements;
	private ImmutableList<String> tooltips;
	private int selected;
	private int hovered;
	private String hover;
	private boolean toggle;
	private IHoverableHandler parent;

	private static final String TRIANGLE_DOWN = "\u25BC";
	private static final String TRIANGLE_UP = "\u25B2";

	public GuiDropdown(int id, int x, int y, int width, int height, List<String> elements, IHoverableHandler parent) {
		super(id, x, y, width, height, "");
		this.elements = ImmutableList.copyOf(elements);
		this.parent = parent;
	}

	public GuiDropdown setTooltips(List<String> tooltips) {
		this.tooltips = ImmutableList.copyOf(tooltips);
		return this;
	}

	public GuiDropdown setSelected(int selected) {
		this.selected = selected;
		return this;
	}

	public GuiDropdown setEnabled(boolean enabled) {
		this.enabled = enabled;
		this.toggle = false;
		return this;
	}

	public GuiDropdown setHoverInformation(String hover) {
		this.hover = hover;
		return this;
	}

	public boolean isOpen() {
		return toggle;
	}

	public int getSelectedElement() {
		return selected;
	}

	public ImmutableList<String> getElements() {
		return elements;
	}

	@Override
	public void drawButton(Minecraft mc, int x, int y) {
		if (!visible)
			return;
		FontRenderer fr = mc.fontRenderer;

		if (x >= xPosition && x < xPosition + width && y >= yPosition && y < yPosition + height && tooltips != null
				&& !toggle && enabled)
			parent.setCurrentItem(this);

		drawRect(xPosition, yPosition, xPosition + width, yPosition + height, 0xFF000000);
		drawRect(xPosition + 1, yPosition + 1, xPosition + width - 1, yPosition + height - 1, 0xFF525252);

		int k = this.enabled ? toggle ? 2 : 1 : 0;
		GuiUtils.drawContinuousTexturedBox(buttonTextures, xPosition + width - height + 1, yPosition, 0, 46 + k * 20,
				height - 1, height, 200, 20, 2, 3, 2, 2, zLevel);

		mc.renderEngine.bindTexture(Resources.RESOURCE_GUI_CONTROLS);
		Gui.func_146110_a(xPosition + width - height / 2 - 4, yPosition + height / 2 - 3, 16 - (toggle ? 8 : 0), 0, 8,
				7, 32, 32);

		String current = elements.get(selected);
		int textWidth = width - height - 5;
		current = RenderUtils.cutStringToSize(fr, current, textWidth);
		fr.drawString(current, xPosition + 3, yPosition + height / 2 - fr.FONT_HEIGHT / 2 + 1, enabled ? 0xFFFFFF
				: 0x919191);

		if (isOpen()) {
			drawRect(xPosition + 1, yPosition + height + 1, xPosition + width + 1, yPosition + height + elements.size()
					* height + 1, 0xAA000000);
			drawRect(xPosition, yPosition + height, xPosition + width, yPosition + height + elements.size() * height,
					0xFF000000);
			drawRect(xPosition + 1, yPosition + height, xPosition + width - 1, yPosition + height - 1 + elements.size()
					* height, 0xFF919191);

			if (x >= xPosition && y >= yPosition + height && x < xPosition + width
					&& y < yPosition + height + elements.size() * height) {
				hovered = (y - yPosition - height) / height;
				if (hovered != selected)
					drawRect(xPosition + 1, yPosition + height * (hovered + 1),
							xPosition + width - 1, yPosition + height * (hovered + 2) - 1, 0xFF5C648E);
				parent.setCurrentItem(this);
			} else
				hovered = -1;

			for (int i = 0; i < elements.size(); i++) {
				if (i == selected)
					drawRect(xPosition + 1, yPosition + height * (i + 1), xPosition + width - 1, yPosition + height
							* (i + 2) - 1, 0xFF5C648E);
				String element = elements.get(i);
				element = RenderUtils.cutStringToSize(fr, element, width - 5);
				fr.drawStringWithShadow(element, xPosition + 3, yPosition + height + height * i + height / 2
						- fr.FONT_HEIGHT / 2, i == selected ? 0xFFFFA0 : 0xFFFFFF);
			}
		}
	}

	@Override
	public boolean mousePressed(Minecraft mc, int x, int y) {
		if (!visible)
			return false;
		if (x >= xPosition && y >= yPosition && x < xPosition + width
				&& y < yPosition + height + elements.size() * height) {
			if (toggle && y >= yPosition + height) {
				int y2 = (y - yPosition - height) / height;
				if (y2 != selected) {
					selected = y2;
					toggle = false;
					return true;
				}
				toggle = false;
			}
		} else
			toggle = false;

		boolean pressed = enabled && visible && x >= xPosition + width - height + 1 && y >= yPosition
				&& x < xPosition + width && y < yPosition + height;
		if (pressed && enabled) {
			toggle = !toggle;
			func_146113_a(Minecraft.getMinecraft().getSoundHandler());
		}

		return false;
	}

	@Override
	public List<String> getHoverInformation() {
		List<String> list;
		int active = isOpen() ? hovered : selected;
		String tooltip;
		if (active >= 0 && (tooltip = tooltips.get(active)) != null) {
			list = Arrays.asList(MiscUtils.stringNewlineSplit(tooltip));
		} else
			list = Lists.newArrayList();
		return list;
	}
}
