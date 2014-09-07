package vic.mod.integratedcircuits;

import java.util.ArrayList;

import net.minecraft.client.shader.Framebuffer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityAssembler extends TileEntity
{
	@SideOnly(Side.CLIENT)
	public Framebuffer circuitFBO;
	
	//Used to unload the FBOs when the world does. If there is a better way to do this, tell me.
	@SideOnly(Side.CLIENT)
	public static ArrayList<Framebuffer> fboArray = new ArrayList<Framebuffer>();
	
	public TileEntityAssembler()
	{
		
	}
	
	@SideOnly(Side.CLIENT)
	public void initFramebuffer()
	{
		circuitFBO = new Framebuffer(64, 64, false);
		fboArray.add(circuitFBO);
		circuitFBO.bindFramebuffer(false);
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glViewport(0, 0, 64, 64);
		GL11.glOrtho(0, 64, 64, 0, -1, 1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
        
		GL11.glColor3f(1F, 0, 0);
		
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2i(0, 0);
		GL11.glVertex2i(0, 20);
		GL11.glVertex2i(20, 20);
		GL11.glVertex2i(20, 0);
		GL11.glEnd();
		
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2i(20, 20);
		GL11.glVertex2i(20, 45);
		GL11.glVertex2i(45, 45);
		GL11.glVertex2i(45, 20);
		GL11.glEnd();
		
		GL11.glLineWidth(1F);
		GL11.glColor3f(1F, 1F, 0);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2i(1, 1);
		GL11.glVertex2i(15, 30);
		GL11.glEnd();
		
		GL11.glBegin(GL11.GL_POINTS);
		GL11.glVertex2i(25, 25);
		GL11.glEnd();
		
		GL11.glBegin(GL11.GL_POINTS);
		GL11.glVertex2i(63, 63);
		GL11.glEnd();
		
		circuitFBO.unbindFramebuffer();
	}

	@Override
	public void updateEntity() 
	{
		if(circuitFBO == null && FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) initFramebuffer();
	}

	@Override
	public void invalidate() 
	{
		super.invalidate();
		if(worldObj.isRemote) 
		{
			circuitFBO.deleteFramebuffer();
			fboArray.remove(circuitFBO);
		}
	}
}
