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
	private MCDataOutputImpl out;
	
	public PacketDataStream() {}
	
	public PacketDataStream(MCDataOutputImpl out, int x, int y, int z)
	{
		super(x, y, z);
		this.out = out;
	}
	
	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
		ByteBuf buf = Unpooled.buffer();
		buffer.readBytes(buf, buffer.readableBytes());
		in = new PacketCustom(buf);
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		PacketCustom packet = new PacketCustom("", 1);
		packet.writeByteArray(out.toByteArray());
		buffer.writeBytes(packet.getByteBuf());
	}

	@Override
	public void process(EntityPlayer player, Side side) 
	{
		// TODO Change this
		TileEntityGate gate = (TileEntityGate)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(gate == null) return;
		byte discr = in.readByte();
		gate.getGate().read(discr, in);
	}
}
