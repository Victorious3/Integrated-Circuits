package vic.mod.integratedcircuits;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.world.WorldEvent;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.client.ItemCircuitRenderer;
import vic.mod.integratedcircuits.client.PartCircuitRenderer;
import vic.mod.integratedcircuits.client.TileEntityAssemblerRenderer;
import vic.mod.integratedcircuits.client.TileEntityPCBLayoutRenderer;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	public static PartCircuitRenderer renderer;

	@Override
	public void initialize() 
	{
		super.initialize();
		registerRenderers();
	}
	
	public void registerRenderers()
	{
		TileEntityAssembler.fboArray = new LinkedList<Framebuffer>();
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPCBLayout.class, new TileEntityPCBLayoutRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAssembler.class, new TileEntityAssemblerRenderer());
		MinecraftForgeClient.registerItemRenderer(IntegratedCircuits.itemCircuit, new ItemCircuitRenderer());
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
		
		if(tileEntity instanceof IDiskDrive)
			box = DiskDriveUtils.getDiskDriveBoundingBox((IDiskDrive)tileEntity, x, y, z, event.target.hitVec);	
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
	
	//Don't even look at what's coming now. Not related at all.
	
	private static ResourceLocation crownLocation = new ResourceLocation(IntegratedCircuits.modID, "textures/crown.png");
	private static ResourceLocation haloLocation = new ResourceLocation(IntegratedCircuits.modID, "textures/halo.png");
	
	public static class ModelCrown extends ModelBase
	{
		public static ModelCrown instance = new ModelCrown();
		
		public ModelRenderer crown1;
		public ModelRenderer crown2;
		
		public ModelCrown()
		{
			int i1 = 7;
			int i2 = 18;	
			this.textureWidth = i1 * 2;
			this.textureHeight = i2;		
			float f1 = -(i1 / 2F);	
			float f2 = (float)(i1 * Math.sin(Math.toRadians(45)) + i1 / 2F);	
			
			crown1 = new ModelRenderer(this);
			crown1.setTextureOffset(0, 0);
			crown1.addBox(f1, 0, -f2, i1, i2, 0);
			crown1.setTextureOffset(7, 0);
			crown1.addBox(f1, 0, f2, i1, i2, 0);
			crown1.setTextureOffset(0, -7);
			crown1.addBox(-f2, 0, f1, 0, i2, i1);
			crown1.setTextureOffset(7, -7);
			crown1.addBox(f2, 0, f1, 0, i2, i1);
			crown1.rotateAngleY = (float) Math.toRadians(30);
			
			crown2 = new ModelRenderer(this);
			crown2.setTextureOffset(7, 0);
			crown2.addBox(f1, 0, -f2, i1, i2, 0);
			crown2.setTextureOffset(0, 0);
			crown2.addBox(f1, 0, f2, i1, i2, 0);
			crown2.setTextureOffset(7, -7);
			crown2.addBox(-f2, 0, f1, 0, i2, i1);
			crown2.setTextureOffset(0, -7);
			crown2.addBox(f2, 0, f1, 0, i2, i1);
			crown2.rotateAngleY = (float) Math.toRadians(75);
		}
		
		public void render(float scale)
		{
			Minecraft.getMinecraft().renderEngine.bindTexture(crownLocation);
			crown1.render(scale);
			crown2.render(scale);
		}
	}
	
	public static class ModelHalo extends ModelBase
	{
		public static ModelHalo instance = new ModelHalo();
		
		public ModelRenderer halo;
		
		public ModelHalo()
		{
			textureWidth = 256;
			textureHeight = 128;
			
			halo = new ModelRenderer(this);
			halo.addBox(-64, 0, -64, 128, 0, 128);
		}
		
		public void render(float scale)
		{
			Minecraft.getMinecraft().renderEngine.bindTexture(haloLocation);
			halo.render(scale);
		}
	}
	
	private float partialTicks;
	private long tickCounter;
	
	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event)
	{
		partialTicks = event.renderTickTime;
	}
	
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event)
	{
		tickCounter++;
	}
	
	@SubscribeEvent
	public void onLivingRender(RenderLivingEvent.Post event)
	{
		if(event.entity instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)event.entity;
			String name = player.getCommandSenderName();
			Minecraft mc = Minecraft.getMinecraft();
			
			int renderType = 0;
			if(name.equalsIgnoreCase("victorious3")) renderType = 1;
			else if(name.equalsIgnoreCase("thog92")) renderType = 2;
			if(renderType == 0) return;
			
			if(mc.thePlayer.getCommandSenderName().equals(name) 
				&& (mc.currentScreen instanceof GuiInventory 
				|| mc.currentScreen instanceof GuiContainerCreative)) return;
			if(renderType == 1 && player.inventory.armorItemInSlot(3) != null) return;
			
			float yaw = interpolateRotation(player.prevRotationYawHead, player.rotationYawHead, partialTicks);
			float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
			float headOffset = player.height - player.eyeHeight - 0.266F;
			if(player.isSneaking()) headOffset -= 0.0625;
			
			GL11.glPushMatrix();
			GL11.glTranslated(event.x, event.y, event.z);
			
			GL11.glRotatef(180, 0, 0, 1);
			GL11.glTranslated(0, -headOffset, 0);
			GL11.glRotated(yaw + 270, 0, 1, 0);
			GL11.glRotated(pitch, 0, 0, 1);	
			GL11.glTranslated(0, headOffset, 0);
			
			if(renderType == 2)
			{
				//Le halo
				GL11.glPushMatrix();
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GL11.glColor4f(1F, 1F, 1F, 1F);
				float scale = 1 / 128F;
				GL11.glTranslated(0, -player.height - player.eyeHeight - 5 * scale + (player.isSneaking() ? 0.0625 : 0), 0);
				GL11.glRotated(25, 1, 0, -1);
				GL11.glTranslatef(-32 * scale, -8 * scale, -20 * scale);
				ModelHalo.instance.halo.rotateAngleY = (float)Math.toRadians(tickCounter + partialTicks);
				ModelHalo.instance.render(scale);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glShadeModel(GL11.GL_FLAT);
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glPopMatrix();
			}
			else if(renderType == 1)
			{
				//Le crown
				GL11.glPushMatrix();
				float scale = 1 / 64F;
				GL11.glTranslated(0, -player.height - player.eyeHeight - 14 * scale + (player.isSneaking() ? 0.0625 : 0), 0);
				GL11.glTranslated(15 * scale, -2 * scale, 15 * scale);
				
				float f1 = (float)(7 * Math.sin(Math.toRadians(45)) + 7 / 2F) * scale;
				GL11.glTranslatef(-f1, 0, -f1);
				GL11.glRotated(-25, 1, 0, -1);
				GL11.glTranslatef(f1, 0, f1);
				
				ModelCrown.instance.render(scale);
				GL11.glPopMatrix();
			}
			
			GL11.glPopMatrix();
		}
	}
	
	private float interpolateRotation(float f1, float f2, float p1)
    {
		float f3;
		for(f3 = f2 - f1; f3 < -180.0F; f3 += 360.0F);
		while(f3 >= 180.0F) f3 -= 360.0F;
		return f1 + p1 * f3;
    }
}
