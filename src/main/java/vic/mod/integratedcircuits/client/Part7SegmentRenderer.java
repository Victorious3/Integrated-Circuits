package vic.mod.integratedcircuits.client;

import net.minecraft.block.material.MapColor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.client.model.ModelSocket;
import vic.mod.integratedcircuits.misc.RenderUtils;
import vic.mod.integratedcircuits.part.Part7Segment;
import codechicken.lib.vec.Transformation;

public class Part7SegmentRenderer extends PartRenderer<Part7Segment>
{
	public Part7SegmentRenderer()
	{
		models.add(new ModelSocket());
		addBundledConnections(15, 1, 2, 1, 2);
		addRedstoneConnections(15, 1, 2, 1, 2);
	}
	
	private int display;
	private int color;
	
	@Override
	public void prepare(Part7Segment part) 
	{
		if(part.isSlave)
			prepareBundled(0);
		else if(part.hasSlaves)
			prepareBundled(13);
		else prepareBundled(15);
		prepareRedstone(0, 0);
	}

	@Override
	public void prepareInv(ItemStack stack) 
	{
		display = 127;
		color = stack.getItemDamage();
		prepareBundled(15);
		prepareRedstone(0, 0);
	}
	
	@Override
	public void prepareDynamic(Part7Segment part, float partialTicks) 
	{
		display = part.display;
		color = part.color;
	}

	@Override
	public void renderDynamic(Transformation t) 
	{
		GL11.glPushMatrix();
		t.glApply();
		GL11.glTranslatef(0.5F, 0, 0.5F);
		GL11.glRotatef(180, 0, 1, 0);
		GL11.glTranslatef(-0.5F, 0, -0.5F);
		
		GL11.glTranslatef(17 / 64F, 3 / 16F + 0.002F, 11 / 64F);
		render7Segment(display, 1 / 48F, color);
		
		GL11.glPopMatrix();
	}
	
	public static void render7Segment(int display, float scale, int color)
	{
		renderSegment((display & 2) != 0, 17, 4, 20, 15, scale, color);    //1
		renderSegment((display & 4) != 0, 17, 18, 20, 29, scale, color);   //2
		
		renderSegment((display & 1) != 0, 6, 1, 17, 4, scale, color);      //0
		renderSegment((display & 64) != 0, 6, 15, 17, 18, scale, color);   //6
		renderSegment((display & 8) != 0, 6, 29, 17, 32, scale, color);    //3
		
		renderSegment((display & 16) != 0, 3, 18, 6, 29, scale, color);    //4
		renderSegment((display & 32) != 0, 3, 4, 6, 15, scale, color);     //5
		
		renderSegment((display & 128) != 0, 20, 29, 23, 32, scale, color); //7 (dot)
	}
	
	public static void renderSegment(boolean enabled, int x1, int y1, int x2, int y2, float scale, int color)
	{
		color = MapColor.getMapColorForBlockColored(color).colorValue;
		if(enabled) 
		{
			RenderUtils.applyColorIRGB(color);
			RenderUtils.setBrightness(240, 240);
		}
		else RenderUtils.applyColorIRGB(color, 0.2F);
		
		double u1 = Resources.ICON_IC_SEGMENT.getInterpolatedU(x1 / 2D);
		double u2 = Resources.ICON_IC_SEGMENT.getInterpolatedU(x2 / 2D);
		double v1 = Resources.ICON_IC_SEGMENT.getInterpolatedV(y1 / 2D);
		double v2 = Resources.ICON_IC_SEGMENT.getInterpolatedV(y2 / 2D);

		Tessellator tes = Tessellator.instance;
		tes.startDrawingQuads();
		tes.setNormal(0, 1, 0);
		tes.addVertexWithUV(x1 * scale, 0, y1 * scale, u1, v1);
		tes.addVertexWithUV(x1 * scale, 0, y2 * scale, u1, v2);
		tes.addVertexWithUV(x2 * scale, 0, y2 * scale, u2, v2);
		tes.addVertexWithUV(x2 * scale, 0, y1 * scale, u2, v1);
		tes.draw();
		
		if(enabled) RenderUtils.resetBrightness();
	}
}
