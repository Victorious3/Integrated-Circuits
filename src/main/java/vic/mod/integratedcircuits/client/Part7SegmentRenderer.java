package vic.mod.integratedcircuits.client;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.Resources;
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
	
	@Override
	public void prepare(Part7Segment part) 
	{
		if(part.isSlave)
			prepareBundled(0);
		else if(part.slaves.size() > 0)
			prepareBundled(13);
		else prepareBundled(15);
		prepareRedstone(0, 0);
	}

	@Override
	public void prepareInv(ItemStack stack) 
	{
		prepareBundled(15);
		prepareRedstone(0, 0);
	}
	
	@Override
	public void prepareDynamic(Part7Segment part, float partialTicks) 
	{
		display = part.display;
	}

	@Override
	public void renderDynamic(Transformation t) 
	{
		GL11.glPushMatrix();
		t.glApply();
		GL11.glTranslatef(0.5F, 0, 0.5F);
		GL11.glRotatef(180, 0, 1, 0);
		GL11.glTranslatef(-0.5F, 0, -0.5F);
		
		renderSegment((display & 2) != 0, 17, 4, 20, 15);   //1
		renderSegment((display & 4) != 0, 17, 18, 20, 29);  //2
		
		renderSegment((display & 1) != 0, 6, 1, 17, 4);     //0
		renderSegment((display & 64) != 0, 6, 15, 17, 18);  //6
		renderSegment((display & 8) != 0, 6, 29, 17, 32);   //3
		
		renderSegment((display & 16) != 0, 3, 18, 6, 29);   //4
		renderSegment((display & 32) != 0, 3, 4, 6, 15);    //5
		
		renderSegment((display & 128) != 0, 20, 29, 23, 32); //7 (dot)
		
		GL11.glPopMatrix();
	}
	
	public void renderSegment(boolean enabled, int x1, int y1, int x2, int y2)
	{
		if(enabled) 
		{
			GL11.glColor3f(0, 1, 0);
			RenderUtils.setBrightness(240, 240);
		}
		else GL11.glColor3f(0, 0.2F, 0);
		
		double u1 = Resources.ICON_IC_SEGMENT.getInterpolatedU(x1 / 2D);
		double u2 = Resources.ICON_IC_SEGMENT.getInterpolatedU(x2 / 2D);
		double v1 = Resources.ICON_IC_SEGMENT.getInterpolatedV(y1 / 2D);
		double v2 = Resources.ICON_IC_SEGMENT.getInterpolatedV(y2 / 2D);
		double y = 3 / 16F + 0.002F;
		
		float s = 48F;
		float xOff = 17 / 64F, yOff = 11 / 64F;
		
		Tessellator tes = Tessellator.instance;
		tes.startDrawingQuads();
		tes.setNormal(0, 1, 0);
		tes.addVertexWithUV(x1 / s + xOff, y, y1 / s + yOff, u1, v1);
		tes.addVertexWithUV(x1 / s + xOff, y, y2 / s + yOff, u1, v2);
		tes.addVertexWithUV(x2 / s + xOff, y, y2 / s + yOff, u2, v2);
		tes.addVertexWithUV(x2 / s + xOff, y, y1 / s + yOff, u2, v1);
		tes.draw();
		
		if(enabled) RenderUtils.resetBrightness();
	}
}
