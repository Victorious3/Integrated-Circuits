package vic.mod.integratedcircuits;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.mojang.realmsclient.gui.ChatFormatting;

public class ItemFloppyDisk extends Item
{
	public ItemFloppyDisk()
	{
		setCreativeTab(CreativeTabs.tabMisc);
		setUnlocalizedName(IntegratedCircuits.modID + ".floppy");
		setTextureName(IntegratedCircuits.modID + ":floppy");
		setMaxStackSize(1);
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) 
	{
		NBTTagCompound comp = stack.getTagCompound();
		if(comp == null || !comp.hasKey("name")) itemInformation.add(ChatFormatting.GRAY + "" + ChatFormatting.ITALIC + "empty");
		else
		{
			itemInformation.add("Name: " + ChatFormatting.ITALIC + comp.getString("name"));
			itemInformation.add("Size: " + ChatFormatting.ITALIC + comp.getString("size"));
		}
	}
}
