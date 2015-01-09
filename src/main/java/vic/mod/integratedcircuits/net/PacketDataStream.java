package vic.mod.integratedcircuits.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.tile.TileEntityGate;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.packet.PacketCustom;
import cpw.mods.fml.relauncher.Side;

public class PacketDataStream extends PacketTileEntity<PacketDataStream>
{
	private MCDataInput in;
	private TileEntityGate gate;
	
	public PacketDataStream() {}
	
	public PacketDataStream(TileEntityGate gate)
	{
		super(gate.xCoord, gate.yCoord, gate.zCoord);
		this.gate = gate;
	}
	
	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
		int size = buffer.readInt();
		ByteBuf buf = Unpooled.buffer(size);
		buffer.readBytes(buf);
		in = new PacketCustom(buf);
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		byte[] out = gate.out.toByteArray();
		buffer.writeInt(out.length);
		buffer.writeBytes(out);
		gate.out = null;
	}

	@Override
	public void process(EntityPlayer player, Side side) 
	{
		TileEntityGate gate = (TileEntityGate)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(gate == null) return;
		byte discr = in.readByte();
		gate.getGate().read(discr, in);
	}
}
