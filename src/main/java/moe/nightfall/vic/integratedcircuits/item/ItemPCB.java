package moe.nightfall.vic.integratedcircuits.item;

import java.util.List;

import moe.nightfall.vic.integratedcircuits.client.Resources;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

public class ItemPCB extends ItemBase {
	public ItemPCB() {
		super("pcb");
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
		NBTTagCompound comp = stack.getTagCompound();
		if (comp != null && comp.hasKey("circuit")) {
			comp = comp.getCompoundTag("circuit");
			ItemFloppyDisk.addInformation(comp, itemInformation, false);
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (stack.getItemDamage() == 0)
			return StatCollector.translateToLocal((getUnlocalizedName() + ".name"));
		else
			return StatCollector.translateToLocal((getUnlocalizedName() + ".printed.name"));
	}

	@Override
	public IIcon getIconFromDamage(int damage) {
		if (damage == 0)
			return Resources.ICON_PCB_RAW;
		return Resources.ICON_PCB;
	}
}
