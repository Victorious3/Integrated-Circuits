package moe.nightfall.vic.integratedcircuits.client;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import moe.nightfall.vic.integratedcircuits.Content;
import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer.CircuitRenderWrapper;
import moe.nightfall.vic.integratedcircuits.proxy.ClientProxy;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.RenderItemFrame;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.common.MinecraftForge;

public class ItemPCBPrintRenderer implements IItemRenderer {

	public ItemPCBPrintRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	// Used to cache the current stack so that it doesn't have to reload the
	// whole circuit data on every frame
	private ItemStack heldStack;
	private CircuitRenderWrapper heldCData;

	private Map<ItemStack, TextureRenderer.Entry> textures = Maps.newHashMap();
	private List<TextureRenderer.Entry> rendered = Lists.newLinkedList();

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		if (type == ItemRenderType.FIRST_PERSON_MAP) {
			return true;
		}
		return false;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {

		if (type == ItemRenderType.FIRST_PERSON_MAP) {
			if (!(heldStack == item)) {
				heldCData = new CircuitRenderWrapper(CircuitData.readFromNBT(item.stackTagCompound));
				heldStack = item;
			}

			GL11.glPushMatrix();
			float scale = 8 * (16F / heldCData.getCircuitData().getSize());
			GL11.glScalef(scale, scale, 1);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			CircuitPartRenderer.renderPerfboard(heldCData.getCircuitData());
			CircuitPartRenderer.renderParts(heldCData);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glPopMatrix();
		}
	}

	Method f_renderItemFrame = ReflectionHelper.findMethod(RenderItemFrame.class, null,
			new String[] { "func_147915_b" }, new Class[] { EntityItemFrame.class });

	@SubscribeEvent
	public void onRenderTick(RenderTickEvent event) {
		if (event.phase == Phase.END) {
			// Clean up textures
			Iterator<Entry<ItemStack, TextureRenderer.Entry>> iterator = textures.entrySet().iterator();

			while (iterator.hasNext()) {
				Entry<ItemStack, TextureRenderer.Entry> entry = iterator.next();
				if (!rendered.contains(entry.getValue())) {
					ClientProxy.textureRenderer.delete(entry.getValue());
					iterator.remove();
				}
			}
		}
	}

	@SubscribeEvent
	public void onRenderInItemFrame(final RenderItemInFrameEvent event) throws Exception {
		if (event.item.getItem() == Content.itemPCBPrint) {
			if (!textures.containsKey(event.item)) {
				TextureRenderer.Entry entry = new TextureRenderer.Entry() {
					@Override
					public void render(float partial) {
						CircuitRenderWrapper crw = new CircuitRenderWrapper(CircuitData.readFromNBT(event.item.stackTagCompound));
						float scale = 16 * (16F / crw.getCircuitData().getSize());
						GL11.glScalef(scale, scale, 1);
						GL11.glDisable(GL11.GL_DEPTH_TEST);
						CircuitPartRenderer.renderPerfboard(crw.getCircuitData());
						CircuitPartRenderer.renderParts(crw);
						GL11.glEnable(GL11.GL_DEPTH_TEST);
					}
				};
				textures.put(event.item, entry);
				ClientProxy.textureRenderer.schedule(entry);
				// We haven't created the texture yet so there isn't much to
				// render
				return;
			}

			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(event.item).textureID());
			GL11.glPushMatrix();

			// Undo the translation...
			switch (event.entityItemFrame.getRotation()) {
			case 1:
				GL11.glTranslatef(-0.52F, -0.34F, 0.016F);
				break;
			case 2:
				GL11.glTranslatef(-0.5F, -0.36F, 0.016F);
				break;
			case 3:
				GL11.glTranslatef(-0.48F, -0.34F, 0.016F);
				break;
			default:
				GL11.glTranslatef(-0.5F, -0.32F, 0.016F);
			}

			GL11.glDisable(GL11.GL_LIGHTING);
			Tessellator tes = Tessellator.instance;
			tes.startDrawingQuads();
			tes.addVertexWithUV(1, 1, 0, 1, 1);
			tes.addVertexWithUV(0, 1, 0, 0, 1);
			tes.addVertexWithUV(0, 0, 0, 0, 0);
			tes.addVertexWithUV(1, 0, 0, 1, 0);
			tes.draw();
			GL11.glEnable(GL11.GL_LIGHTING);

			GL11.glPopMatrix();

			GL11.glRotatef(-90 * event.entityItemFrame.getRotation(), 0.0F, 0.0F, -1.0F);

			// Undo the translation AGAIN This time with different handpicked
			// values...
			switch (event.entityItemFrame.getRotation()) {
			case 1:
				GL11.glTranslatef(0.16F, 0.02F, -0.4842F);
				break;
			case 2:
				GL11.glTranslatef(0, -0.14F, -0.4842F);
				break;
			case 3:
				GL11.glTranslatef(-0.16F, 0.02F, -0.4842F);
				break;
			default:
				GL11.glTranslatef(0, 0.18F, -0.4842F);
			}

			GL11.glRotatef(event.entityItemFrame.rotationYaw, 0.0F, -1.0F, 0.0F);
			f_renderItemFrame.invoke(event.renderer, event.entityItemFrame);
			event.setCanceled(true);
		}
	}
}
