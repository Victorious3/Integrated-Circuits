package vic.mod.integratedcircuits;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class DiskDrive 
{
	public static AxisAlignedBB getDiskDriveBoundingBox(IDiskDrive drive, int x, int y, int z, Vec3 hitVec)
	{
		AxisAlignedBB box = drive.getBoundingBox();
		box.offset(x, y, z);
		if(!box.isVecInside(hitVec)) return null;
		return box;
	}
	
	public static void dropFloppy(IDiskDrive drive, World world, int x, int y, int z)
	{
		if(drive.getDisk() != null) world.spawnEntityInWorld(new EntityItem(world, x, y, z, drive.getDisk()));
	}
	
	@SideOnly(Side.CLIENT)
	public static void renderFloppy(IDiskDrive drive, ModelFloppy model, double x, double y, double z, float partialTicks, int rotation)
	{
		if(drive.getDisk() != null)
		{
			ItemStack floppy = drive.getDisk();
			String name = floppy.getTagCompound() != null 
				&& floppy.getTagCompound().hasKey("circuit") 
				? floppy.getTagCompound().getCompoundTag("circuit").getCompoundTag("properties").getString("name") : null;
			
			GL11.glPushMatrix();
			GL11.glTranslated(x, y, z);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glColor3f(0.05F, 0.05F, 0.05F);
			model.floppy.rotateAngleY = (float)Math.toRadians(-90 * rotation);
			model.floppy.render(1 / 16F);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glPopMatrix();
			
			if(name != null)
			{
				FontRenderer fr = RenderManager.instance.getFontRenderer();
				GL11.glPushMatrix();
				GL11.glTranslated(x, y, z);
				GL11.glRotatef(180, 0, 0, 1);
				GL11.glTranslatef(-0.5F, -0.5F, 0.5F);
				GL11.glRotatef(90 * rotation, 0, 1, 0);
				GL11.glTranslatef(0.5F, 0.5F, -0.5F);
				GL11.glTranslatef(-1, -1, 0);
				float scale = 1 / 128F;
				GL11.glScalef(scale, scale, scale);
				GL11.glTranslated(32, 110, -8.005);
				GL11.glDisable(GL11.GL_LIGHTING);
				fr.drawString(name, 0, 0, 0xFFFFFF);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glPopMatrix();
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static class ModelFloppy extends ModelBase
	{
		public ModelRenderer floppy;
		
		public ModelFloppy(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
		{
			floppy = new ModelRenderer(this);
			floppy.addBox(minX, minY, minZ, maxX, maxY, maxZ);
			floppy.setRotationPoint(8, 8, 8);
		}
	}
}
