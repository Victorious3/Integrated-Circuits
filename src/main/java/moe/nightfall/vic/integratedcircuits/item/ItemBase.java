package moe.nightfall.vic.integratedcircuits.item;

import net.minecraftforge.fml.common.registry.GameRegistry;
import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;

public class ItemBase extends Item {
	private boolean hasIcon = true;

	public ItemBase(String name) {
		register(this, name);
	}

	static void register(Item item, String name) {
		item.setCreativeTab(IntegratedCircuits.creativeTab);
		item.setUnlocalizedName(Constants.MOD_ID + "." + name);
		item.setTextureName(Constants.MOD_ID + ":" + name);
		GameRegistry.registerItem(item, Constants.MOD_ID + "_" + name, Constants.MOD_ID);
	}

	public ItemBase setHasIcon(boolean hasIcon) {
		this.hasIcon = hasIcon;
		return this;
	}

	@Override
	public void registerIcons(IIconRegister ir) {
		if (hasIcon)
			super.registerIcons(ir);
	}
}
