package vic.mod.integratedcircuits.client;

import java.util.Random;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.shader.TesselatorVertexState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.DiskDriveUtils;
import vic.mod.integratedcircuits.DiskDriveUtils.ModelFloppy;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.TileEntityAssembler;
import vic.mod.integratedcircuits.proxy.ClientProxy;
import vic.mod.integratedcircuits.util.RenderUtils;

public class TileEntityAssemblerRenderer extends TileEntitySemiTransparentRenderer
{
	private static ModelFloppy model = new ModelFloppy(-7, -7, -9, 12, 2, 1);
	
	private ResourceLocation safetyRegulationsTex = new ResourceLocation(IntegratedCircuits.modID, "textures/blocks/assembler_safety.png");
	private ResourceLocation bottomTex = new ResourceLocation(IntegratedCircuits.modID, "textures/blocks/assembler_bottom.png");
	
	public void renderTileEntityAt(TileEntityAssembler te, double x, double y, double z, float partialTicks)
	{	
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		GL11.glRotatef(-90 * te.rotation, 0, 1, 0);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		GL11.glColor3f(1, 1, 1);
		
		if(getCurrentRenderPass() == 0)
		{
			GL11.glPushMatrix();
			Tessellator tes = Tessellator.instance;
			this.bindTexture(bottomTex);
			tes.startDrawingQuads();
			tes.addVertexWithUV(0, 8 / 16F, 0, 0, 0);
			tes.addVertexWithUV(0, 8 / 16F, 1, 0, 1);
			tes.addVertexWithUV(1, 8 / 16F, 1, 1, 1);
			tes.addVertexWithUV(1, 8 / 16F, 0, 1, 0);
			tes.draw();
			
			if(te.matrix != null && te.verts != null)
			{
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glPushMatrix();
				
				GL11.glTranslatef(0.5F, 0, 0.5F);
				GL11.glRotatef(180, 0, 1, 0);
				GL11.glTranslatef(-0.5F, 0, -0.5F);
				
				GL11.glTranslatef(-1 + 3 / 16F, 0, -1 + 3 / 16F);
				GL11.glTranslatef(1, 8 / 16F + 0.0005F, 1);
				float s = 10 / 16F / te.size;
				GL11.glScalef(s, 1 / 80F, s);
				Tessellator verts = te.verts;
				TesselatorVertexState state = verts.getVertexState(0, 0, 0);
				verts.draw();
				verts.startDrawingQuads();
				verts.setVertexState(state);
				GL11.glPopMatrix();
				GL11.glEnable(GL11.GL_TEXTURE_2D);
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
		}
		
		GL11.glPushMatrix();
		GL11.glTranslatef(0.5F, 14 / 16F, 0.5F);
		GL11.glRotatef(45, 0, 1, 0);
		
		float x1 = 17;
		float y1 = 17;
		x1 += 0.5F;
		y1 += 0.5F;
		
		for(int i = 0; i < 4; i++)
		{
			GL11.glPushMatrix();
			GL11.glRotatef(90 * i, 0, 1, 0);
			GL11.glTranslatef(0, 0, -0.5F);
			GL11.glRotatef(-90, 0, 1, 0);
			float aZ = 0, aY = 0, w2 = 0, w3 = 0;
			
			if(te.matrix != null)
			{
				float x2 = x1;
				float y2 = y1;
				
				if(i == 3 || i == 1) 
				{
					x2 = y1;
					y2 = x1;
				}
				
				if(i == 3 || i == 0) x2 = te.size - x2;
				if(i == 1 || i == 0) y2 = te.size - y2;
				
				float w1 = (10 / 16F * (x2 / (float)te.size)) + (3 / 16F - (0.5F - (float)Math.sin(Math.PI / 4D) * 0.5F));
				float h1 = (10 / 16F * (y2 / (float)te.size)) + (3 / 16F - (0.5F - (float)Math.sin(Math.PI / 4D) * 0.5F));
				aZ = (float)Math.atan(w1 / h1);
				w2 = (float)(w1 / Math.sin(aZ));
				aZ = (float)Math.toDegrees(aZ) - 45F;
				aY = 90F - (float)Math.toDegrees(Math.atan(w2 / (6 / 16F)));
				w3 = w2 / (float)Math.sin(Math.toRadians(90F - aY));
			}
			
			if(getCurrentRenderPass() > 0) renderLasers(1 / 64F, -aY, aZ, w3, te, partialTicks);
			else ModelLaser.instance.render(1 / 64F, -aY, aZ, true, 0, partialTicks, te);
			GL11.glPopMatrix();
		}
		
		GL11.glPopMatrix();	
		if(getCurrentRenderPass() == 0) addToRenderQueue(te.xCoord, te.yCoord, te.zCoord);	
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
		
		if(getCurrentRenderPass() == 0) DiskDriveUtils.renderFloppy(te, model, x, y, z, partialTicks, te.rotation);
	}
	
	private Random rand = new Random();
	
	private void renderLasers(float scale, float aZ, float aY, float length, TileEntityAssembler te, float partialTicks)
	{
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		
		GL11.glPushMatrix();
		if(length > 0) 
		{
			GL11.glRotatef(aY, 0, 1, 0);
			GL11.glRotatef(aZ, 0, 0, 1);
			
			double offset = Math.abs(rand.nextGaussian());
			
			Tessellator tes = Tessellator.instance;
			GL11.glScalef(scale, scale, scale);	
			for(int j = 0; j < 4; j++)
			{
				float color = (float)(0.75 + offset);
				tes.startDrawing(GL11.GL_QUADS);
				tes.setColorRGBA_F(color, 0, 0, 1);
				tes.addVertex(0, 0, 0);
				tes.addVertex(length / scale, 0, 0);	
				tes.setColorRGBA_F(0, 0, 0, 1);
				tes.addVertex(length / scale, 0.5, 0.5);
				tes.addVertex(0, 0.5, 0.5);
				tes.draw();
				GL11.glRotatef(90, 1, 0, 0);
			}
			GL11.glScalef(1 / scale, 1 / scale, 1 / scale);
		}
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
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
			stick.addBox(0, -24, -1, 1, 24, 2);
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
		
		public void render(float scale, float h1, float h2, boolean spinning, float length, float partialTicks, TileEntity te)
		{
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glPushMatrix();
			GL11.glColor3f(0.1F, 0.1F, 0.1F);
			stick.render(scale);
			
			GL11.glRotatef(h2, 0, 1, 0);
			GL11.glRotatef(h1, 0, 0, 1);
			base.render(scale);
			float rot = (float)Math.toRadians((float)ClientProxy.clientTicks * 4 + partialTicks * 4);
			
			GL11.glColor3f(0.2F, 0.2F, 0.2F);
			head1.rotateAngleX = rot;
			head1.render(scale);
			
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
			
			if(spinning) GL11.glColor3f(1, 0, 0);
			else GL11.glColor3f(0.4F, 0, 0);
			
			head2.rotateAngleX = rot;
			head2.render(scale);
			
			int enabled = ClientProxy.clientTicks % 40 / 10;
			for(int i = 0; i < 4; i++)
			{
				torus[i].rotateAngleX = rot;
				if(i == enabled && spinning) GL11.glColor3f(1, 0, 0);
				else GL11.glColor3f(0.4F, 0, 0);
				torus[i].render(scale);
			}
			
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glPopMatrix();
			RenderUtils.resetBrightness(te);
		}
	}
}
