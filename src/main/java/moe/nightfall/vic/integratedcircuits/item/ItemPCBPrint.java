package moe.nightfall.vic.integratedcircuits.item;

import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import moe.nightfall.vic.integratedcircuits.Content;
import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public class ItemPCBPrint extends ItemMap {

	public static ItemStack create(CircuitData cdata) {
		ItemStack stack = new ItemStack(Content.itemPCBPrint);
		stack.stackTagCompound = cdata.writeToNBT(new NBTTagCompound());
		return stack;
	}

	public ItemPCBPrint() {
		ItemBase.register(this, "pcb_print");
		this.setCreativeTab(null);
		this.setMaxStackSize(1);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int meta, float hitX, float hitY, float hitZ) {
		return true;
	}

	@Override
	public MapData getMapData(ItemStack stack, World world) {
		return null;
	}

	@Override
	public void updateMapData(World world, Entity entity, MapData data) {

	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int meta, boolean par5) {

	}

	@Override
	public Packet func_150911_c(ItemStack stack, World world, EntityPlayer player) {
		return null;
	}

	@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer player) {

	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par5) {
		NBTTagCompound comp = stack.getTagCompound();
		if (comp != null) {
			ItemFloppyDisk.addInformation(comp, itemInformation, false);
		} else {
			itemInformation.add(ChatFormatting.RED + "Kill me...");
		}
	}
}
