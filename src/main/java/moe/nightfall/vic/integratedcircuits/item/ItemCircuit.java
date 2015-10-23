package moe.nightfall.vic.integratedcircuits.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import codechicken.lib.vec.BlockCoord;

public class ItemCircuit extends ItemGate {
	public ItemCircuit() {
		super("circuit");
		setMaxStackSize(1);
		setCreativeTab(null);
		setHasIcon(false);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (stack.getTagCompound() == null)
			return StatCollector.translateToLocalFormatted(getUnlocalizedName() + ".name", "INVALID!");
		String name = stack.getTagCompound().getCompoundTag("circuit").getCompoundTag("properties").getString("name");
		return StatCollector.translateToLocalFormatted(getUnlocalizedName() + ".name", name);
	}

	@Override
	public String getGateID(ItemStack stack, EntityPlayer player, BlockCoord pos) {
		return "circuit";
	}
	
	@Override
	public Boolean usedUpOnPlace(EntityPlayer player) {
		return true;
	}
}
