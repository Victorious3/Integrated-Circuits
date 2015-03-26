package vic.mod.integratedcircuits.client;

import net.minecraft.item.ItemStack;
import codechicken.lib.vec.Transformation;

public interface IPartRenderer <T>
{
	public void prepare(T part);
	
	public void prepareInv(ItemStack stack);
	
	public void prepareDynamic(T part, float partialTicks);
	
	public void renderStatic(Transformation t, int orient);
	
	public void renderDynamic(Transformation t);
}
