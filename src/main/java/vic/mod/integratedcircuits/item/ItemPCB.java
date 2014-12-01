package vic.mod.integratedcircuits.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import vic.mod.integratedcircuits.IntegratedCircuits;

import com.mojang.realmsclient.gui.ChatFormatting;

public class ItemPCB extends ItemBase
{
	private static IIcon icon;
	private static IIcon icon_raw;
	
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
