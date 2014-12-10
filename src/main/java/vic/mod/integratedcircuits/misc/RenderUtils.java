package vic.mod.integratedcircuits.misc;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

public class RenderUtils 
{
	public static void drawTexture(double x, double y, int u, int v, int w, int h, float rotation, Gui gui)
	{
		GL11.glTranslated(x, y, 0);
		GL11.glTranslatef(w /2F, h /2F, 0);
		GL11.glRotatef(-rotation, 0, 0, 1);
		GL11.glTranslatef(-w / 2F, -h / 2F, 0);
		gui.drawTexturedModalRect(0, 0, u, v, w, h);
		GL11.glTranslated(-x, -y, 0);
	}
	
	public static void addBox(Tessellator tes, double x1, double y1, double z1, double w, double h, double d)
	{
		double x2 = x1 + w;
		double y2 = y1 + h;
		double z2 = z1 + d;
		
		tes.addVertex(x1, y2, z1);
		tes.addVertex(x1, y2, z2);
		tes.addVertex(x2, y2, z2);
		tes.addVertex(x2, y2, z1);
		
		tes.addVertex(x2, y1, z1);
		tes.addVertex(x2, y1, z2);
		tes.addVertex(x1, y1, z2);
		tes.addVertex(x1, y1, z1);
		
		tes.addVertex(x1, y1, z1);
		tes.addVertex(x1, y2, z1);
		tes.addVertex(x2, y2, z1);
		tes.addVertex(x2, y1, z1);
		
		tes.addVertex(x2, y1, z2);
		tes.addVertex(x2, y2, z2);
		tes.addVertex(x1, y2, z2);
		tes.addVertex(x1, y1, z2);
		
		tes.addVertex(x2, y1, z1);
		tes.addVertex(x2, y2, z1);
		tes.addVertex(x2, y2, z2);
		tes.addVertex(x2, y1, z2);
		
		tes.addVertex(x1, y1, z2);
		tes.addVertex(x1, y2, z2);
		tes.addVertex(x1, y2, z1);
		tes.addVertex(x1, y1, z1);
	}
	
	public static void drawStringWithBorder(FontRenderer fr, String str, int x, int y, int color, int border)
	{
		fr.drawString(str, x + 1, y, border);
		fr.drawString(str, x - 1, y, border);
		fr.drawString(str, x, y + 1, border);
		fr.drawString(str, x, y - 1, border);
		fr.drawString(str, x, y, color);
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
	
	private static float lightX, lightY;
	
	public static void setBrightness(float lightX, float lightY)
	{
		RenderUtils.lightX = OpenGlHelper.lastBrightnessX;
		RenderUtils.lightY = OpenGlHelper.lastBrightnessY;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightX, lightY);
	}
	
	public static void resetBrightness()
	{
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightX, lightY);
	}
}
