package moe.nightfall.vic.integratedcircuits.compat.buildcraft;

import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.Content;
import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketProvider;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketWrapper;
import moe.nightfall.vic.integratedcircuits.misc.RayTracer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.PipeManager;
import buildcraft.api.transport.pluggable.PipePluggable;
import codechicken.lib.vec.BlockCoord;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
				return pluggable instanceof ISocketWrapper ? ((ISocketWrapper) pluggable).getSocket() : null;
			}
		});

		MinecraftForge.EVENT_BUS.register(new BCEventHandler());
	}
	
	public static class BCEventHandler {
		
		@SubscribeEvent
		public void onPlayerInteract(PlayerInteractEvent event) {
			if (event.action != Action.RIGHT_CLICK_BLOCK)
				return;
			TileEntity te = event.world.getTileEntity(event.x, event.y, event.z);
			if (te instanceof IPipeTile) {
				IPipeTile ptile = (IPipeTile) te;
				PipePluggable ppl = ptile.getPipePluggable(ForgeDirection.getOrientation(event.face));
				if (ppl instanceof ISocketWrapper) {
					MovingObjectPosition target = RayTracer.rayTrace(event.entityPlayer, 1F);
					ItemStack stack = event.entityPlayer.getCurrentEquippedItem();

					// Have to ignore the soldering iron since this isn't a
					// regular socket, I can't remove the gate
					// TODO Might want an API method for this
					if (stack.getItem() == Content.itemSolderingIron)
						return;
					((ISocketWrapper) ppl).getSocket().activate(event.entityPlayer, target, stack);

					event.useBlock = Result.DENY;
					event.useItem = Result.DENY;
				}
			}
		}
	}

	public static IPipeTile getPipe(World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(x, y, z);
		return (IPipeTile) (te instanceof IPipeTile ? te : null);
	}
}
