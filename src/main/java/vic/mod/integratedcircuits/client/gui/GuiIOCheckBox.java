package vic.mod.integratedcircuits.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import vic.mod.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import cpw.mods.fml.client.config.GuiCheckBox;

public class GuiIOCheckBox extends GuiCheckBox implements IHoverable
{
	private GuiPCBLayout parent;
	private int side;
	
	public GuiIOCheckBox(int id, int xPos, int yPos, GuiPCBLayout parent, int side) 
	{
		super(id, xPos, yPos, "", false);
		this.parent = parent;
		this.side = side;
	}

	@Override
	public boolean mousePressed(Minecraft mc, int x, int y) 
	{
		boolean b = parent.blockMouseInput ? false : super.mousePressed(mc, x, y);
		if(b)
		{
			parent.te.setInputMode(isChecked(), side);
			refresh();
		}
		return b;
	}
	
	@Override
	public void drawButton(Minecraft mc, int x, int y) 
	{
		if(x >= this.xPosition && y >= this.yPosition && x < this.xPosition + this.width && y < this.yPosition + this.height)
			parent.setCurrentItem(this);
		super.drawButton(mc, x, y);
	}

	public void refresh()
	{
		setIsChecked((parent.te.con >> side & 1) != 0);
	}

	@Override
	public List<String> getHoverInformation() 
	{
		ArrayList<String> text = new ArrayList<String>();
		text.add(isChecked() ? "bundled" : "simple");
		return text;
	}
}
