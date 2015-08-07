package moe.nightfall.vic.integratedcircuits.net.pcb;

import java.io.IOException;

import moe.nightfall.vic.integratedcircuits.client.gui.cad.GuiCAD;
import moe.nightfall.vic.integratedcircuits.net.PacketTileEntity;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityCAD;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;

public class PacketPCBSimulation extends PacketTileEntity<PacketPCBSimulation> {

	private boolean step, pausing;

	public PacketPCBSimulation() {
	}

	public PacketPCBSimulation(boolean step, boolean pausing, int xCoord, int yCoord, int zCoord) {
		super(xCoord, yCoord, zCoord);
		this.step = step;
		this.pausing = pausing;
	}

	@Override
	public void read(PacketBuffer buffer) throws IOException {
		super.read(buffer);
		this.step = buffer.readBoolean();
		this.pausing = buffer.readBoolean();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException {
		super.write(buffer);
		buffer.writeBoolean(step);
		buffer.writeBoolean(pausing);
	}

	@Override
	public void process(EntityPlayer player, Side side) {
		TileEntityCAD te = (TileEntityCAD) player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if (te != null) {
			if (step)
				te.step();
			te.setPausing(pausing);

			if (side.isServer()) {
				CommonProxy.networkWrapper.sendToAllAround(this, new TargetPoint(te.getWorldObj().provider.dimensionId,
						xCoord, yCoord, zCoord, 8));
			} else if (Minecraft.getMinecraft().currentScreen instanceof GuiCAD)
				((GuiCAD) Minecraft.getMinecraft().currentScreen).refreshUI();
		}
	}
}
