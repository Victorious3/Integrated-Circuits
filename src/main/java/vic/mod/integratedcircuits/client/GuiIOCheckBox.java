package vic.mod.integratedcircuits.client;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
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
			if(isChecked()) parent.te.setInputMode(parent.te.con | 1 << side);
			else parent.te.setInputMode(parent.te.con & ~(1 << side));
			refresh();
		}
		return b;
	}
	
	@Override
	public void drawButton(Minecraft mc, int x, int y) 
	{
		if(x >= this.xPosition && y >= this.yPosition && x < this.xPosition + this.width && y < this.yPosition + this.height)
			parent.hoveredElement = this;
		super.drawButton(mc, x, y);
	}

	public void refresh()
	{
		setIsChecked((parent.te.con >> side & 1) != 0);
	}

	@Override
	public ArrayList<String> getHoverInformation() 
	{
		ArrayList<String> text = new ArrayList<String>();
		text.add(isChecked() ? "bundled" : "simple");
		return text;
	}
}
