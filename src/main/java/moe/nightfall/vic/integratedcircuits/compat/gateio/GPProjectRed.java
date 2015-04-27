package moe.nightfall.vic.integratedcircuits.compat.gateio;

import moe.nightfall.vic.integratedcircuits.api.gate.GateIOProvider;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket.EnumConnectionType;
import moe.nightfall.vic.integratedcircuits.tile.FMPartGate;
import mrtjp.projectred.api.IBundledEmitter;
import mrtjp.projectred.api.IBundledTile;
import mrtjp.projectred.api.IConnectable;
import mrtjp.projectred.transmission.APIImpl_Transmission;
import mrtjp.projectred.transmission.BundledCablePart;
import mrtjp.projectred.transmission.IRedwireEmitter;
import net.minecraft.tileentity.TileEntity;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Rotation;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.common.Optional.Method;

public class GPProjectRed extends GateIOProvider {

	private GPProjectRed() {
	}

	@Override
	@Method(modid = "ProjRed|Core")
	public byte[] calculateBundledInput(int side, int rotation, int abs, BlockCoord offset) {

		// Straight signal
		TileEntity t = socket.getWorld().getTileEntity(offset.x, offset.y, offset.z);

		if (t instanceof IBundledEmitter) {
			return updateBundledPartSignal(t, abs ^ 1);
		} else if (t instanceof TileMultipart) {
			return updateBundledPartSignal(((TileMultipart) t).partMap(socket.getSide()), (rotation + 2) % 4);
		} else {
			return APIImpl_Transmission.getBundledSignal(socket.getWorld(), offset, abs ^ 1);
		}
	}

	@Method(modid = "ProjRed|Transmission")
	protected byte[] updateBundledPartSignal(Object part, int r)
	{
		if (part instanceof IBundledEmitter)
			return ((IBundledEmitter) part).getBundledSignal(r);
		return null;
	}

	@Method(modid = "ForgeMultipart")
	protected int updatePartSignal(Object part, int r)
	{
		if (part instanceof IRedwireEmitter)
			return ((IRedwireEmitter) part).getRedwireSignal(r);
		return 0;
	}

	@Interface(iface = "mrtjp.projectred.api.IBundledTile", modid = "ProjRed|Core")
	public static class GPProjectRedTile extends GPProjectRed implements IBundledTile {

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

	@InterfaceList({
			@Interface(iface = "mrtjp.projectred.api.IBundledEmitter", modid = "ProjRed|Core"),
			@Interface(iface = "mrtjp.projectred.api.IConnectable", modid = "ProjRed|Core")
	})
	public static class GPProjectRedFMP extends GPProjectRed implements IBundledEmitter, IConnectable {

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
				TMultiPart tp = ((TileMultipart) ((FMPartGate) socket.getWrapper()).getTile()).partMap(abs);
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
			TMultiPart tp = ((TileMultipart) ((FMPartGate) socket.getWrapper()).getTile()).partMap(abs);
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
}
