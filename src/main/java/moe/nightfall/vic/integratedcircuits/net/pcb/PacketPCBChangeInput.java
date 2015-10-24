package moe.nightfall.vic.integratedcircuits.net.pcb;

import java.io.IOException;

import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.client.gui.cad.GuiCAD;
import moe.nightfall.vic.integratedcircuits.net.PacketTileEntity;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityCAD;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;

public class PacketPCBChangeInput extends PacketTileEntity<PacketPCBChangeInput> {
	private boolean input;
	private int[] io;
	private int con;

	public PacketPCBChangeInput() {
	}

	public PacketPCBChangeInput(boolean input, int[] io, int con, int xCoord, int yCoord, int zCoord) {
		super(xCoord, yCoord, zCoord);
		this.io = io;
		this.input = input;
		this.con = con;
	}

	@Override
	public void read(PacketBuffer buffer) throws IOException {
		super.read(buffer);
		input = buffer.readBoolean();
		con = buffer.readInt();
		io = new int[4];
		io[0] = buffer.readInt();
		io[1] = buffer.readInt();
		io[2] = buffer.readInt();
		io[3] = buffer.readInt();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException {
		super.write(buffer);
		buffer.writeBoolean(input);
		buffer.writeInt(con);
		buffer.writeInt(io[0]);
		buffer.writeInt(io[1]);
		buffer.writeInt(io[2]);
		buffer.writeInt(io[3]);
	}

	@Override
	public void process(EntityPlayer player, Side side) {
		TileEntityCAD te = (TileEntityCAD) player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if (te == null)
			return;
		if (input)
			te.in = io;
		else
			te.out = io;
		if (te.getCircuitData().supportsBundled())
			te.getCircuitData().getProperties().setCon(con);
		else {
			// TODO: Make this nicer... This is a quick solution that seems to work, but isn't very good.
			final int NA = te.getCircuitData().getProperties().setModeAtSide(0, ISocket.EnumConnectionType.ANALOG);
			final int EA = te.getCircuitData().getProperties().setModeAtSide(1, ISocket.EnumConnectionType.ANALOG);
			final int SA = te.getCircuitData().getProperties().setModeAtSide(2, ISocket.EnumConnectionType.ANALOG);
			final int WA = te.getCircuitData().getProperties().setModeAtSide(3, ISocket.EnumConnectionType.ANALOG);
			final int NN = te.getCircuitData().getProperties().setModeAtSide(0, ISocket.EnumConnectionType.NONE);
			final int EN = te.getCircuitData().getProperties().setModeAtSide(1, ISocket.EnumConnectionType.NONE);
			final int SN = te.getCircuitData().getProperties().setModeAtSide(2, ISocket.EnumConnectionType.NONE);
			final int WN = te.getCircuitData().getProperties().setModeAtSide(3, ISocket.EnumConnectionType.NONE);
			
			if (con == NA) {
				te.getCircuitData().getProperties().setCon(NN);
			} else if (con == EA) {
				te.getCircuitData().getProperties().setCon(EN);
			} else if (con == SA) {
				te.getCircuitData().getProperties().setCon(SN);
			} else if (con == WA) {
				te.getCircuitData().getProperties().setCon(WN);
			} else {
				te.getCircuitData().getProperties().setCon(0);
			}
		}
		if (input && side == Side.SERVER) {
			te.getCircuitData().updateInput();
			CommonProxy.networkWrapper.sendToAllAround(this, new TargetPoint(te.getWorldObj().getWorldInfo()
				.getVanillaDimension(), xCoord, yCoord, zCoord, 8));
		}
		if (side == Side.CLIENT && Minecraft.getMinecraft().currentScreen instanceof GuiCAD)
			((GuiCAD) Minecraft.getMinecraft().currentScreen).refreshIO();
	}
}
