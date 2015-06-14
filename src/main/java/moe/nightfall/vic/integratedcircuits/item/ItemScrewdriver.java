package moe.nightfall.vic.integratedcircuits.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.common.Optional.Interface;

@InterfaceList({
	@Interface(iface = "mrtjp.projectred.api.IScrewdriver", modid = "ProjRed|Core"),
	@Interface(iface = "com.bluepowermod.api.misc.IScrewdriver", modid = "bluepowerAPI")
})
public class ItemScrewdriver extends ItemBase implements mrtjp.projectred.api.IScrewdriver, com.bluepowermod.api.misc.IScrewdriver {
	public ItemScrewdriver() {
		super("screwdriver");
		setMaxStackSize(1);
		setMaxDamage(128);
		setNoRepair();
	}

	@Override
	public void damageScrewdriver(World world, EntityPlayer player) {
		player.getHeldItem().damageItem(1, player);
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
		return true;
	}

	@Override
	public boolean damage(ItemStack stack, int damage, EntityPlayer player, boolean simulated) {
		player.getHeldItem().damageItem(1, player);
		return true;
	}
}
