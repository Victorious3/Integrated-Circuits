package moe.nightfall.vic.integratedcircuits.net.pcb;

import codechicken.lib.vec.BlockCoord;
import cpw.mods.fml.relauncher.Side;
import moe.nightfall.vic.integratedcircuits.net.PacketTileEntity;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityCAD;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityPrinter;
import net.minecraft.entity.player.EntityPlayer;

public class PacketPCBPrint extends PacketTileEntity<PacketPCBPrint> {

	public PacketPCBPrint() {
	}

	public PacketPCBPrint(int xCoord, int yCoord, int zCoord) {
		super(xCoord, yCoord, zCoord);
	}

	@Override
	public void process(EntityPlayer player, Side side) {
		TileEntityCAD cad = (TileEntityCAD) player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if (cad != null && cad.isPrinterConnected()) {
			BlockCoord pl = new BlockCoord(cad).offset(cad.printerLocation().ordinal());
			TileEntityPrinter printer = (TileEntityPrinter) player.worldObj.getTileEntity(pl.x, pl.y, pl.z);
			if (printer != null) {
				printer.print(cad.getCircuitData());
			}
		}
	}
}
