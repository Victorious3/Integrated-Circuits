package moe.nightfall.vic.integratedcircuits.net.pcb;

import java.io.IOException;

import moe.nightfall.vic.integratedcircuits.client.gui.cad.GuiCAD;
import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.net.PacketTileEntity;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityCAD;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import cpw.mods.fml.relauncher.Side;

public class PacketPCBLoad extends PacketTileEntity<PacketPCBLoad> {
	private CircuitData data;

	public PacketPCBLoad() {
	}

	/** Used by the Disk IO to send the complete CircuitData to the client **/
	public PacketPCBLoad(CircuitData data, int xCoord, int yCoord, int zCoord) {
		super(xCoord, yCoord, zCoord);
		this.data = data;
	}

	@Override
	public void read(PacketBuffer buffer) throws IOException {
		super.read(buffer);
		data = CircuitData.readFromNBT(buffer.readNBTTagCompoundFromBuffer());
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException {
		super.write(buffer);
		buffer.writeNBTTagCompoundToBuffer(data.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void process(EntityPlayer player, Side side) {
		TileEntityCAD layout = (TileEntityCAD) player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if (layout != null) {
			data.setParent(layout);
			layout.setCircuitData(data);
		}
		if (Minecraft.getMinecraft().currentScreen instanceof GuiCAD)
			((GuiCAD) Minecraft.getMinecraft().currentScreen).refreshUI();
	}
}
