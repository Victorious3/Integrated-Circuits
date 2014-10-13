package vic.mod.integratedcircuits;

import mrtjp.projectred.ProjectRedIntegration;
import net.minecraft.item.Item;

public class ItemLaser extends Item
{
	public ItemLaser()
	{
		setCreativeTab(ProjectRedIntegration.tabIntegration());
		setUnlocalizedName(IntegratedCircuits.modID + ".laser");
		setMaxStackSize(1);
	}
}
