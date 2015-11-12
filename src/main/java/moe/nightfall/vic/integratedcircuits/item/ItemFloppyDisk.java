package moe.nightfall.vic.integratedcircuits.item;

import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import moe.nightfall.vic.integratedcircuits.Config;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;

public class ItemFloppyDisk extends ItemBase {
	public ItemFloppyDisk() {
		super("floppy");
		setMaxStackSize(1);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
		NBTTagCompound comp = stack.getTagCompound();
		if (comp != null && comp.hasKey("circuit")) {
			comp = comp.getCompoundTag("circuit");
			addInformation(comp, itemInformation, true);
		} else if (Config.enableTooltips) {
			itemInformation.addAll(MiscUtils.appendToAll(ChatFormatting.GRAY + "" + ChatFormatting.ITALIC,
					MiscUtils.splitTranslateToLocalFormatted("circuit.tooltip.info")));
		}
	}

	public static void addInformation(NBTTagCompound comp, List itemInformation, boolean author) {
		int size = comp.getInteger("size");
		itemInformation.add(ChatFormatting.GRAY + StatCollector.translateToLocalFormatted("circuit.tooltip.name",
				ChatFormatting.WHITE + comp.getCompoundTag("properties").getString("name")));
		itemInformation.add(ChatFormatting.GRAY + StatCollector.translateToLocalFormatted("circuit.tooltip.size",
				"" + ChatFormatting.WHITE + size + "x" + size));
		if (author)
			itemInformation.add(ChatFormatting.GRAY + StatCollector.translateToLocalFormatted("circuit.tooltip.author",
					ChatFormatting.WHITE + comp.getCompoundTag("properties").getString("author")));
	}
}
