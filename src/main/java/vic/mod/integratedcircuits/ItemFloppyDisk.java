package vic.mod.integratedcircuits;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.mojang.realmsclient.gui.ChatFormatting;

public class ItemFloppyDisk extends Item
{
	public ItemFloppyDisk()
	{
		setCreativeTab(IntegratedCircuits.creativeTab);
		setUnlocalizedName(IntegratedCircuits.modID + ".floppy");
		setTextureName(IntegratedCircuits.modID + ":floppy");
		setMaxStackSize(1);
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) 
	{
		NBTTagCompound comp = stack.getTagCompound();
		if(comp != null && comp.hasKey("circuit"))
		{
			comp = comp.getCompoundTag("circuit");
			Integer size = comp.getInteger("size");
			itemInformation.add(ChatFormatting.GRAY + "Name: " + ChatFormatting.WHITE + comp.getCompoundTag("properties").getString("name"));
			itemInformation.add(ChatFormatting.GRAY + "Size: " + ChatFormatting.WHITE + size + "x" + size);
			itemInformation.add(ChatFormatting.GRAY + "Author: " + ChatFormatting.WHITE + comp.getCompoundTag("properties").getString("author"));
		}
	}
}
