package vic.mod.integratedcircuits;

import net.minecraft.client.shader.Framebuffer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityAssembler extends TileEntity
{
	@SideOnly(Side.CLIENT)
	public Framebuffer circuitFBO;
	
	@SideOnly(Side.CLIENT)
	public void initFramebuffer()
	{
		circuitFBO = new Framebuffer(64, 64, false);
		circuitFBO.bindFramebuffer(false);
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glViewport(0, 0, 64, 64);
//		GL11.glOrtho(0, 64, 64, 0, -1, 1); If I enable that, it won't draw anything at all.
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glColor3f(1F, 0, 0);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2i(0, 0);
		GL11.glVertex2i(0, 64);
		GL11.glVertex2i(64, 64);
		GL11.glVertex2i(64, 0);
		GL11.glEnd();
		GL11.glColor3f(0, 1F, 0);
		GL11.glBegin(GL11.GL_POINTS);
		GL11.glVertex2i(0, 0);
		GL11.glEnd();
		GL11.glBegin(GL11.GL_POINTS);
		GL11.glVertex2i(25, 25);
		GL11.glEnd();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		circuitFBO.unbindFramebuffer();
	}
}
