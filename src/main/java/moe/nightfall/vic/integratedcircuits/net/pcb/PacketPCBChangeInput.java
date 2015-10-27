package moe.nightfall.vic.integratedcircuits.net.pcb;

import java.io.IOException;

import moe.nightfall.vic.integratedcircuits.client.gui.cad.GuiCAD;
import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.cp.CircuitProperties;
import moe.nightfall.vic.integratedcircuits.net.PacketTileEntity;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityCAD;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
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

	public PacketPCBChangeInput(boolean input, int[] io, int con, TileEntityCAD tileEntityCAD) {
		super(tileEntityCAD.xCoord, tileEntityCAD.yCoord, tileEntityCAD.zCoord);
		this.io = io;
		this.input = input;
		this.con = con;
		
		// Validate IO width before we send the packet
		CircuitData data = tileEntityCAD.getCircuitData();
		boolean widthOK = true;
		for(int ioSide = 0; ioSide <= 3; ioSide++) {
			widthOK = widthOK && data.maximumIOSize() >= CircuitProperties.getModeAtSide(con, ioSide).size;
		}
		// Now crash people who try to send an "invalid" packet... Doing it here gives us a decent stacktrace.
		if (!widthOK) Minecraft.getMinecraft().displayCrashReport(new CrashReport("PCB IO mode selected for at least one side is too long.\nContact mod authors to report this error.",
		                                                          new AssertionError("PCB IO mode selected for at least one side is too long. Size of PCB is " + data.getSize() + "x" + data.getSize())));
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
		
		CircuitData data = te.getCircuitData();
		
		data.getProperties().setCon(con);
		data.clearIOAndSetupIO();
		
		if (input && side == Side.SERVER) {
			te.getCircuitData().updateInput();
			CommonProxy.networkWrapper.sendToAllAround(this, new TargetPoint(te.getWorldObj().getWorldInfo()
				.getVanillaDimension(), xCoord, yCoord, zCoord, 8));
		}
		if (side == Side.CLIENT && Minecraft.getMinecraft().currentScreen instanceof GuiCAD)
			((GuiCAD) Minecraft.getMinecraft().currentScreen).refreshIO();
	}
}
