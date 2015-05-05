package moe.nightfall.vic.integratedcircuits.misc;

import moe.nightfall.vic.integratedcircuits.client.Resources;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import codechicken.lib.render.CCRenderState;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderUtils {
	public static void drawTexture(double x, double y, int u, int v, int w, int h, float rotation, Gui gui) {
		GL11.glTranslated(x, y, 0);
		GL11.glTranslatef(w / 2F, h / 2F, 0);
		GL11.glRotatef(-rotation, 0, 0, 1);
		GL11.glTranslatef(-w / 2F, -h / 2F, 0);
		gui.drawTexturedModalRect(0, 0, u, v, w, h);
		GL11.glTranslated(-x, -y, 0);
	}

	public static void addLine(double x, double y, double x2, double y2, double linewidth) {
		Tessellator tes = Tessellator.instance;
		if (x > x2) {
			double t = x2;
			x2 = x;
			x = t;

			t = y2;
			y2 = y;
			y = t;
		}

		double g = y2 - y;
		double a = x2 - x;
		double angle = Math.atan(g / a);
		double size = linewidth / 2D;
		double ox = Math.sin(angle) * size;
		double oy = Math.cos(angle) * size;

		tes.addVertex(x + ox, y - oy, 0);
		tes.addVertex(x - ox, y + oy, 0);
		tes.addVertex(x2 - ox, y2 + oy, 0);
		tes.addVertex(x2 + ox, y2 - oy, 0);
	}

	public static void addBox(Tessellator tes, double x1, double y1, double z1, double w, double h, double d) {
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

	public static void drawStringWithBorder(FontRenderer fr, String str, int x, int y, int color, int border) {
		fr.drawString(str, x + 1, y, border);
		fr.drawString(str, x - 1, y, border);
		fr.drawString(str, x, y + 1, border);
		fr.drawString(str, x, y - 1, border);
		fr.drawString(str, x, y, color);
	}

	public static void drawGUIWindow(int xOffset, int yOffset, int width, int height) {
		GL11.glTranslatef(xOffset, yOffset, 0);
		Gui.drawRect(3, 3, width - 3, height - 3, 0xFFC6C6C6);
		Gui.drawRect(4, 0, width - 4, 1, 0xFF000000);
		Gui.drawRect(4, 1, width - 4, 3, 0xFFFFFFFF);
		Gui.drawRect(4, height - 1, width - 4, height, 0xFF000000);
		Gui.drawRect(4, height - 3, width - 4, height - 1, 0xFF555555);
		Gui.drawRect(0, 4, 1, height - 4, 0xFF000000);
		Gui.drawRect(1, 4, 3, height - 4, 0xFFFFFFFF);
		Gui.drawRect(width - 1, 4, width, height - 4, 0xFF000000);
		Gui.drawRect(width - 3, 4, width - 1, height - 4, 0xFF555555);

		Minecraft.getMinecraft().renderEngine.bindTexture(Resources.RESOURCE_GUI_CONTROLS);
		GL11.glColor4f(1, 1, 1, 1);
		Gui.func_146110_a(0, 0, 0, 0, 4, 4, 32, 32);
		Gui.func_146110_a(width - 4, 0, 4, 0, 4, 4, 32, 32);
		Gui.func_146110_a(width - 4, height - 4, 4, 4, 4, 4, 32, 32);
		Gui.func_146110_a(0, height - 4, 0, 4, 4, 4, 32, 32);
		GL11.glTranslatef(-xOffset, -yOffset, 0);
	}

	public static void applyColorIRGBA(int rbga) {
		float red = (float) (rbga >> 16 & 255) / 255.0F;
		float blue = (float) (rbga >> 8 & 255) / 255.0F;
		float green = (float) (rbga & 255) / 255.0F;
		float alpha = (float) (rbga >> 24 & 255) / 255.0F;
		GL11.glColor4f(red, blue, green, alpha);
	}

	public static void applyColorIRGB(int rbg) {
		applyColorIRGB(rbg, 1F);
	}

	public static void applyColorIRGB(int rbg, float brightness) {
		float red = (float) (rbg >> 16 & 255) / 255.0F * brightness;
		float blue = (float) (rbg >> 8 & 255) / 255.0F * brightness;
		float green = (float) (rbg & 255) / 255.0F * brightness;
		GL11.glColor4f(red, blue, green, (CCRenderState.alphaOverride > 0 ? CCRenderState.alphaOverride : 255) / 255F);
	}

	public static String cutStringToSize(FontRenderer fr, String str, int width) {
		if (fr.getStringWidth(str) > width) {
			String dots = "...";
			int dotsWidth = fr.getStringWidth(dots);
			int i;
			for (i = str.length() / 2; i > 0; i--) {
				int length = fr.getStringWidth(str.substring(0, i)) + dotsWidth
						+ fr.getStringWidth(str.substring(str.length() - i, str.length()));
				if (length < width)
					break;
			}
			str = str.substring(0, i) + dots + str.substring(str.length() - i, str.length());
		}
		return str;
	}

	public static int glGetFramebufferAttachmentParameteri(int target, int attachment, int pname) {
		if (OpenGlHelper.framebufferSupported) {
			int fboType = ReflectionHelper.getPrivateValue(OpenGlHelper.class, null, "field_153212_w");
			switch (fboType) {
				case 0:
					return GL30.glGetFramebufferAttachmentParameteri(target, attachment, pname);
				case 1:
					return ARBFramebufferObject.glGetFramebufferAttachmentParameteri(target, attachment, pname);
				case 2:
					return EXTFramebufferObject.glGetFramebufferAttachmentParameteriEXT(target, attachment, pname);
			}
		}
		return 0;
	}

	private static float lightX, lightY;

	public static void setBrightness(float lightX, float lightY) {
		RenderUtils.lightX = OpenGlHelper.lastBrightnessX;
		RenderUtils.lightY = OpenGlHelper.lastBrightnessY;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightX, lightY);
	}

	public static void resetBrightness() {
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightX, lightY);
	}
}
