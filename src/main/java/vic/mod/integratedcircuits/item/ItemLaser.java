package vic.mod.integratedcircuits.item;

import vic.mod.integratedcircuits.IntegratedCircuits;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;

public class ItemLaser extends Item
{
	public ItemLaser()
	{
		setCreativeTab(IntegratedCircuits.creativeTab);
		setUnlocalizedName(IntegratedCircuits.modID + ".laser");
	}

	@Override 
	public void registerIcons(IIconRegister p_94581_1_) {}
}
