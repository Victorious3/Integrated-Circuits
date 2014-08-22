package vic.mod.integratedcircuits.client;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.TileEntityPCBLayout;

public class TileEntityPCBLayoutRenderer extends TileEntitySpecialRenderer
{
	public void renderTileEntityAt(TileEntityPCBLayout te, double x, double y, double z, float partialTicks) 
	{
		if(te.getStackInSlot(0) != null)
		{
			ItemStack floppy = te.getStackInSlot(0);
			String name = floppy.getTagCompound() != null && floppy.getTagCompound().hasKey("name") ? floppy.getTagCompound().getString("name") : null;
			
			GL11.glPushMatrix();
			GL11.glTranslated(x, y, z);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glColor3f(0.05F, 0.05F, 0.05F);
			ModelFloppy.instance.floppy.rotateAngleY = (float)Math.toRadians(-90 * te.rotation);
			ModelFloppy.instance.floppy.render(1 / 16F);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glPopMatrix();
			
			if(name != null)
			{
				FontRenderer fr = RenderManager.instance.getFontRenderer();
				GL11.glPushMatrix();
				GL11.glTranslated(x, y, z);
				GL11.glRotatef(180, 0, 0, 1);
				GL11.glTranslatef(-0.5F, -0.5F, 0.5F);
				GL11.glRotatef(90 * te.rotation, 0, 1, 0);
				GL11.glTranslatef(0.5F, 0.5F, -0.5F);
				GL11.glTranslatef(-1, -1, 0);
				float scale = 1 / 128F;
				GL11.glScalef(scale, scale, scale);
				GL11.glTranslated(32, 110, -8.005);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glDisable(GL11.GL_LIGHTING);
				fr.drawString(name, 0, 0, 0xFFFFFF);
				GL11.glPopMatrix();
			}
		}
	}
	
	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) 
	{
		renderTileEntityAt((TileEntityPCBLayout)te, x, y, z, partialTicks);
	}
	
	private static class ModelFloppy extends ModelBase
	{
		public static ModelFloppy instance = new ModelFloppy();
		
		public ModelRenderer floppy;
		
		public ModelFloppy()
		{
			floppy = new ModelRenderer(this);
			floppy.addBox(-7, -7, -9, 12, 2, 1);
			floppy.setRotationPoint(8, 8, 8);
		}
	}
}
