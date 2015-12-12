package moe.nightfall.vic.integratedcircuits.client.gui.component;

import java.util.Arrays;
import java.util.List;

import moe.nightfall.vic.integratedcircuits.client.gui.GuiAssembler;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import moe.nightfall.vic.integratedcircuits.misc.ItemAmount;
import moe.nightfall.vic.integratedcircuits.misc.RenderUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Mouse;

import net.minecraftforge.fml.client.config.GuiUtils;

public class GuiCraftingListEntry implements IGuiListEntry, IHoverable {
	private static final ResourceLocation buttonTextures = new ResourceLocation("textures/gui/widgets.png");
	private ItemAmount amount;
	private ItemStack stack;
	private GuiAssembler parent;

	public GuiCraftingListEntry(ItemAmount amount, GuiAssembler parent) {
		this.amount = amount;
		this.parent = parent;
		this.stack = new ItemStack(amount.item);
	}

	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator tes, int mouseX,
			int mouseY, boolean isSelected) {
		FontRenderer fr = parent.mc.fontRenderer;
		if (mouseX >= x && mouseY >= y && mouseX <= x + listWidth && mouseY <= y + slotHeight
				&& !parent.te.laserHelper.isRunning && !Mouse.isButtonDown(0))
			parent.setCurrentItem(this);

		GuiUtils.drawContinuousTexturedBox(buttonTextures, x + 1, y, 0, 46, listWidth - 2, slotHeight, 200, 20, 2, 3,
				2, 2, 0);

		int needed = (int) Math.ceil(amount.amount) * parent.te.request;
		int current = parent.container.getAmountOf(amount.item);
		boolean supplied = current >= needed;

		RenderHelper.enableStandardItemLighting();
		RenderItem.getInstance().renderItemIntoGUI(fr, parent.mc.renderEngine, stack, x + 2, y + slotHeight / 2 - 8,
				true);
		RenderHelper.disableStandardItemLighting();

		String s = current + "/" + needed;
		String s2 = stack.getDisplayName();
		s2 = RenderUtils.cutStringToSize(fr, s2, listWidth - 45);

		fr.setUnicodeFlag(true);
		fr.drawString(s, x + listWidth - 2 - fr.getStringWidth(s), y + slotHeight - fr.FONT_HEIGHT, 0xFFFFFF);
		fr.setUnicodeFlag(false);

		fr.drawStringWithShadow(s2, x + 21, y + slotHeight / 2 - fr.FONT_HEIGHT / 2, 0xFFFFFF);
		int c1 = 0x00FF00;
		int c2 = 0xFF0000;
		RenderUtils.drawStringWithBorder(fr, supplied ? "\u2714" : "x", x + 4, y + slotHeight / 2, supplied ? c1 : c2,
				0);
	}

	@Override
	public boolean mousePressed(int id, int x, int y, int mouseEvent, int relX, int relY) {
		return false;
	}

	@Override
	public void mouseReleased(int id, int x, int y, int mouseEvent, int relX, int relY) {

	}

	@Override
	public List<String> getHoverInformation() {
		return Arrays.asList(stack.getDisplayName());
	}
}