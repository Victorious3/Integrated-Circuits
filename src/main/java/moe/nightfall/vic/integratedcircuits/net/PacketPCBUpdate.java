package moe.nightfall.vic.integratedcircuits.net;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityCAD;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import cpw.mods.fml.relauncher.Side;

public class PacketPCBUpdate extends PacketTileEntity<PacketPCBUpdate> {
	private CircuitData data;
	private ByteBuf buf;

	public PacketPCBUpdate() {
	}

	/** Will update the PCB matrix for the client **/
	public PacketPCBUpdate(CircuitData data, int tx, int ty, int tz) {
		super(tx, ty, tz);
		this.data = data;
	}

	@Override
	public void read(PacketBuffer buffer) throws IOException {
		super.read(buffer);
		this.buf = buffer.copy();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException {
		super.write(buffer);
		data.writeToStream(buffer);
	}

	@Override
	public void process(EntityPlayer player, Side side) {
		TileEntityCAD layout = (TileEntityCAD) Minecraft.getMinecraft().theWorld.getTileEntity(xCoord,
				yCoord, zCoord);
		if (layout != null) {
			data = layout.getCircuitData();
			data.readFromStream(buf);
		}
	}
}
