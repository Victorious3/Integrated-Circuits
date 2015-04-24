package moe.nightfall.vic.integratedcircuits.client.gui;

import moe.nightfall.vic.integratedcircuits.ContainerAssembler;
import moe.nightfall.vic.integratedcircuits.ContainerPCBLayout;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityAssembler;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityPCBLayout;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (id) {
			case 0:
				return new ContainerPCBLayout((TileEntityPCBLayout) world.getTileEntity(x, y, z));
			case 1:
				return new ContainerAssembler(player.inventory, (TileEntityAssembler) world.getTileEntity(x, y, z));
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (id) {
			case 0:
				return new GuiPCBLayout(new ContainerPCBLayout((TileEntityPCBLayout) world.getTileEntity(x, y, z)));
			case 1:
				return new GuiAssembler(new ContainerAssembler(player.inventory, (TileEntityAssembler) world.getTileEntity(
						x, y, z)));
		}
		return null;
	}
}
