package vic.mod.integratedcircuits.net;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.TileEntityPCBLayout;
import vic.mod.integratedcircuits.ic.CircuitData;
import cpw.mods.fml.relauncher.Side;

public class PacketPCBUpdate extends PacketPCB<PacketPCBUpdate>
{
	private CircuitData data;
	private ByteBuf buf;
	
	public PacketPCBUpdate(){}
	
	/** Will update the PCB matrix for the client **/
	public PacketPCBUpdate(CircuitData data, int tx, int ty, int tz)
	{
		super(tx, ty, tz);
		this.data = data;
	}
	
	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
		this.buf = buffer.copy();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		data.writeToStream(buffer);
	}

	@Override public void process(EntityPlayer player, Side side) 
	{
		TileEntityPCBLayout layout = (TileEntityPCBLayout)Minecraft.getMinecraft().theWorld.getTileEntity(xCoord, yCoord, zCoord);
		if(layout != null)
		{
			data = layout.getCircuitData();
			data.readFromStream(buf);
		}
	}
}
