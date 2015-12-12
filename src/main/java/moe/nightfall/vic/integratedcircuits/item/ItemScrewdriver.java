package moe.nightfall.vic.integratedcircuits.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.common.util.ForgeDirection;

@InterfaceList({
	@Interface(iface = "mrtjp.projectred.api.IScrewdriver", modid = "ProjRed|Core"),
	@Interface(iface = "com.bluepowermod.api.misc.IScrewdriver", modid = "bluepowerAPI"),
	@Interface(iface = "buildcraft.api.tools.IToolWrench", modid = "BuildCraftAPI|tools")
})
public class ItemScrewdriver extends ItemBase implements mrtjp.projectred.api.IScrewdriver, 
                                                         com.bluepowermod.api.misc.IScrewdriver,
                                                         buildcraft.api.tools.IToolWrench {
	public ItemScrewdriver() {
		super("screwdriver");
		setMaxStackSize(1);
		setMaxDamage(128);
		setNoRepair();
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		Block blockClicked = world.getBlock(x, y, z);

		boolean rotate = !player.isSneaking();

		if (rotate) {
			return blockClicked.rotateBlock(world, x, y, z, ForgeDirection.getOrientation(side));
		}

		return super.onItemUseFirst(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
	}

	@Override
	public void damageScrewdriver(EntityPlayer player, ItemStack stack) {
		stack.damageItem(1, player);
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
		return true;
	}

	@Override
	public boolean damage(ItemStack stack, int damage, EntityPlayer player, boolean simulated) {
		if (player != null)
			if (player.capabilities.isCreativeMode)
				return true;

		if (!simulated) stack.damageItem(1, player);
		return true;
	}

	@Override
	public boolean canUse(EntityPlayer player, ItemStack stack) {
		return true;
	}
	
	@Override
	public boolean canWrench(EntityPlayer player, int x, int y, int z) {
		return false;
	}
	
	@Override
	public void wrenchUsed(EntityPlayer player, int x, int y, int z) {
		player.swingItem();
	}
}
