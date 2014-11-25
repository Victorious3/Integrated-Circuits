package vic.mod.integratedcircuits.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import vic.mod.integratedcircuits.misc.ItemAmount;
import vic.mod.integratedcircuits.misc.RenderUtils;

public class GuiCraftingListEntry implements IGuiListEntry
{
	private ItemAmount amount;
	private ItemStack stack;
	private GuiAssembler parent;
	
	public GuiCraftingListEntry(ItemAmount amount, GuiAssembler parent)
	{
		this.amount = amount;
		this.parent = parent;
		this.stack = new ItemStack(amount.item);
	}
	
	@Override
	public void drawEntry(int id, int x, int y, int par4, int par5, Tessellator tes, int par7, int par8, boolean par6) 
	{
		FontRenderer fr = parent.mc.fontRenderer;
		
		int needed = (int)Math.ceil(amount.amount);
		int current = parent.container.getAmountOf(amount.item);
		boolean supplied = current >= needed;
		
		RenderHelper.enableStandardItemLighting();
		RenderItem.getInstance().renderItemIntoGUI(fr, parent.mc.renderEngine, stack, x + 2, y, true);
		RenderHelper.disableStandardItemLighting();
		
		String s = current + "/" + needed;
		
		fr.drawString(s, x + 100 - fr.getStringWidth(s), y + 8, 0x202020);
		fr.drawString(stack.getDisplayName(), x + 21, y + 5, 0xFFFFFF);
		int c1 = 0x00FF00;
		int c2 = 0xFF0000;
		RenderUtils.drawStringWithBorder(fr, supplied ? "\u2714" : "x", x + 2, y + 8, supplied ? c1 : c2, 0);
	}

	@Override
	public boolean mousePressed(int id, int x, int y, int mouseEvent, int relX, int relY) 
	{
		return false;
	}

	@Override
	public void mouseReleased(int id, int x, int y, int mouseEvent, int relX, int relY) 
	{
		
	}	
}