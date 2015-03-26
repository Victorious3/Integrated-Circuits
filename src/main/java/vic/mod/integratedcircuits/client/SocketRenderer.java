package vic.mod.integratedcircuits.client;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import vic.mod.integratedcircuits.client.model.IComponentModel;
import vic.mod.integratedcircuits.client.model.ModelBase;
import vic.mod.integratedcircuits.gate.GateProvider;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Translation;

import com.google.common.collect.Lists;

public abstract class SocketRenderer<T extends GateProvider> implements IItemRenderer, IPartRenderer<T>
{	
	protected List<IComponentModel> models = Lists.newLinkedList();
	
	public SocketRenderer(IIcon icon)
	{
		models.add(new ModelBase(icon));
	}

	public void prepare(T part) {}
	
	public void prepareInv(ItemStack stack) {}
	
	public void prepareDynamic(T part, float partialTicks) {}
	
	public void renderStatic(Transformation t, int orient)
	{
		for(IComponentModel m : models) m.renderModel(t, orient);
	}
	
	public void renderDynamic(Transformation t) {}
	
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) 
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) 
	{
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) 
	{
		switch (type) {
		case ENTITY : renderPart(item, -0.3F, 0F, -0.3F, 0.6F); break;
		case EQUIPPED : renderPart(item, 0.0F, 0.15F, 0.0F, 1.0F); break;
		case EQUIPPED_FIRST_PERSON : renderPart(item, 1.0F, -0.2F, -0.4f, 2.0F); break;
		case INVENTORY : renderPart(item, 0.0F, 0.2F, 0.0F, 1.0F); break;
		default: break;
		}
	}
	
	private void renderPart(ItemStack stack, float x, float y, float z, float scale)
	{
		TextureUtils.bindAtlas(0);
		CCRenderState.reset();
		CCRenderState.setDynamic();
		CCRenderState.pullLightmap();
		prepareInv(stack);
		CCRenderState.startDrawing();
		renderStatic(new Scale(scale).with(new Translation(x, y, z)), 0);
		CCRenderState.draw();
		renderDynamic(new Scale(scale).with(new Translation(x, y, z)));
	}
}
