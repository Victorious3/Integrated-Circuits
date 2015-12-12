package moe.nightfall.vic.integratedcircuits.compat.gateio;

import moe.nightfall.vic.integratedcircuits.api.gate.ISocket.EnumConnectionType;
import mrtjp.projectred.api.IBundledEmitter;
import mrtjp.projectred.api.IConnectable;
import mrtjp.projectred.transmission.IRedwireEmitter;
import net.minecraft.tileentity.TileEntity;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Rotation;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;

@InterfaceList({
		@Interface(iface = "mrtjp.projectred.api.IBundledEmitter", modid = "ProjRed|Core"),
		@Interface(iface = "mrtjp.projectred.api.IConnectable", modid = "ProjRed|Core")
})
public class GPProjectRedFMP extends GPProjectRed implements IBundledEmitter, IConnectable {

	@Override
	@Method(modid = "ProjRed|Core")
	public byte[] calculateBundledInput(int side, int rotation, int abs, BlockCoord offset) {

		byte[] power = null;
		// Corner signal
		if (((abs ^ 1) & 6) != ((socket.getSide() ^ 1) & 6)) {
			BlockCoord pos = offset.copy().offset(socket.getSide());
			TileEntity t = socket.getWorld().getTileEntity(pos.x, pos.y, pos.z);
			if (t != null && t instanceof TileMultipart)
				power = updateBundledPartSignal(((TileMultipart) t).partMap(abs ^ 1), Rotation.rotationTo(abs ^ 1, socket.getSide() ^ 1));
			if (power != null)
				return power;
		}

		power = super.calculateBundledInput(side, rotation, abs, offset);
		if (power != null)
			return power;

		// Internal signal
		if ((abs & 6) != (socket.getSide() & 6)) {
			TMultiPart tp = ((TileMultipart) ((JCuboidPart) socket.getWrapper()).getTile()).partMap(abs);
			power = updateBundledPartSignal(tp, Rotation.rotationTo(abs, socket.getSide()));
			if (power != null)
				return power;
		}

		return null;
	}

	@Override
	@Method(modid = "ProjRed|Core")
	public int calculateRedstoneInput(int side, int rotation, int abs, BlockCoord offset) {

		int power = 0;

		// Corner signal
		if (((abs ^ 1) & 6) != ((socket.getSide() ^ 1) & 6)) {
			BlockCoord pos = offset.copy().offset(socket.getSide());
			TileEntity t = socket.getWorld().getTileEntity(pos.x, pos.y, pos.z);
			if (t != null && t instanceof TileMultipart)
				power = updatePartSignal(((TileMultipart) t).partMap(abs ^ 1), Rotation.rotationTo(abs ^ 1, socket.getSide() ^ 1));
			if (power > 0)
				return power / 17;
		}

		// Internal signal
		TMultiPart tp = ((TileMultipart) ((JCuboidPart) socket.getWrapper()).getTile()).partMap(abs);
		if ((abs & 6) != (socket.getSide() & 6)) {
			power = updatePartSignal(tp, Rotation.rotationTo(abs, socket.getSide()));
			if (power > 0)
				return power / 17;
		}

		return 0;
	}

	@Override
	@Method(modid = "ProjRed|Core")
	public boolean canConnectCorner(int arg0) {
		return false;
	}

	@Override
	@Method(modid = "ProjRed|Core")
	public boolean connectCorner(IConnectable arg0, int arg1, int arg2) {
		return connectStraight(arg0, arg1, arg2);
	}

	@Override
	@Method(modid = "ProjRed|Core")
	public boolean connectInternal(IConnectable arg0, int arg1) {
		return connectStraight(arg0, arg1, 0);
	}

	@Override
	@Method(modid = "ProjRed|Core")
	public boolean connectStraight(IConnectable arg0, int arg1, int arg2) {
		int side = socket.getRotationRel(arg1);
		EnumConnectionType type = socket.getConnectionTypeAtSide(side);
		if (arg0 instanceof IRedwireEmitter && type.isRedstone())
			return true;
		else if (arg0 instanceof IBundledEmitter && type.isBundled())
			return true;
		return false;
	}

	@Override
	@Method(modid = "ProjRed|Core")
	public byte[] getBundledSignal(int arg0) {
		int rot = socket.getRotationRel(arg0);
		EnumConnectionType type = socket.getConnectionTypeAtSide(rot);
		if (!type.isBundled())
			return null;
		return socket.getOutput()[rot];
	}
}