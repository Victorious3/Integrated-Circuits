package moe.nightfall.vic.integratedcircuits.client;

import java.util.List;

import moe.nightfall.vic.integratedcircuits.api.IPartRenderer;
import moe.nightfall.vic.integratedcircuits.client.model.IComponentModel;
import net.minecraft.item.ItemStack;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Translation;

import com.google.common.collect.Lists;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class PartRenderer<T> implements IPartRenderer<T> {
	protected List<IComponentModel> models = Lists.newLinkedList();

	@Override
	public void prepare(T part) {
	}

	@Override
	public void prepareInv(ItemStack stack) {
	}

	@Override
	public void prepareDynamic(T part, float partialTicks) {
	}

	@Override
	public void renderStatic(Transformation t) {
		for (IComponentModel m : models)
			m.renderModel(t);
	}

	@Override
	public void renderDynamic(Transformation t) {
	}

	@Override
	public List<IComponentModel> getModels() {
		return models;
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		prepareInv(item);
		switch (type) {
			case ENTITY:
				renderPart(item, -0.3F, 0F, -0.3F, 0.6F);
				break;
			case EQUIPPED:
				renderPart(item, 0.0F, 0.15F, 0.0F, 1.0F);
				break;
			case EQUIPPED_FIRST_PERSON:
				renderPart(item, 1.0F, -0.2F, -0.4f, 2.0F);
				break;
			case INVENTORY:
				renderPart(item, 0.0F, 0.2F, 0.0F, 1.0F);
				break;
			default:
				break;
		}
	}

	protected void renderPart(ItemStack stack, float x, float y, float z, float scale) {
		TextureUtils.bindAtlas(0);
		CCRenderState.reset();
		CCRenderState.setDynamic();
		CCRenderState.pullLightmap();
		prepareInv(stack);
		CCRenderState.startDrawing();
		renderStatic(new Scale(scale).with(new Translation(x, y, z)));
		CCRenderState.draw();
		renderDynamic(new Scale(scale).with(new Translation(x, y, z)));
	}
}
