package moe.nightfall.vic.integratedcircuits.net.pcb;

import java.io.IOException;
import java.util.UUID;

import moe.nightfall.vic.integratedcircuits.client.gui.cad.GuiCAD;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.net.PacketTileEntity;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityCAD;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;

public class PacketPCBChangeName extends PacketTileEntity<PacketPCBChangeName> {
	private String name;
	private UUID uuid;

	public PacketPCBChangeName() {
	}

	public PacketPCBChangeName(EntityPlayer sender, String name, int xCoord, int yCoord, int zCoord) {
		super(xCoord, yCoord, zCoord);
		this.name = name;
		uuid = sender.getPersistentID();
	}

	@Override
	public void read(PacketBuffer buffer) throws IOException {
		super.read(buffer);
		this.name = buffer.readStringFromBuffer(7);
		long l1 = buffer.readLong();
		long l2 = buffer.readLong();
		this.uuid = new UUID(l1, l2);
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException {
		super.write(buffer);
		buffer.writeStringToBuffer(this.name);
		buffer.writeLong(uuid.getMostSignificantBits());
		buffer.writeLong(uuid.getLeastSignificantBits());
	}

	@Override
	public void process(EntityPlayer player, Side side) {
		TileEntityCAD te = (TileEntityCAD) player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if (te != null) {
			te.getCircuitData().getProperties().setName(this.name);
			if (side == Side.SERVER) {
				CommonProxy.networkWrapper.sendToAllAround(this, new TargetPoint(te.getWorldObj().provider.dimensionId,
						xCoord, yCoord, zCoord, 8));
			} else if (Minecraft.getMinecraft().currentScreen instanceof GuiCAD
					&& !MiscUtils.thePlayer().getPersistentID().equals(uuid))
				((GuiCAD) Minecraft.getMinecraft().currentScreen).refreshUI();
		}
	}
}
