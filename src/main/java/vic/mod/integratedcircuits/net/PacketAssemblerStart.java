package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.TileEntityAssembler;
import cpw.mods.fml.relauncher.Side;

public class PacketAssemblerStart extends PacketTileEntity<PacketAssemblerStart>
{
	private byte amount;
	
	public PacketAssemblerStart() {}
	
	public PacketAssemblerStart(int xCoord, int yCoord, int zCoord, byte amount)
	{
		super(xCoord, yCoord, zCoord);
		this.amount = amount;
	}

	
	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
		amount = buffer.readByte();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		buffer.writeByte(amount);
	}

	@Override
	public void process(EntityPlayer player, Side side) 
	{
		TileEntityAssembler te = (TileEntityAssembler)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(te == null) return;
		if(side == Side.SERVER)
		{
			if(amount > 0)
			{
				if(te.cdata != null && te.laserHelper.getLaserAmount() > 0 && te.getStackInSlot(1) != null && te.queue == 0 && te.getStackInSlot(1).getItemDamage() == 0)
				{
					te.decrStackSize(1, 1);
					te.laserHelper.reset();
					te.laserHelper.start();
					te.queue = amount;
					IntegratedCircuits.networkWrapper.sendToDimension(this, player.worldObj.provider.dimensionId);
				}
			}
			else 
			{
				te.laserHelper.reset();
				te.queue = amount;
				IntegratedCircuits.networkWrapper.sendToDimension(this, player.worldObj.provider.dimensionId);
			}
		}
		else if(side == Side.CLIENT)
		{
			te.laserHelper.isRunning = true;
			te.getWorldObj().markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
			te.excMatrix = new boolean[te.size][te.size];
			te.isOccupied = true;
			te.queue = amount;
		}
	}
}
