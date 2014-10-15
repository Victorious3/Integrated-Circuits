package vic.mod.integratedcircuits.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.proxy.ClientProxy;
import vic.mod.integratedcircuits.util.RenderUtils;

public class ModelLaser extends ModelBase
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
	
	public void render(float scale, float h1, float h2, boolean spinning, float partialTicks, TileEntity te)
	{
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glPushMatrix();
		GL11.glColor3f(0.1F, 0.1F, 0.1F);
		if(te != null) stick.render(scale);
		
		GL11.glRotatef(h2, 0, 1, 0);
		GL11.glRotatef(h1, 0, 0, 1);
		base.render(scale);
		float rot = spinning ? (float)Math.toRadians((float)ClientProxy.clientTicks * 4 + partialTicks * 4) : 0;
		
		GL11.glColor3f(0.2F, 0.2F, 0.2F);
		head1.rotateAngleX = rot;
		head1.render(scale);
		
		if(te != null) OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		
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
		if(te != null) RenderUtils.resetBrightness(te);
		GL11.glColor3f(1F, 1F, 1F);
	}
}