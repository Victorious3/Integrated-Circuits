package vic.mod.integratedcircuits;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import vic.mod.integratedcircuits.client.gui.GuiAssembler;
import vic.mod.integratedcircuits.client.gui.GuiPCBLayout;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) 
	{
		if(id == 0) return new ContainerPCBLayout((TileEntityPCBLayout)world.getTileEntity(x, y, z));
		else if(id == 1) return new ContainerAssembler(player.inventory, (TileEntityAssembler)world.getTileEntity(x, y, z));
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) 
	{
		if(id == 0) return new GuiPCBLayout(new ContainerPCBLayout((TileEntityPCBLayout)world.getTileEntity(x, y, z)));
		else if(id == 1) return new GuiAssembler(new ContainerAssembler(player.inventory, (TileEntityAssembler)world.getTileEntity(x, y, z)));
		return null;
	}
}
