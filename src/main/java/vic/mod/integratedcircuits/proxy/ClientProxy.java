package vic.mod.integratedcircuits.proxy;

import java.lang.reflect.Field;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.world.WorldEvent;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.Constants;
import vic.mod.integratedcircuits.DiskDrive;
import vic.mod.integratedcircuits.DiskDrive.IDiskDrive;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.client.ItemLaserRenderer;
import vic.mod.integratedcircuits.client.Part7SegmentRenderer;
import vic.mod.integratedcircuits.client.PartCircuitRenderer;
import vic.mod.integratedcircuits.client.Resources;
import vic.mod.integratedcircuits.client.SemiTransparentRenderer;
import vic.mod.integratedcircuits.client.TileEntityAssemblerRenderer;
import vic.mod.integratedcircuits.client.TileEntityGateRenderer;
import vic.mod.integratedcircuits.client.TileEntityPCBLayoutRenderer;
import vic.mod.integratedcircuits.client.gui.Gui7Segment;
import vic.mod.integratedcircuits.gate.Part7Segment;
import vic.mod.integratedcircuits.misc.RenderUtils;
import vic.mod.integratedcircuits.tile.TileEntityAssembler;
import vic.mod.integratedcircuits.tile.TileEntityGate;
import vic.mod.integratedcircuits.tile.TileEntityPCBLayout;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	public static SemiTransparentRenderer stRenderer;
	public static Resources resources;
	
	public static int clientTicks;
	public static PartCircuitRenderer circuitRenderer;
	public static Part7SegmentRenderer segmentRenderer;
	
	@Override
	public void initialize() 
	{
		super.initialize();
		stRenderer = new SemiTransparentRenderer();
		TileEntityAssemblerRenderer.fboArray = new LinkedList<Framebuffer>();
		
		Constants.GATE_RENDER_ID = RenderingRegistry.getNextAvailableRenderId();
		
		TileEntityGateRenderer gateRenderer = new TileEntityGateRenderer();
		RenderingRegistry.registerBlockHandler(Constants.GATE_RENDER_ID, gateRenderer);
		
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPCBLayout.class, new TileEntityPCBLayoutRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAssembler.class, new TileEntityAssemblerRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGate.class, gateRenderer);
		
		MinecraftForgeClient.registerItemRenderer(IntegratedCircuits.itemLaser, new ItemLaserRenderer());
	}
	
	@Override
	public void preInitialize() 
	{
		super.preInitialize();
		
		circuitRenderer = new PartCircuitRenderer();
		segmentRenderer = new Part7SegmentRenderer();
		resources = new Resources();
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
			box = DiskDrive.getDiskDriveBoundingBox((IDiskDrive)tileEntity, x, y, z, event.target.hitVec);
		if(tileEntity instanceof TileEntityAssembler && box == null)
			box = getLaserBoundingBox((TileEntityAssembler)tileEntity, x, y, z, event.player, event.partialTicks).getLeft();
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
		try {
			for(Framebuffer buf : TileEntityAssemblerRenderer.fboArray)
				buf.deleteFramebuffer();
		} catch (RuntimeException e) {}
		TileEntityAssemblerRenderer.fboArray.clear();
	}
	
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			GuiScreen gui = Minecraft.getMinecraft().currentScreen;
			if(gui == null || !gui.doesGuiPauseGame()) clientTicks++;
		}
	}

	/** Needed because of reflection. */
	public static void open7SegmentGUI(Part7Segment part)
	{
		Minecraft.getMinecraft().displayGuiScreen(new Gui7Segment(part));
	}

	
	// Don't even look at what's coming now. Not related at all.

	private enum FancyThing { NONE, SHIRO, JIBRIL, STEPH, MAMI, NANO, CIRNO }
	private FancyThing getFancyThing(UUID uuid, String skinID)
	{
		String uuidStr = uuid.toString();
		// Is this someone who has deserved it?
		if (uuidStr.equals("b027a4f4-d480-426c-84a3-a9cb029f4b72") || // victorious3
			uuidStr.equals("6a7f2000-5853-4934-981d-5077be5a0b50") || // Thog
			uuidStr.equals("e2519b08-5d04-42a3-a98e-c70de4a0374e") || // RX14
			uuidStr.equals("eba64cb1-0d29-4434-8d5e-31004b00488c") || // riskyken
			uuidStr.equals("3239d8f3-dd0c-48d3-890e-d3dad403f758") || // skyem
			uuidStr.equals("771422e7-904c-4952-bb55-de9590f97739")) { // andrejsavikin
				// Work out what skin they have
				if (skinID.equals("skins/8fcd9586da356dfe3038fcad96925c43bea5b67a576c9b4e6b10f1b0bb7f1fc5")) // Shiro skin
					return FancyThing.SHIRO;
				else if (skinID.equals("skins/d45286a47c460daddedd3f02accf8b0a5b65a86dfcbffdb86e955b95e075aa")) // Jibril skin
					return FancyThing.JIBRIL;
				else if (skinID.equals("skins/7c53efc23da1887fe82b42921fcc714f76fb0e62fb032eae7039a7134e2110")) // Steph skin
					return FancyThing.STEPH;
				else if (skinID.equals("skins/3f98d0a766e1170d389ad283860329485e5be7668bdbfe45ff04c9ba5a8a2")) // Mami skin
					return FancyThing.MAMI;
				else if (skinID.equals("skins/23295447ce21e83e36da7360ee1fe34c15b9391fb564773c954e59c83ff6d1f9")) // Nano skin
					return FancyThing.JIBRIL;
				else if (skinID.equals("skins/b87e257050b59622aa2e65aeba9ea195698b625225566dd2682a77bec68398")) // Cirno skin
					return FancyThing.CIRNO;
		}
		// You do not get a fancy thing, sorry. :(
		return FancyThing.NONE;
	}
	private FancyThing getFancyThing(AbstractClientPlayer player)
	{
		return getFancyThing(player.getUniqueID(), player.getLocationSkin().getResourcePath());
	}

	@SubscribeEvent
	public void onPlayerRender(RenderPlayerEvent.Specials.Post event)
	{
		EntityPlayer player = event.entityPlayer;
		Minecraft mc = Minecraft.getMinecraft();

		// Get fancy thing of the player
		FancyThing fancyThing = FancyThing.NONE;
		if (player instanceof AbstractClientPlayer)
			fancyThing = getFancyThing((AbstractClientPlayer)player);
		if (fancyThing == FancyThing.NONE) return;
		
		boolean headArmour = player.inventory.armorItemInSlot(3) != null &&
				(fancyThing == FancyThing.SHIRO || fancyThing == FancyThing.STEPH || fancyThing == FancyThing.MAMI);
		
		//Test if AW is hiding the headgear
		if(IntegratedCircuits.isAWLoaded)
		{
			try {
				Object epRenderCache = Class.forName("riskyken.armourersWorkshop.client.handler.PlayerSkinHandler").getDeclaredField("INSTANCE").get(null);
				Field f = epRenderCache.getClass().getDeclaredField("skinMap");
				f.setAccessible(true);
				Map skinMap = (Map)f.get(epRenderCache);
				if(skinMap.containsKey(player.getPersistentID()))
				{
					Object skinInfo = skinMap.get(player.getPersistentID());
					Object nakedInfo = skinInfo.getClass().getMethod("getNakedInfo").invoke(skinInfo);
					BitSet armourOverride = (BitSet)nakedInfo.getClass().getDeclaredField("armourOverride").get(nakedInfo);
					if(armourOverride.get(0)) headArmour = false;
				}	
			} catch (Exception e) {}
		}

		if(headArmour) return;

		float yaw = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * event.partialRenderTick;
		float yawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * event.partialRenderTick;
		float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * event.partialRenderTick;
		float pitchZ = (float)Math.toDegrees(event.renderer.modelBipedMain.bipedHead.rotateAngleZ);
		
		GL11.glPushMatrix();
		
		GL11.glColor3f(1F, 1F, 1F);
		GL11.glRotatef(pitchZ, 0, 0, 1);
		GL11.glRotatef(yawOffset, 0, -1, 0);
		GL11.glRotatef(yaw - 270, 0, 1, 0);
		GL11.glRotatef(pitch, 0, 0, 1);
		
		GL11.glTranslated(0, (player.isSneaking() ? 0.0625 : 0), 0);
		Tessellator tes = Tessellator.instance;

		switch (fancyThing)
		{
			case JIBRIL:
				// Jibril
				GL11.glPushMatrix();
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				RenderUtils.setBrightness(240, 240);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GL11.glColor4f(1F, 1F, 1F, 1F);

				mc.renderEngine.bindTexture(Resources.RESOURCE_MISC_HALO);

				GL11.glRotated(30, 1, 0, -1);
				GL11.glTranslatef(-0.1F, -0.62F, -0.1F);
				GL11.glRotatef(player.ticksExisted + event.partialRenderTick, 0, 1, 0);

				tes.startDrawingQuads();
				tes.addVertexWithUV(-0.5, 0, -0.5, 0, 0);
				tes.addVertexWithUV(-0.5, 0, 0.5, 0, 1);
				tes.addVertexWithUV(0.5, 0, 0.5, 1, 1);
				tes.addVertexWithUV(0.5, 0, -0.5, 1, 0);
				tes.draw();

				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glShadeModel(GL11.GL_FLAT);
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glPopMatrix();
				RenderUtils.resetBrightness();
				break;
			case SHIRO:
				//Shiro Nai
				GL11.glPushMatrix();
				float scale = 1 / 64F;

				GL11.glTranslated(15 * scale, -0.78, 15 * scale);
				float f1 = (float)(7 * Math.sin(Math.toRadians(45)) + 7 / 2F) * scale;
				GL11.glTranslatef(-f1, 0, -f1);
				GL11.glRotated(-25, 1, 0, -1);
				GL11.glTranslatef(f1, 0, f1);

				GL11.glEnable(GL11.GL_CULL_FACE);
				ModelCrown.instance.render(scale);
				GL11.glDisable(GL11.GL_CULL_FACE);
				GL11.glPopMatrix();
				break;
			case STEPH:
				//Stephanie Dola
				mc.renderEngine.bindTexture(Resources.RESOURCE_MISC_EARS);
				ModelDogEars.instance.render(pitch, player.rotationYawHead - player.prevRotationYawHead);
				GameData.getBlockRegistry().getObject(player.getCommandSenderName());
				break;
			case MAMI:
				//Mami Tomoe
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				renderCurl();
				GL11.glScalef(1, 1, -1);
				renderCurl();
				GL11.glScalef(1, 1, -1);
				renderHat();
				GL11.glEnable(GL11.GL_TEXTURE_2D);

				mc.renderEngine.bindTexture(Resources.RESOURCE_MISC_FLOWER);
				GL11.glPushMatrix();
				GL11.glTranslatef(0, -9 / 16F, 0);
				GL11.glTranslatef(2 / 16F, 0, -3.3F / 16F);
				GL11.glRotatef(85, 1, 0, 0);
				GL11.glRotatef(30, 0, 0, 1);
				tes.startDrawingQuads();
				tes.addVertexWithUV(-2 / 16F, 0, -2 / 16F, 0, 0);
				tes.addVertexWithUV(-2 / 16F, 0, 2 / 16F, 0, 1);
				tes.addVertexWithUV(2 / 16F, 0, 2 / 16F, 1, 1);
				tes.addVertexWithUV(2 / 16F, 0, -2 / 16F, 1, 0);
				tes.draw();
				GL11.glPopMatrix();

				mc.renderEngine.bindTexture(Resources.RESOURCE_MISC_FLUFF);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glPushMatrix();
				GL11.glTranslatef(-1 / 16F, -8 / 16F, -5F / 16F);
				GL11.glRotatef(0, 1, 0, 0);
				tes.startDrawingQuads();
				tes.addVertexWithUV(0, -3 / 16F, -3 / 16F, 0, 0);
				tes.addVertexWithUV(0, 3 / 16F, -3 / 16F, 0, 1);
				tes.addVertexWithUV(0, 3 / 16F, 3 / 16F, 1, 1);
				tes.addVertexWithUV(0, -3 / 16F, 3 / 16F, 1, 0);
				tes.draw();
				GL11.glPopMatrix();
				GL11.glEnable(GL11.GL_LIGHTING);
				break;
		}
		GL11.glPopMatrix();
	}

	public static void renderCurl()
	{
		GL11.glPushMatrix();
		GL11.glRotatef(40, 1, 0, 0);
		GL11.glTranslatef(3 / 16F, 1.5F / 16F, 3.5F / 16F);
		
		Tessellator tes = Tessellator.instance;
		tes.startDrawing(GL11.GL_QUAD_STRIP);
		tes.setColorRGBA_I(0xF9DE85, 255);
		float x = 0, y = 0, z = 0, angle;
		float distance = 0.4F;
		
		GL11.glShadeModel(GL11.GL_SMOOTH);
		for(angle = 0.5F; angle <= (Math.PI * 2.16F * 2); angle += distance) 
		{
			float pos = 1 - (float)(angle / (Math.PI * 2.16F * 2)) * 0.7F;
			x = (float) Math.sin(angle) * 0.1F * pos;
			z = (float) Math.cos(angle) * 0.1F * pos;
			Vec3 normals = Vec3.createVectorHelper(x, 0, z).normalize();
			tes.setNormal((float)normals.xCoord, (float)normals.yCoord, (float)normals.zCoord);
			tes.addVertex(x - 0.025, y - 0.025, z - 0.025);
			tes.addVertex(x + 0.025, y + 0.025, z + 0.025);
			y += 0.01;
		}
		tes.draw();
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glPopMatrix();
	}
	
	public static void renderHat()
	{
		GL11.glPushMatrix();
		GL11.glTranslatef(0, -10 / 16F, 0);
		
		float radius = 4 / 16F, height = 2 / 16F;
		float res = 0.7F;
		Tessellator tes = Tessellator.instance;
		
		tes.startDrawing(GL11.GL_TRIANGLE_FAN);
		tes.setColorRGBA_I(0x57424F, 255);
		tes.setNormal(0, -1, 0);
		tes.addVertex(0, 0, 0);
		for(float i = 0; i <= 2 * Math.PI; i += res)
			tes.addVertex(radius * Math.cos(i), 0, radius * Math.sin(i));
		tes.addVertex(radius, 0, 0);
		tes.draw();
		
		Vec3 center = Vec3.createVectorHelper(-radius, -height / 2, -radius);
		tes.startDrawing(GL11.GL_QUAD_STRIP);
		tes.setColorRGBA_I(0x57424F, 255);
		for(float i = 0; i <= 2 * Math.PI; i += res)
		{
			float x = (float)(radius * Math.cos(i));
			float z = (float)(radius * Math.sin(i));
			Vec3 v1 = Vec3.createVectorHelper(x, 0, z).subtract(center).normalize();
			tes.setNormal((float)v1.xCoord, (float)v1.yCoord, (float)v1.zCoord);
			tes.addVertex(x, 0, z);
			Vec3 v2 = Vec3.createVectorHelper(x, height, z).subtract(center).normalize();
			tes.setNormal((float)v2.xCoord, (float)v2.yCoord, (float)v2.zCoord);
			tes.addVertex(x, height, z);
		}
		tes.addVertex(radius, 0, 0);
		tes.addVertex(radius, height, 0);
		tes.draw();
		
		GL11.glPopMatrix();
	}
	
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
			Minecraft.getMinecraft().renderEngine.bindTexture(Resources.RESOURCE_MISC_CROWN);
			crown1.render(scale);
			crown2.render(scale);
		}
	}
	
	public static class ModelDogEars extends ModelBase
	{
		public static ModelDogEars instance = new ModelDogEars();
		
		public ModelRenderer ear;
		
		public ModelDogEars()
		{
			this.textureWidth = 16;
			this.textureHeight = 16;
			ear = new ModelRenderer(this);
			ear.addBox(0, 0, 0, 3, 9, 1);
		}
		
		public void render(float pitch, float off)
		{
			GL11.glPushMatrix();
			GL11.glTranslatef(0, -6 / 16F, -5 / 16F);
			GL11.glTranslatef(1.5F / 16F, 0, 0.5F / 16F);
			GL11.glRotatef(-pitch, 0, 0, 1);
			GL11.glRotatef(-5, 1, 0, 0);
			if(off < 0) GL11.glRotatef(off, 1, 0, 0);
			GL11.glTranslatef(-1.5F / 16F, 0, -0.5F / 16F);
			ear.render(1 / 16F);
			GL11.glPopMatrix();
			
			GL11.glPushMatrix();
			GL11.glTranslatef(0, -6 / 16F, 4 / 16F);
			GL11.glTranslatef(1.5F / 16F, 0, 0.5F / 16F);
			GL11.glRotatef(-pitch, 0, 0, 1);
			GL11.glRotatef(5, 1, 0, 0);
			if(off > 0) GL11.glRotatef(off, 1, 0, 0);
			GL11.glTranslatef(-1.5F / 16F, 0, -0.5F / 16F);
			ear.render(1 / 16F);
			GL11.glPopMatrix();
		}
	}
}
