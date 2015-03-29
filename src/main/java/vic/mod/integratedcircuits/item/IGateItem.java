package vic.mod.integratedcircuits.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import codechicken.lib.vec.BlockCoord;

public interface IGateItem
{
	public String getGateID(ItemStack stack, EntityPlayer player, BlockCoord pos);
}
