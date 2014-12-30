package vic.mod.integratedcircuits.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import vic.mod.integratedcircuits.IntegratedCircuits;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemBase extends Item
{
	private boolean hasIcon = true;

	public ItemBase(String name)
	{
		setCreativeTab(IntegratedCircuits.creativeTab);
		setUnlocalizedName(IntegratedCircuits.modID + "." + name);
		setTextureName(IntegratedCircuits.modID + ":" + name);
		GameRegistry.registerItem(this, IntegratedCircuits.modID + "_" + name, IntegratedCircuits.modID);
	}
	
	public ItemBase setHasIcon(boolean hasIcon)
	{
		this.hasIcon = hasIcon;
		return this;
	}

	@Override
	public void registerIcons(IIconRegister ir) 
	{
		if(hasIcon) super.registerIcons(ir);
	}
}
