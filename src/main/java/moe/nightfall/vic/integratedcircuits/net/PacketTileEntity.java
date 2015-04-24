package moe.nightfall.vic.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;

public abstract class PacketTileEntity<T extends AbstractPacket<T>> extends AbstractPacket<T> {
	protected int xCoord, yCoord, zCoord;

	public PacketTileEntity() {
	}

	public PacketTileEntity(int xCoord, int yCoord, int zCoord) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
	}

	@Override
	public void read(PacketBuffer buffer) throws IOException {
		this.xCoord = buffer.readInt();
		this.yCoord = buffer.readInt();
		this.zCoord = buffer.readInt();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException {
		buffer.writeInt(xCoord);
		buffer.writeInt(yCoord);
		buffer.writeInt(zCoord);
	}
}
