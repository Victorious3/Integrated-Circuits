package moe.nightfall.vic.integratedcircuits.compat.gateio;

import mods.immibis.redlogic.api.wiring.IBundledEmitter;
import mods.immibis.redlogic.api.wiring.IBundledUpdatable;
import mods.immibis.redlogic.api.wiring.IBundledWire;
import mods.immibis.redlogic.api.wiring.IConnectable;
import mods.immibis.redlogic.api.wiring.IWire;
import moe.nightfall.vic.integratedcircuits.api.gate.GateIOProvider;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket.EnumConnectionType;
import net.minecraft.tileentity.TileEntity;
import codechicken.lib.vec.BlockCoord;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;

@InterfaceList({ @Interface(iface = "mods.immibis.redlogic.api.wiring.IBundledUpdatable", modid = "RedLogic"),
		@Interface(iface = "mods.immibis.redlogic.api.wiring.IBundledEmitter", modid = "RedLogic"),
		@Interface(iface = "mods.immibis.redlogic.api.wiring.IConnectable", modid = "RedLogic") })
public class GPRedLogic extends GateIOProvider implements IBundledUpdatable, IBundledEmitter, IConnectable {

	@Override
	@Method(modid = "RedLogic")
	public byte[] calculateBundledInput(int side, int rotation, int abs, BlockCoord offset) {
		byte[] power = null;
		TileEntity te = socket.getWorld().getTileEntity(offset.x, offset.y, offset.z);
		if (te instanceof IBundledEmitter) {
			IBundledEmitter emitter = (IBundledEmitter) te;
			power = emitter.getBundledCableStrength(socket.getSide(), abs ^ 1);
		}
		return power;
	}

	@Override
	@Method(modid = "RedLogic")
	public byte[] getBundledCableStrength(int blockFace, int toDirection) {
		return GateIO.getBundledSignal(socket, toDirection);
	}

	@Override
	@Method(modid = "RedLogic")
	public void onBundledInputChanged() {
		socket.updateInput();
	}

	@Override
	@Method(modid = "RedLogic")
	public boolean connects(IWire wire, int blockFace, int fromDirection) {
		if ((fromDirection & 6) == (socket.getSide() & 6))
			return false;
		int rel = socket.getSideRel(fromDirection);

		if (blockFace == -1)
			return false;
		EnumConnectionType type = socket.getConnectionTypeAtSide(rel);
		if (wire instanceof IBundledWire)
			return type.isBundled();
		else
			return type.isRedstone();
	}

	@Override
	@Method(modid = "RedLogic")
	public boolean connectsAroundCorner(IWire wire, int blockFace, int fromDirection) {
		// TODO I could do something about this.
		return false;
	}
}