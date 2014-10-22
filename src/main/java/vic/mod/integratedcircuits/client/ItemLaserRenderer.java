package vic.mod.integratedcircuits.client;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

public class ItemLaserRenderer implements IItemRenderer
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
		GL11.glPushMatrix();
		if(type == ItemRenderType.INVENTORY)
			GL11.glRotatef(90, 0, 1, 0);
		
		if(type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON)
			GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		else if(type == ItemRenderType.ENTITY || type == ItemRenderType.INVENTORY)
			GL11.glTranslatef(-0.5F, 0F, 0F);
		
		if(type == ItemRenderType.EQUIPPED) {
			GL11.glRotatef(160, 0, 0, 1);
			GL11.glRotatef(30, 1, 1, 0);
			GL11.glTranslatef(-0.2F, 0.1F, 0.4F);
		}	
		
		if(type == ItemRenderType.INVENTORY)
			GL11.glScalef(4.5F, 4.5F, 4.5F);
		else GL11.glScalef(4F, 4F, 4F);
		
		ModelLaser.instance.render(1 / 64F, 0, 0, false, 0, 1, null);
		GL11.glPopMatrix();
	}
}
