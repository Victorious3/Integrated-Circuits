package vic.mod.integratedcircuits;

import net.minecraft.client.gui.Gui;

import org.lwjgl.opengl.GL11;

public class RenderUtils 
{
	public static void drawTexture(int u, int v, float rotation, Gui gui, double x, double y, int w, int h)
	{
		GL11.glTranslated(x, y, 0);
		GL11.glTranslatef(w /2F, h /2F, 0);
		GL11.glRotatef(-rotation, 0, 0, 1);
		GL11.glTranslatef(-w / 2F, -h / 2F, 0);
		gui.drawTexturedModalRect(0, 0, u, v, w, h);
		GL11.glTranslated(-x, -y, 0);
	}
	
	public static void applyColorIRGBA(int rbga)
	{
		float red = (float)(rbga >> 16 & 255) / 255.0F;
		float blue = (float)(rbga >> 8 & 255) / 255.0F;
		float green = (float)(rbga & 255) / 255.0F;
		float alpha = (float)(rbga >> 24 & 255) / 255.0F;
        GL11.glColor4f(red, blue, green, alpha);
	}
	
	public static void applyColorIRGB(int rbg)
	{
		float red = (float)(rbg >> 16 & 255) / 255.0F;
		float blue = (float)(rbg >> 8 & 255) / 255.0F;
		float green = (float)(rbg & 255) / 255.0F;
        GL11.glColor4f(red, blue, green, 1F);
	}
}
