package vic.mod.integratedcircuits.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import org.lwjgl.opengl.GL11;

public class GuiLabel extends Gui
{
	private String text;
	private int xCoord, yCoord;
	private int size = 6;
	private boolean shadow;
	private int color;
	
	public GuiLabel(int x, int y, String text)
	{
		this(x, y, text, 0xFFFFFF);
	}
	
	public GuiLabel(int x, int y, String text, int color) 
	{
		this.text = text;
		this.color = color;
		this.xCoord = x;
		this.yCoord = y;
	}
	
	public void drawLabel(Minecraft mc, int x, int y)
	{
		float scale = size / 6F;
		GL11.glScalef(scale, scale, 1);
		String[] list = text.split("\n");
		for(int i = 0; i < list.length; i++)
		{
			if(shadow) mc.fontRenderer.drawStringWithShadow(list[i], xCoord, yCoord + i * (size + 3), color);
			else mc.fontRenderer.drawString(list[i], xCoord, yCoord + i * (size + 3), color);
		}
		GL11.glScalef(1 / scale, 1 / scale, 1);
	}
	
	public GuiLabel setTextSize(int size)
	{
		this.size = size;
		return this;
	}
	
	public GuiLabel setTextShadow(boolean shadow)
	{
		this.shadow = shadow;
		return this;
	}
	
	public GuiLabel setTextColor(int color)
	{
		this.color = color;
		return this;
	}
	
	public String getText()
	{
		return text;
	}
	
	public GuiLabel setText(String text)
	{
		this.text = text;
		return this;
	}
}
