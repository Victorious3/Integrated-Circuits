package moe.nightfall.vic.integratedcircuits.net;

import java.io.IOException;

import moe.nightfall.vic.integratedcircuits.api.ISocket.EnumConnectionType;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiPCBLayout;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityPCBLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;

public class PacketPCBClear extends PacketTileEntity<PacketPCBClear>
{
	private byte size;
	
	public PacketPCBClear() {}
	
	public PacketPCBClear(byte size, int xCoord, int yCoord, int zCoord)
	{
		super(xCoord, yCoord, zCoord);
		this.size = size;
	}
	
	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
		size = buffer.readByte();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		buffer.writeByte(size);
	}

	@Override
	public void process(EntityPlayer player, Side side) 
	{
		TileEntityPCBLayout te = (TileEntityPCBLayout)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(te != null)
		{
			if(side == side.SERVER)
				te.cache.create(player.getGameProfile().getId());
			
			boolean changed = te.getCircuitData().hasChanged();
			te.getCircuitData().clear(size);
			if(!te.getCircuitData().supportsBundled()) te.getCircuitData().getProperties().setCon(0);
			
			te.in = new int[4];
			te.out = new int[4];
			for(int i = 0; i < 4; i++)
				if(te.getCircuitData().getProperties().getModeAtSide(i) == EnumConnectionType.ANALOG) te.in[i] = 1;
			
			if(side == side.SERVER)
			{
				if(changed) te.cache.capture(player.getGameProfile().getId());
				
				CommonProxy.networkWrapper.sendToAllAround(this, 
					new TargetPoint(te.getWorldObj().getWorldInfo().getVanillaDimension(), xCoord, yCoord, zCoord, 8));		
			}
			else if(Minecraft.getMinecraft().currentScreen instanceof GuiPCBLayout)
				((GuiPCBLayout)Minecraft.getMinecraft().currentScreen).refreshUI();
		}
	}
}
