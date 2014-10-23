package vic.mod.integratedcircuits.client.gui;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import vic.mod.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import vic.mod.integratedcircuits.client.gui.GuiInterfaces.IHoverableHandler;
import cpw.mods.fml.client.config.GuiCheckBox;

public class GuiCheckBoxExt extends GuiCheckBox implements IHoverable
{
	private String hoverInfo;
	private IHoverableHandler parent;
	
	public GuiCheckBoxExt(int id, int xPos, int yPos, String displayString, boolean isChecked, String hoverInfo, IHoverableHandler parent) 
	{
		super(id, xPos, yPos, displayString, isChecked);
		this.hoverInfo = hoverInfo;
		this.parent = parent;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) 
	{
		super.drawButton(mc, mouseX, mouseY);
		if(field_146123_n) parent.setCurrentItem(this);
	}
	
	@Override
	public List<String> getHoverInformation() 
	{
		return Arrays.asList(hoverInfo);
	}
}
