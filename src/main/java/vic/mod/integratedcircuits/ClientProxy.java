package vic.mod.integratedcircuits;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.world.WorldEvent;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.client.TileEntityAssemblerRenderer;
import vic.mod.integratedcircuits.client.TileEntityPCBLayoutRenderer;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public void initialize() 
	{
		super.initialize();
		registerRenderers();
	}
	
	public void registerRenderers()
	{
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPCBLayout.class, new TileEntityPCBLayoutRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAssembler.class, new TileEntityAssemblerRenderer());
	}
	
	@SubscribeEvent
	public void onBlockHighlight(DrawBlockHighlightEvent event)
	{
		World world = event.player.worldObj;
		if((event.target == null) || (event.target.typeOfHit != MovingObjectType.BLOCK)) return;
		int x = event.target.blockX;
		int y = event.target.blockY;
		int z = event.target.blockZ;
		
		AxisAlignedBB box = null;
		Block block = world.getBlock(x, y, z);
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		
		if(block instanceof BlockPCBLayout)
		{
			box = getDiskDriveBoundingBox((TileEntityPCBLayout)tileEntity, x, y, z, event.target.hitVec);
		}
		
		if(box == null) return;
		
		double xOff = event.player.lastTickPosX + (event.player.posX - event.player.lastTickPosX) * event.partialTicks;
		double yOff = event.player.lastTickPosY + (event.player.posY - event.player.lastTickPosY) * event.partialTicks;
		double zOff = event.player.lastTickPosZ + (event.player.posZ - event.player.lastTickPosZ) * event.partialTicks;
		box = box.offset(-xOff, -yOff, -zOff).expand(0.002, 0.002, 0.002);
        
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
		GL11.glLineWidth(2.0F);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDepthMask(false);

		RenderGlobal.drawOutlinedBoundingBox(box, -1);

		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		
		event.setCanceled(true);
	}
	
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event)
	{
		for(Framebuffer buf : TileEntityAssembler.fboArray)
		{
			buf.deleteFramebuffer();
		}
		TileEntityAssembler.fboArray.clear();
	}
}
