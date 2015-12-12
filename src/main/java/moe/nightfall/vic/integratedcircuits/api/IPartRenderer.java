package moe.nightfall.vic.integratedcircuits.api;

import java.util.List;

import moe.nightfall.vic.integratedcircuits.client.model.IComponentModel;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import codechicken.lib.vec.Transformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IPartRenderer<T> extends IItemRenderer {
	public List<IComponentModel> getModels();

	public void prepare(T part);

	public void prepareInv(ItemStack stack);

	public void prepareDynamic(T part, float partialTicks);

	public void renderStatic(Transformation t);

	public void renderDynamic(Transformation t);
}
