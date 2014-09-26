package vic.mod.integratedcircuits.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.ClientProxy;
import vic.mod.integratedcircuits.DiskDriveUtils;
import vic.mod.integratedcircuits.DiskDriveUtils.ModelFloppy;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.TileEntityAssembler;
import vic.mod.integratedcircuits.util.RenderUtils;

public class TileEntityAssemblerRenderer extends TileEntitySpecialRenderer
{
	private static ModelFloppy model = new ModelFloppy(-7, -7, -9, 12, 2, 1);
	
	private ResourceLocation safetyRegulationsTex = new ResourceLocation(IntegratedCircuits.modID, "textures/blocks/assembler_safety.png");
	private ResourceLocation bottomTex = new ResourceLocation(IntegratedCircuits.modID, "textures/blocks/assembler_bottom.png");
	
	public void renderTileEntityAt(TileEntityAssembler te, double x, double y, double z, float partialTicks)
	{		
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		GL11.glRotatef(-90 * te.rotation, 0, 1, 0);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		
		GL11.glPushMatrix();
		Tessellator tes = Tessellator.instance;
		this.bindTexture(bottomTex);
		tes.startDrawingQuads();
		tes.addVertexWithUV(0, 8 / 16F, 0, 0, 0);
		tes.addVertexWithUV(0, 8 / 16F, 1, 0, 1);
		tes.addVertexWithUV(1, 8 / 16F, 1, 1, 1);
		tes.addVertexWithUV(1, 8 / 16F, 0, 1, 0);
		tes.draw();
		
		if(te.circuitFBO != null)
		{
			te.circuitFBO.bindFramebufferTexture();
			tes.startDrawingQuads();
			tes.addVertexWithUV(0, 9 / 16F, 0, 0, 0);
			tes.addVertexWithUV(0, 9 / 16F, 1, 0, 1);
			tes.addVertexWithUV(1, 9 / 16F, 1, 1, 1);
			tes.addVertexWithUV(1, 9 / 16F, 0, 1, 0);
			tes.draw();
		}

		GL11.glRotatef(180, 0, 0, 1);
		GL11.glTranslatef(-1.005F, -1, 0);
		this.bindTexture(safetyRegulationsTex);
		tes.startDrawingQuads();
		tes.addVertexWithUV(0, 9 / 16F, 1 - 7 / 16F, 0, 0);
		tes.addVertexWithUV(0, 1 - 3 / 16F, 1 - 7 / 16F, 0, 1);
		tes.addVertexWithUV(0, 1 - 3 / 16F, 0 + 1 / 16F, 1, 1);
		tes.addVertexWithUV(0, 9 / 16F, 0 + 1 / 16F, 1, 0);
		tes.draw();
		GL11.glPopMatrix();	
		
		GL11.glPushMatrix();
			GL11.glColor3f(0, 0, 0);
			GL11.glTranslatef(0.5F, 14 / 16F, 0.5F);
			GL11.glRotatef(45, 0, 1, 0);
			ModelLaser.instance = new ModelLaser();
			
			GL11.glPushMatrix();
			GL11.glTranslatef(0, 0, -0.5F);
			GL11.glRotatef(-90, 0, 1, 0);
			ModelLaser.instance.render(1 / 64F, -30, 0, true, partialTicks, te);
			GL11.glPopMatrix();
			
			GL11.glPushMatrix();
			GL11.glRotatef(90, 0, 1, 0);
			GL11.glTranslatef(0, 0, -0.5F);
			GL11.glRotatef(-90, 0, 1, 0);
			ModelLaser.instance.render(1 / 64F, -30, 0, true, partialTicks, te);
			GL11.glPopMatrix();
			
			GL11.glPushMatrix();
			GL11.glRotatef(180, 0, 1, 0);
			GL11.glTranslatef(0, 0, -0.5F);
			GL11.glRotatef(-90, 0, 1, 0);
			ModelLaser.instance.render(1 / 64F, -30, 0, true, partialTicks, te);
			GL11.glPopMatrix();
			
			GL11.glPushMatrix();
			GL11.glRotatef(270, 0, 1, 0);
			GL11.glTranslatef(0, 0, -0.5F);
			GL11.glRotatef(-90, 0, 1, 0);
			ModelLaser.instance.render(1 / 64F, -30, 0, true, partialTicks, te);
			GL11.glPopMatrix();	
		GL11.glPopMatrix();
		
		GL11.glPopMatrix();
		DiskDriveUtils.renderFloppy(te, model, x, y, z, partialTicks, te.rotation);
		GL11.glDisable(GL11.GL_LIGHTING);
	}
	
	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks)
	{
		this.renderTileEntityAt((TileEntityAssembler)te, x, y, z, partialTicks);
	}
	
	public static class ModelLaser extends ModelBase
	{
		public static ModelLaser instance = new ModelLaser();
		
		public ModelRenderer base;
		public ModelRenderer stick;
		public ModelRenderer head1;
		public ModelRenderer head2;
		public ModelRenderer[] torus;
		
		public ModelLaser()
		{
			base = new ModelRenderer(this);
			base.addBox(0, -4, -4, 2, 8, 8);
			stick = new ModelRenderer(this);
			stick.addBox(0, -22, -1, 1, 22, 2);
			head1 = new ModelRenderer(this);
			head1.addBox(2, -2, -2, 10, 4, 4);
			head2 = new ModelRenderer(this);
			head2.addBox(12, -1, -1, 3, 2, 2);
			
			torus = new ModelRenderer[4];
			for(int i = 0; i < 4; i++)
			{
				torus[i] = new ModelRenderer(this);
				torus[i].addBox(3 + i * 2, -3, -3, 1, 6, 6);
			}
		}
		
		public void render(float scale, float h1, float h2, boolean spinning, float partialTicks, TileEntity te)
		{
			h1 = (float)Math.toRadians(h1);
			h2 = (float)Math.toRadians(h2);
			
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glColor3f(0.1F, 0.1F, 0.1F);
			base.rotateAngleZ = h1;
			base.rotateAngleY = h2;
			base.render(scale);
			
			stick.render(scale);
			
			float rot = (float)Math.toRadians((float)ClientProxy.clientTicks * 4 + partialTicks * 4);
			
			GL11.glColor3f(0.2F, 0.2F, 0.2F);
			head1.rotateAngleZ = h1;
			head1.rotateAngleX = rot;
			head1.rotateAngleY = h2;
			head1.render(scale);
			
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
			if(spinning) GL11.glColor3f(1, 0, 0);
			else GL11.glColor3f(0.4F, 0, 0);
			head2.rotateAngleZ = h1;
			head2.rotateAngleX = rot;
			head2.rotateAngleY = h2;	
			head2.render(scale);
			
			int enabled = ClientProxy.clientTicks % 40 / 10;
			for(int i = 0; i < 4; i++)
			{
				torus[i].rotateAngleZ = h1;
				torus[i].rotateAngleX = rot;
				torus[i].rotateAngleY = h2;
				
				if(i == enabled && spinning) GL11.glColor3f(1, 0, 0);
				else GL11.glColor3f(0.4F, 0, 0);
				
				torus[i].render(scale);
			}
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			RenderUtils.resetBrightness(te);
		}
	}
}
