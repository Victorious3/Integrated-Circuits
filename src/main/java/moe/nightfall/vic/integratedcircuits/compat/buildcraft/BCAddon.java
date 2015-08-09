package moe.nightfall.vic.integratedcircuits.compat.buildcraft;

import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.PipeManager;
import buildcraft.api.transport.pluggable.PipePluggable;
import codechicken.lib.vec.BlockCoord;

public class BCAddon {
	public static void preInit() {

		PipeManager.registerPipePluggable(GatePipePluggable.class, Constants.MOD_ID + ".gate");

		IntegratedCircuitsAPI.registerSocketProvider(new ISocketProvider() {
			@Override
			public ISocket getSocketAt(World world, BlockCoord pos, int side) {
				IPipeTile tile = getPipe(world, pos.x, pos.y, pos.z);
				if (tile == null)
					return null;
				PipePluggable pluggable = tile.getPipePluggable(ForgeDirection.getOrientation(side));
				return (ISocket) (pluggable instanceof ISocket ? pluggable : null);
			}
		});
	}

	public static IPipeTile getPipe(World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(x, y, z);
		return (IPipeTile) (te instanceof IPipeTile ? te : null);
	}
}
