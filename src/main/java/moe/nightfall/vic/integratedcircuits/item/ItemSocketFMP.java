package moe.nightfall.vic.integratedcircuits.item;

import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketWrapper;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemSocketFMP extends JItemMultiPart {
	public ItemSocketFMP() {
		setUnlocalizedName(Constants.MOD_ID + ".socket_fmp");
		setCreativeTab(IntegratedCircuits.creativeTab);
		GameRegistry.registerItem(this, Constants.MOD_ID + "_socket_fmp", Constants.MOD_ID);
	}

	@Override
	public TMultiPart newPart(ItemStack stack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 arg5) {
		ISocketWrapper part = (ISocketWrapper) MultiPartRegistry.createPart(Constants.MOD_ID + ".socket_fmp", false);
		part.getSocket().preparePlacement(player, pos, side, stack);
		return (TMultiPart) part;
	}

	@Override
	public void registerIcons(IIconRegister ir) {
	}
}
