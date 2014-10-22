package vic.mod.integratedcircuits.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.apache.commons.lang3.text.WordUtils;

import cpw.mods.fml.client.config.GuiButtonExt;

public class GuiCallback<E extends GuiScreen & IGuiCallback> extends GuiScreen
{
	private E parent;
	private int width, height;
	
	public GuiCallback(E parent, int width, int height, Action... action)
	{
		this.mc = Minecraft.getMinecraft();
		this.parent = parent;
		for(int i = 0; i < action.length; i++)
		{
			Action a = action[i];
			addControl(new GuiButtonExt(a.ordinal(), width / 2 - i * 76 / 2, height - 25, 75, 25, WordUtils.capitalize(a.name())));
		}
	}
	
	public static enum Action
	{
		YES, NO, OK, CANCEL, CUSTOM;
	}
	
	public void display()
	{
		mc.displayGuiScreen(this);
	}
	
	public void destory()
	{
		mc.displayGuiScreen(parent);
	}
	
	public GuiCallback addControl(GuiButton control)
	{
		control.id += 4;
		buttonList.add(control);
		return this;
	}

	@Override
	protected void actionPerformed(GuiButton button) 
	{
		if(button.id < 4)
		{
			destory();
			parent.onCallback(this, Action.values()[button.id], button.id);
		}	
		else parent.onCallback(this, Action.CUSTOM, button.id - 4);
	}

	@Override
	public void drawScreen(int x, int y, float par3) 
	{		
		parent.drawScreen(-1, -1, par3);
		drawDefaultBackground();
		super.drawScreen(x, y, par3);
	}

	@Override
	public boolean doesGuiPauseGame() 
	{
		return false;
	}
}
