package vic.mod.integratedcircuits;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import com.mojang.realmsclient.gui.ChatFormatting;

public class ItemPCB extends Item
{
	private static IIcon icon;
	private static IIcon icon_raw;
	
	public ItemPCB()
	{
		setUnlocalizedName(IntegratedCircuits.modID + ".pcb");
		setCreativeTab(IntegratedCircuits.creativeTab);
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) 
	{
		NBTTagCompound comp = stack.getTagCompound();
		if(comp != null && comp.hasKey("name"))
		{
			Integer size = comp.getInteger("size");
			itemInformation.add(ChatFormatting.GRAY + "Name: " + ChatFormatting.WHITE + comp.getString("name"));
			itemInformation.add(ChatFormatting.GRAY + "Size: " + ChatFormatting.WHITE + size + "x" + size);
		}
	}
	
	@Override
	public void registerIcons(IIconRegister ir) 
	{
		icon = ir.registerIcon(IntegratedCircuits.modID + ":pcb");
		icon_raw = ir.registerIcon(IntegratedCircuits.modID + ":pcb_raw");
	}

	@Override
	public IIcon getIconFromDamage(int damage) 
	{
		if(damage == 0) return icon_raw;
		return icon;
	}
}
