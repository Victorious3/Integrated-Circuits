package moe.nightfall.vic.integratedcircuits.net.pcb;

import java.io.IOException;

import moe.nightfall.vic.integratedcircuits.api.gate.ISocket.EnumConnectionType;
import moe.nightfall.vic.integratedcircuits.client.gui.cad.GuiCAD;
import moe.nightfall.vic.integratedcircuits.net.PacketTileEntity;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityCAD;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;

public class PacketPCBClear extends PacketTileEntity<PacketPCBClear> {
	private byte size;

	public PacketPCBClear() {
	}

	public PacketPCBClear(byte size, int xCoord, int yCoord, int zCoord) {
		super(xCoord, yCoord, zCoord);
		this.size = size;
	}

	@Override
	public void read(PacketBuffer buffer) throws IOException {
		super.read(buffer);
		size = buffer.readByte();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException {
		super.write(buffer);
		buffer.writeByte(size);
	}

	// TODO Might want to move some of this stuff around to make it more
	// convenient, e.g All of this should be a method inside CDATA

	@Override
	public void process(EntityPlayer player, Side side) {
		TileEntityCAD te = (TileEntityCAD) player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if (te != null) {
			if (side == side.SERVER)
				te.cache.create(player.getGameProfile().getId());

			boolean changed = te.getCircuitData().hasChanged();
			
			te.getCircuitData().clearAllAndSetup(size);

			// Reset IO    TODO: Somehow move to circuitdata?
			te.in = new int[4];
			te.out = new int[4];

			for (int i = 0; i < 4; i++)
				if (te.getCircuitData().getProperties().getModeAtSide(i) == EnumConnectionType.ANALOG)
					te.in[i] = 1;

			if (side == side.SERVER) {
				if (changed)
					te.cache.capture(player.getGameProfile().getId());

				CommonProxy.networkWrapper.sendToAllAround(this, new TargetPoint(te.getWorldObj().getWorldInfo()
					.getVanillaDimension(), xCoord, yCoord, zCoord, 8));
			} else if (Minecraft.getMinecraft().currentScreen instanceof GuiCAD)
				((GuiCAD) Minecraft.getMinecraft().currentScreen).refreshUI();
		}
	}
}
