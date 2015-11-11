package moe.nightfall.vic.integratedcircuits.client;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

public class TextureRenderer {

	private Set<Entry> schedule = Sets.newLinkedHashSet();
	private List<Integer> textureList = Lists.newLinkedList();
	private Framebuffer fbo;

	public static abstract class Entry {
		private int texture = -1;

		public final int textureID() {
			return texture;
		}

		public abstract void render(float partial);
	}

	public TextureRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
	}

	@SubscribeEvent
	public void onRenderTick(RenderTickEvent event) {
		if (event.phase == Phase.START && schedule.size() > 0) {
			int currentFBO = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
			if (fbo == null)
				fbo = new Framebuffer(256, 256, false);
			fbo.unbindFramebuffer();

			fbo.bindFramebuffer(false);
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glPushMatrix();
			GL11.glLoadIdentity();
			GL11.glViewport(0, 0, 256, 256);
			GL11.glOrtho(0, 256, 256, 0, -1, 1);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPushMatrix();
			GL11.glLoadIdentity();

			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			for (Entry entry : schedule)
				updateFramebuffer(entry, event.renderTickTime);
			schedule.clear();

			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glPopMatrix();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPopMatrix();

			fbo.unbindFramebuffer();
			OpenGlHelper.func_153171_g(OpenGlHelper.field_153198_e, currentFBO);
		}
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		try {
			for (Integer texture : textureList)
				TextureUtil.deleteTexture(texture);
		} catch (Exception e) {
		}
		textureList.clear();
	}

	public void schedule(Entry entry) {
		schedule.add(entry);
	}

	public void delete(Entry entry) {
		TextureUtil.deleteTexture(entry.texture);
	}

	private void updateFramebuffer(Entry entry, float partial) {
		if (entry.texture == -1) {
			entry.texture = fbo.framebufferTexture = TextureUtil.glGenTextures();
			textureList.add(entry.texture);
			fbo.setFramebufferFilter(GL11.GL_NEAREST);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.framebufferTexture);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, fbo.framebufferTextureWidth,
					fbo.framebufferTextureHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
			OpenGlHelper.func_153171_g(OpenGlHelper.field_153198_e, fbo.framebufferObject);
		}

		fbo.framebufferTexture = entry.texture;
		OpenGlHelper.func_153188_a(OpenGlHelper.field_153198_e, OpenGlHelper.field_153200_g, GL11.GL_TEXTURE_2D,
				fbo.framebufferTexture, 0);

		GL11.glColor3f(1, 1, 1);
		entry.render(partial);
	}
}
