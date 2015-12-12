package moe.nightfall.vic.integratedcircuits.net;

import java.io.IOException;

import moe.nightfall.vic.integratedcircuits.LaserHelper.Laser;
import moe.nightfall.vic.integratedcircuits.client.TileEntityAssemblerRenderer;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityAssembler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

public class PacketAssemblerUpdate extends PacketTileEntity<PacketAssemblerUpdate> {
	private int x, y, id;
	private boolean isRunning;

	public PacketAssemblerUpdate() {
	}

	public PacketAssemblerUpdate(boolean isActive, int x, int y, int id, int xCoord, int yCoord, int zCoord) {
		super(xCoord, yCoord, zCoord);
		this.x = x;
		this.y = y;
		this.id = id;
		this.isRunning = isActive;
	}

	@Override
	public void read(PacketBuffer buffer) throws IOException {
		super.read(buffer);
		this.isRunning = buffer.readBoolean();
		this.id = buffer.readInt();
		this.x = buffer.readInt();
		this.y = buffer.readInt();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException {
		super.write(buffer);
		buffer.writeBoolean(isRunning);
		buffer.writeInt(id);
		buffer.writeInt(x);
		buffer.writeInt(y);
	}

	@Override
	public void process(EntityPlayer player, Side side) {
		TileEntityAssembler te = (TileEntityAssembler) player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if (te == null)
			return;
		Laser laser = te.laserHelper.getLaser(id);
		if (laser == null)
			return;
		if (te.excMatrix != null && laser.isRunning)
			te.excMatrix[laser.x][laser.y] = true;
		TileEntityAssemblerRenderer.scheduleFramebuffer(te);
		laser.isRunning = isRunning;
		te.laserHelper.updateStatus();
		if (!te.laserHelper.isRunning)
			te.getWorldObj().markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
		laser.setAim(x, y);
		te.laserHelper.position++;
	}
}
