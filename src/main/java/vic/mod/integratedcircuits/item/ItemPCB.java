package vic.mod.integratedcircuits.item;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import vic.mod.integratedcircuits.client.Resources;

import com.mojang.realmsclient.gui.ChatFormatting;

public class ItemPCB extends ItemBase
{
	public ItemPCB()
	{
		super("pcb");
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
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) 
	{
		if(stack.getItemDamage() == 0)
			return I18n.format(getUnlocalizedName() + ".name");
		else return I18n.format(getUnlocalizedName() + ".printed.name");
	}

	@Override
	public IIcon getIconFromDamage(int damage) 
	{
		if(damage == 0) return Resources.ICON_PCB_RAW;
		return Resources.ICON_PCB;
	}
}
