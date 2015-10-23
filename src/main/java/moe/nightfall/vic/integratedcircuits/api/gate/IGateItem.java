package moe.nightfall.vic.integratedcircuits.api.gate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import codechicken.lib.vec.BlockCoord;

public interface IGateItem {
	String getGateID(ItemStack stack, EntityPlayer player, BlockCoord pos);
	
	/**
	 * Called when a gate (circuit) is placed onto the socket.
	 * This is to allow a gate, if it wants, to allow a player to place it, without using it up.
	 * Note that the creative mode check should not happen here.
	 * 
	 * This is also used to determine if the gate should be dropped when removed.
	 *
	 * @param player The player who is placing the gate
	 * @return If the gate is to be used up (removed from inventory)
	 */
	Boolean usedUpOnPlace(EntityPlayer player);
}
