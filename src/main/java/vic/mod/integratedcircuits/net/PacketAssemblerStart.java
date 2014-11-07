package vic.mod.integratedcircuits.net;

import net.minecraft.entity.player.EntityPlayer;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.TileEntityAssembler;
import cpw.mods.fml.relauncher.Side;

public class PacketAssemblerStart extends PacketTileEntity<PacketAssemblerStart>
{
	public PacketAssemblerStart() {}
	
	public PacketAssemblerStart(int xCoord, int yCoord, int zCoord)
	{
		super(xCoord, yCoord, zCoord);
	}
	
	@Override
	public void process(EntityPlayer player, Side side) 
	{
		TileEntityAssembler te = (TileEntityAssembler)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(te == null) return;
		if(side == Side.SERVER && te.getStackInSlot(1) == null)
		{
			te.laserHelper.reset();
			te.laserHelper.start();
			IntegratedCircuits.networkWrapper.sendToAll(this);
		}
		else if(side == Side.CLIENT)
		{
			te.excMatrix = new boolean[te.size][te.size];
			te.isOccupied = true;
		}
	}
}
