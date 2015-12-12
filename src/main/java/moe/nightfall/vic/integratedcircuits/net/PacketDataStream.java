package moe.nightfall.vic.integratedcircuits.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.vec.BlockCoord;
import net.minecraftforge.fml.relauncher.Side;

public class PacketDataStream extends PacketTileEntity<PacketDataStream> {
	private MCDataInput in;
	private MCDataOutputImpl out;
	private int side;

	public PacketDataStream() {
	}

	public PacketDataStream(MCDataOutputImpl out, int x, int y, int z, int side) {
		super(x, y, z);
		this.out = out;
		this.side = side;
	}

	@Override
	public void read(PacketBuffer buffer) throws IOException {
		super.read(buffer);
		side = buffer.readInt();
		ByteBuf buf = Unpooled.buffer();
		buffer.readBytes(buf, buffer.readableBytes());
		in = new PacketCustom(buf);
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException {
		super.write(buffer);
		buffer.writeInt(side);
		PacketCustom packet = new PacketCustom("", 1);
		packet.writeByteArray(out.toByteArray());
		buffer.writeBytes(packet.getByteBuf());
	}

	@Override
	public void process(EntityPlayer player, Side side) {
		ISocket socket = IntegratedCircuitsAPI.getSocketAt(player.worldObj, new BlockCoord(xCoord, yCoord, zCoord),
				this.side);
		if (socket == null)
			return;
		socket.read(in);
	}
}
