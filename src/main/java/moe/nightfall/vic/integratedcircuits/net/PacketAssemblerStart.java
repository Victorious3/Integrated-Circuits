package moe.nightfall.vic.integratedcircuits.net;

import java.io.IOException;

import moe.nightfall.vic.integratedcircuits.tile.TileEntityAssembler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
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
			if(amount > 0) te.requestCircuit(amount);
			else te.clearQueue();
		}
		else if(side == Side.CLIENT)
		{
			te.laserHelper.reset();
			te.laserHelper.isRunning = true;
			te.getWorldObj().markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
			te.excMatrix = new boolean[te.size][te.size];
			if(amount > 0) te.isOccupied = true;
			te.setQueueSize(amount);
		}
	}
}
