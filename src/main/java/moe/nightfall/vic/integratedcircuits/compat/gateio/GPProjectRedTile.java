package moe.nightfall.vic.integratedcircuits.compat.gateio;

import mrtjp.projectred.api.IBundledTile;
import mrtjp.projectred.transmission.BundledCablePart;
import net.minecraft.tileentity.TileEntity;
import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.Method;

@Interface(iface = "mrtjp.projectred.api.IBundledTile", modid = "ProjRed|Core")
public class GPProjectRedTile extends GPProjectRed implements IBundledTile {

	@Override
	@Method(modid = "ProjRed|Core")
	public boolean canConnectBundled(int side) {
		if ((side & 6) == (socket.getSide() & 6))
			return false;
		int rel = socket.getSideRel(side);

		// Dirty hack for P:R, will only return true if something can
		// connect from that side As there is no way to get the caller of
		// this method, this will return true even if the part connecting
		// can't connect when a different part on the given side can.

		BlockCoord pos = socket.getPos().offset(side);
		TileEntity t = socket.getWorld().getTileEntity(pos.x, pos.y, pos.z);

		if (t instanceof TileMultipart) {
			TMultiPart mp = ((TileMultipart) t).partMap(socket.getSide());
			if (!(mp instanceof BundledCablePart))
				return false;
		}

		return socket.getConnectionTypeAtSide(rel).isBundled();
	}

	@Override
	@Method(modid = "ProjRed|Core")
	public byte[] getBundledSignal(int dir) {
		return GateIO.getBundledSignal(socket, dir);
	}
}