package moe.nightfall.vic.integratedcircuits.item;

import mrtjp.projectred.api.IScrewdriver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.Optional.Interface;

@Interface(iface = "mrtjp.projectred.api.IScrewdriver", modid = "ProjRed|Core")
public class ItemScrewdriver extends ItemBase implements IScrewdriver {
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
}
