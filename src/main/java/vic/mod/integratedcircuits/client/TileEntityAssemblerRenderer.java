package vic.mod.integratedcircuits.client;

import java.util.LinkedList;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.ClientProxy;
import vic.mod.integratedcircuits.DiskDriveUtils;
import vic.mod.integratedcircuits.DiskDriveUtils.ModelFloppy;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.TileEntityAssembler;
import vic.mod.integratedcircuits.util.RenderUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityAssemblerRenderer extends TileEntitySpecialRenderer
{
	private static ModelFloppy model = new ModelFloppy(-7, -7, -9, 12, 2, 1);
	
	private ResourceLocation safetyRegulationsTex = new ResourceLocation(IntegratedCircuits.modID, "textures/blocks/assembler_safety.png");
	private ResourceLocation bottomTex = new ResourceLocation(IntegratedCircuits.modID, "textures/blocks/assembler_bottom.png");

	//Used to unload the FBOs when the world does. If there is a better way to do this, tell me.
	@SideOnly(Side.CLIENT)
	public static LinkedList<Framebuffer> fboArray;
	
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
			float scale = te.size / 68F;
			te.circuitFBO.bindFramebufferTexture();
			tes.startDrawingQuads();
			tes.addVertexWithUV(3 / 16F, 8 / 16F + 0.0005F, 3 / 16F, scale, 0);
			tes.addVertexWithUV(3 / 16F, 8 / 16F + 0.0005F, 13 / 16F, scale, scale);
			tes.addVertexWithUV(13 / 16F, 8 / 16F + 0.0005F, 13 / 16F, 0, scale);
			tes.addVertexWithUV(13 / 16F, 8 / 16F + 0.0005F, 3 / 16F, 0, 0);
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
			
			int x1 = 5;
			int y1 = 5;
			
			for(int i = 0; i < 4; i++)
			{
				GL11.glPushMatrix();
				GL11.glRotatef(90 * i, 0, 1, 0);
				GL11.glTranslatef(0, 0, -0.5F);
				GL11.glRotatef(-90, 0, 1, 0);
				float aZ = 0, aY = 0, w2 = 0, w3 = 0;
				if(te.matrix != null)
				{
					int x2 = x1;
					int y2 = y1;
					
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
					//TODO Not precise.
					w3 = (float)Math.sin(Math.toRadians(aY)) * w2;
				}
				ModelLaser.instance.render(1 / 64F, -aY, aZ, true, (int)Math.ceil(w3 * 128F), partialTicks, te);
				GL11.glPopMatrix();
			}
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
		public ModelRenderer laser;
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
			laser = new ModelRenderer(this);
			
			torus = new ModelRenderer[4];
			for(int i = 0; i < 4; i++)
			{
				torus[i] = new ModelRenderer(this);
				torus[i].addBox(3 + i * 2, -3, -3, 1, 6, 6);
			}
		}
		
		public void render(float scale, float h1, float h2, boolean spinning, int length, float partialTicks, TileEntity te)
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
			
			if(length > 0)
			{
				GL11.glDisable(GL11.GL_LIGHTING);
				//TODO Maybe a little bit of bloom around there? Have to figure out how to do shaders.
				boxList.remove(laser);
				laser = new ModelRenderer(this);
				laser.addBox(15F, -0.5F, -0.5F, length, 1, 1);
				laser.render(scale);
				GL11.glEnable(GL11.GL_LIGHTING);
			}
			
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
	
	public static void updateFramebuffer(TileEntityAssembler te)
	{
		if(te.circuitFBO == null)
		{
			te.circuitFBO = new Framebuffer(68, 68, false);
			TileEntityAssemblerRenderer.fboArray.add(te.circuitFBO);
		}
		te.circuitFBO.framebufferClear();
		if(te.matrix == null) return;
		te.circuitFBO.bindFramebuffer(false);
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glViewport(0, 0, te.size, te.size);
		GL11.glOrtho(0, te.size, te.size, 0, -1, 1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
        
		GL11.glColor3f(0, 0.3F, 0);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2i(0, 0);
		GL11.glVertex2i(0, te.size);
		GL11.glVertex2i(te.size, te.size);
		GL11.glVertex2i(te.size, 0);
		GL11.glEnd();
		
		GL11.glLineWidth(1F);
		GL11.glColor3f(0, 0.8F, 0);
		GL11.glBegin(GL11.GL_POINTS);
		for(int x = 0; x < te.size; x++)
			for(int y = 0; y < te.size; y++)
				if(te.matrix[x][y] > 0) GL11.glVertex2i(x, y + 1);
		GL11.glEnd();
		
		te.circuitFBO.unbindFramebuffer();
	}
}
