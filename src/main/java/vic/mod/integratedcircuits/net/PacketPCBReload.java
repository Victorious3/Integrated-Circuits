package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.TileEntityPCBLayout;
import vic.mod.integratedcircuits.client.GuiPCBLayout;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;

public class PacketPCBReload extends PacketPCB<PacketPCBReload>
{
	private byte size;
	
	public PacketPCBReload() {}
	
	public PacketPCBReload(byte size, int xCoord, int yCoord, int zCoord)
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
			te.getCircuitData().clear(size);
			if(side == side.SERVER)
				IntegratedCircuits.networkWrapper.sendToAllAround(this, 
					new TargetPoint(te.getWorldObj().getWorldInfo().getVanillaDimension(), xCoord, yCoord, zCoord, 8));
			else if(Minecraft.getMinecraft().currentScreen instanceof GuiPCBLayout)
				((GuiPCBLayout)Minecraft.getMinecraft().currentScreen).refreshUI();
		}
	}
}
