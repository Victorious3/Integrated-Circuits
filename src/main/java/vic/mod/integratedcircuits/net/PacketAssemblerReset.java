package vic.mod.integratedcircuits.net;

import net.minecraft.entity.player.EntityPlayer;
import vic.mod.integratedcircuits.TileEntityAssembler;
import cpw.mods.fml.relauncher.Side;

public class PacketAssemblerReset extends PacketTileEntity<PacketAssemblerReset>
{
	public PacketAssemblerReset() {}
	
	public PacketAssemblerReset(int xCoord, int yCoord, int zCoord)
	{
		super(xCoord, yCoord, zCoord);
	}
	
	@Override
	public void process(EntityPlayer player, Side side) 
	{
		TileEntityAssembler te = (TileEntityAssembler)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(te == null) return;
		te.verts = null;
	}
}
