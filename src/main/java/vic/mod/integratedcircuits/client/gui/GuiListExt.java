package vic.mod.integratedcircuits.client.gui;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

public class GuiListExt<T extends IGuiListEntry> extends GuiListExtended
{
	public ArrayList<T> entries = new ArrayList<T>();
	public int xCoord, yCoord;
	public Minecraft mc;
	
	public GuiListExt(Minecraft mc, int xCoord, int yCoord, int width, int height, int elementHeight) 
	{
		super(mc, width, 0, 0, height, elementHeight);
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.mc = Minecraft.getMinecraft();
	}
	
	public boolean mouseClicked(int x, int y, int button) 
	{
		return super.func_148179_a(x + xCoord, y + yCoord, button);
	}

	public boolean mouseMovedOrUp(int x, int y, int button) 
	{
		return super.func_148181_b(x + xCoord, y + yCoord, button);
	}
	
	@Override
	protected void drawSlot(int par1, int x, int y, int par4, Tessellator tes, int par6, int par7) 
	{
		super.drawSlot(par1, 0, y, par4, tes, par6, par7);
	}

	@Override
	public int getListWidth() 
	{
		return width - (func_148135_f() > 0 ? 6 : 0);
	}

	@Override
	public void drawScreen(int x, int y, float par3)
	{
		ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
		int guiScale = scaledresolution.getScaleFactor();
		int width = this.width;
		int height = this.bottom;
		
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor((int)(xCoord * guiScale), this.mc.displayHeight - (int)(yCoord * guiScale) - height * guiScale, (int)(width * guiScale), (int)(height * guiScale));
		GL11.glTranslatef(xCoord, yCoord, 0);
		if(getSize() > 0) super.drawScreen(x - xCoord, y - yCoord, par3);
		GL11.glTranslatef(-xCoord, -yCoord, 0);
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

	@Override
	protected void drawContainerBackground(Tessellator tessellator) 
	{
//		super.drawContainerBackground(tessellator);
	}

	@Override
	public IGuiListEntry getListEntry(int id) 
	{
		return entries.get(id);
	}

	@Override
	protected int getSize() 
	{
		return entries.size();
	}

	@Override
	protected int getScrollBarX() 
	{
		return this.width - 6;
	}
}
