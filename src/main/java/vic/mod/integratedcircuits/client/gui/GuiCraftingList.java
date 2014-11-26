package vic.mod.integratedcircuits.client.gui;

import net.minecraft.client.Minecraft;
import vic.mod.integratedcircuits.misc.CraftingAmount;
import vic.mod.integratedcircuits.misc.ItemAmount;

public class GuiCraftingList extends GuiListExt<GuiCraftingListEntry>
{
	public GuiAssembler parent;
	private CraftingAmount amount;
	private int xCoord, yCoord;
	private Minecraft mc;
	
	public GuiCraftingList(GuiAssembler parent, Minecraft mc, int xCoord, int yCoord, int width, int height) 
	{
		super(mc, xCoord, yCoord, width, height, 25);
		this.parent = parent;
	}
	
	public void setCraftingAmount(CraftingAmount amount)
	{
		this.amount = amount;
		entries.clear();
		for(ItemAmount ia : amount.getCraftingAmount())
			entries.add(new GuiCraftingListEntry(ia, parent));
		
		//TODO
		/*entries.add(new GuiCraftingListEntry(new ItemAmount(Items.apple, 20), parent));
		entries.add(new GuiCraftingListEntry(new ItemAmount(Items.bed, 20), parent));
		entries.add(new GuiCraftingListEntry(new ItemAmount(Items.book, 5), parent));
		entries.add(new GuiCraftingListEntry(new ItemAmount(Items.baked_potato, 20), parent));
		entries.add(new GuiCraftingListEntry(new ItemAmount(Items.diamond_helmet, 100), parent));
		entries.add(new GuiCraftingListEntry(new ItemAmount(Items.leather_helmet, 20), parent));
		entries.add(new GuiCraftingListEntry(new ItemAmount(Items.blaze_rod, 30), parent));*/
	}
}
