package vic.mod.integratedcircuits.client;

import net.minecraft.client.Minecraft;
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
		RenderUtils.setBrightness(240, 240);
		GL11.glColor3f(0, 1, 0);
		
		double u1 = Resources.ICON_IC_SEGMENT.getInterpolatedU(0);
		double u2 = Resources.ICON_IC_SEGMENT.getInterpolatedU(11.5);
		double v1 = Resources.ICON_IC_SEGMENT.getInterpolatedV(0.5);
		double v2 = Resources.ICON_IC_SEGMENT.getInterpolatedV(16);
		double y = 3 / 16F + 0.002F;
		
		Tessellator tes = Tessellator.instance;
		tes.startDrawingQuads();
		tes.setNormal(0, 1, 0);
		tes.addVertexWithUV(3.5 / 16F, y, 2.5 / 16F, u1, v1);
		tes.addVertexWithUV(3.5 / 16F, y, 13.5 / 16F, u1, v2);
		tes.addVertexWithUV(12.5 / 16F, y, 13.5 / 16F, u2, v2);
		tes.addVertexWithUV(12.5 / 16F, y, 2.5 / 16F, u2, v1);
		tes.draw();
		
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glScalef(1 / 32F, 1 / 32F, 1 / 32F);
		Minecraft.getMinecraft().fontRenderer.drawString(String.valueOf(display), 0, 0, 0xFFFFFF, false);
		GL11.glEnable(GL11.GL_LIGHTING);
		
		RenderUtils.resetBrightness();
		GL11.glPopMatrix();
	}
}
