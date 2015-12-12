package moe.nightfall.vic.integratedcircuits.client;

import java.util.LinkedList;

import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import moe.nightfall.vic.integratedcircuits.proxy.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.opengl.GL11;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
/** Used by the assembler to get a second render pass **/
public class SemiTransparentRenderer {
	private LinkedList<Vec3> queued = new LinkedList<Vec3>();

	public SemiTransparentRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onWorldPostRender(RenderWorldLastEvent event) {
		GL11.glPushMatrix();
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);

		// First draw call, without the depth buffer.
		GL11.glDepthMask(false);
		dispatchRender(event.partialTicks, 1, event.context);
		GL11.glDepthMask(true);

		// Second draw call, only to the depth buffer.
		GL11.glColorMask(false, false, false, false);
		dispatchRender(event.partialTicks, 2, event.context);
		GL11.glColorMask(true, true, true, true);

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glShadeModel(GL11.GL_FLAT);

		GL11.glPopMatrix();

		queued.clear();

		((ClientProxy) IntegratedCircuits.proxy).renderPlayer(event.partialTicks, event.context);
	}

	private void dispatchRender(float partialTicks, int pass, RenderGlobal context) {
		for (Vec3 pos : queued) {
			TileEntityRendererDispatcher dispatcher = TileEntityRendererDispatcher.instance;
			TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity((int) pos.xCoord, (int) pos.yCoord,
					(int) pos.zCoord);
			if (te == null)
				continue;
			TileEntitySpecialRenderer renderer = dispatcher.getSpecialRenderer(te);
			if (!(renderer instanceof TileEntitySemiTransparentRenderer))
				throw new IllegalArgumentException();
			TileEntitySemiTransparentRenderer stRenderer = (TileEntitySemiTransparentRenderer) renderer;
			stRenderer.renderPass = pass;
			dispatcher.renderTileEntity(te, partialTicks);
			if (pass == 2)
				stRenderer.renderPass = 0;
		}
	}

	public void addToRenderQueue(int x, int y, int z) {
		queued.add(Vec3.createVectorHelper(x, y, z));
	}
}
