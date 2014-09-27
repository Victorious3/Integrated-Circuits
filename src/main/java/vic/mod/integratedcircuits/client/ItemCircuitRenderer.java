package vic.mod.integratedcircuits.client;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import vic.mod.integratedcircuits.proxy.ClientProxy;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Translation;

public class ItemCircuitRenderer implements IItemRenderer
{
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
		default:
			break;
		}
	}
	
	private void renderPart(ItemStack stack, float x, float y, float z, float scale)
	{
		TextureUtils.bindAtlas(0);
		CCRenderState.reset();
		CCRenderState.setDynamic();
		CCRenderState.pullLightmap();
		ClientProxy.renderer.prepareInv();
		ClientProxy.renderer.prepareInv(stack);
		CCRenderState.startDrawing();
		ClientProxy.renderer.renderStatic(new Scale(scale).with(new Translation(x, y, z)), 0);
		CCRenderState.draw();
		ClientProxy.renderer.renderDynamic(new Scale(scale).with(new Translation(x, y, z)));
	}
}
