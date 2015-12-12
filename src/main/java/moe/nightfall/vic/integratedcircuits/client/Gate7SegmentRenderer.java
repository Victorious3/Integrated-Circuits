package moe.nightfall.vic.integratedcircuits.client;

import moe.nightfall.vic.integratedcircuits.client.model.ModelSegment;
import moe.nightfall.vic.integratedcircuits.gate.Gate7Segment;
import moe.nightfall.vic.integratedcircuits.misc.RenderUtils;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import codechicken.lib.vec.Transformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Gate7SegmentRenderer extends PartRenderer<Gate7Segment> {
	private int display;
	private int color;

	public Gate7SegmentRenderer() {
		models.add(new ModelSegment());
	}

	@Override
	public void prepare(Gate7Segment part) {
		/*
		 * super.prepare(part); if(part.isSlave) { prepareBundled(0);
		 * prepareRedstone(0, 0); } else { int i1 = 15; if(part.hasSlaves) i1 =
		 * 13; if(part.mode > 1) { prepareBundled(i1); prepareRedstone(0, 0); }
		 * else { prepareBundled(0); prepareRedstone(i1, part.io); } }
		 */
	}

	@Override
	public void prepareInv(ItemStack stack) {
		display = 127;
		color = stack.getItemDamage();
	}

	@Override
	public void prepareDynamic(Gate7Segment part, float partialTicks) {
		display = part.digit;
		color = part.color;
	}

	@Override
	public void renderDynamic(Transformation t) {
		GL11.glPushMatrix();
		t.glApply();
		GL11.glTranslatef(0.5F, 0, 0.5F);
		GL11.glRotatef(180, 0, 1, 0);
		GL11.glTranslatef(-0.5F, 0, -0.5F);

		GL11.glTranslatef(17 / 64F, 1 / 16F + 0.002F, 11 / 64F);
		GL11.glDisable(GL11.GL_LIGHTING);
		render7Segment(display, 1 / 48F, color);
		GL11.glEnable(GL11.GL_LIGHTING);

		GL11.glPopMatrix();
	}

	public static void render7Segment(int display, float scale, int color) {
		renderSegment((display & 2) != 0, 17, 4, 20, 15, scale, color); // 1
		renderSegment((display & 4) != 0, 17, 18, 20, 29, scale, color); // 2

		renderSegment((display & 1) != 0, 6, 1, 17, 4, scale, color); // 0
		renderSegment((display & 64) != 0, 6, 15, 17, 18, scale, color); // 6
		renderSegment((display & 8) != 0, 6, 29, 17, 32, scale, color); // 3

		renderSegment((display & 16) != 0, 3, 18, 6, 29, scale, color); // 4
		renderSegment((display & 32) != 0, 3, 4, 6, 15, scale, color); // 5

		renderSegment((display & 128) != 0, 20, 29, 23, 32, scale, color); // 7
																			// (dot)
	}

	public static void renderSegment(boolean enabled, int x1, int y1, int x2, int y2, float scale, int color) {
		color = MapColor.getMapColorForBlockColored(color).colorValue;
		if (enabled) {
			RenderUtils.applyColorIRGB(color);
			RenderUtils.setBrightness(240, 240);
		} else
			RenderUtils.applyColorIRGB(color, 0.2F);

		double u1 = Resources.ICON_IC_SEGMENT.getInterpolatedU(x1 / 2D);
		double u2 = Resources.ICON_IC_SEGMENT.getInterpolatedU(x2 / 2D);
		double v1 = Resources.ICON_IC_SEGMENT.getInterpolatedV(y1 / 2D);
		double v2 = Resources.ICON_IC_SEGMENT.getInterpolatedV(y2 / 2D);

		Tessellator tes = Tessellator.instance;
		tes.startDrawingQuads();
		tes.addVertexWithUV(x1 * scale, 0, y1 * scale, u1, v1);
		tes.addVertexWithUV(x1 * scale, 0, y2 * scale, u1, v2);
		tes.addVertexWithUV(x2 * scale, 0, y2 * scale, u2, v2);
		tes.addVertexWithUV(x2 * scale, 0, y1 * scale, u2, v1);
		tes.draw();

		if (enabled)
			RenderUtils.resetBrightness();
	}
}
