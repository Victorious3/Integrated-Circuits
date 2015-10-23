package moe.nightfall.vic.integratedcircuits.item;

import java.util.List;

import moe.nightfall.vic.integratedcircuits.api.IDyeable;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import codechicken.lib.vec.BlockCoord;

public class Item7Segment extends ItemGate implements IDyeable {
	public Item7Segment() {
		super("7segment");
		setHasIcon(false);
	}

	@Override
	public boolean canDye(int color, ItemStack stack) {
		return true;
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for (int i = 0; i < 16; i++)
			list.add(new ItemStack(this, 1, i));
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return MiscUtils.getLocalizedColor(stack.getItemDamage()) + " "
				+ StatCollector.translateToLocal(stack.getUnlocalizedName() + ".name");
	}

	@Override
	public String getGateID(ItemStack stack, EntityPlayer player, BlockCoord pos) {
		return "7segment";
	}
	
	@Override
	public Boolean usedUpOnPlace(EntityPlayer player) {
		return true;
	}
}
