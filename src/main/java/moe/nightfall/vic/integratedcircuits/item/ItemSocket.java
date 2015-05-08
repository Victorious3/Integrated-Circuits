package moe.nightfall.vic.integratedcircuits.item;

import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.tile.TileEntitySocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;

public class ItemSocket extends ItemBase {
	public ItemSocket() {
		super("socket");
		setHasIcon(false);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		BlockCoord pos = new BlockCoord(x, y, z);
		Vector3 vhit = new Vector3(hitX, hitY, hitZ);
		pos.offset(side);
		return place(stack, player, world, pos, side, vhit);
	}

	private boolean place(ItemStack stack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 vhit) {
		BlockCoord pos2 = pos.copy().offset(side ^ 1);
		if (!MiscUtils.canPlaceGateOnSide(world, pos2.x, pos2.y, pos2.z, side))
			return false;

		if (world.getBlock(pos.x, pos.y, pos.z).isReplaceable(world, pos.x, pos.y, pos.z)) {
			world.setBlock(pos.x, pos.y, pos.z, IntegratedCircuits.blockSocket);
			TileEntitySocket te = (TileEntitySocket) world.getTileEntity(pos.x, pos.y, pos.z);
			te.getSocket().preparePlacement(player, pos2, side, stack);
			MiscUtils.playPlaceSound(world, pos);

			if (!player.capabilities.isCreativeMode)
				stack.stackSize--;
			return true;
		}
		return false;
	}
}
