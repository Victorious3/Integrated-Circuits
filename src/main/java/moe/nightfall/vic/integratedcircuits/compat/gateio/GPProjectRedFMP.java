package moe.nightfall.vic.integratedcircuits.compat.gateio;

import mods.immibis.redlogic.api.wiring.IBundledEmitter;
import moe.nightfall.vic.integratedcircuits.api.gate.GateIOProvider;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket.EnumConnectionType;
import mrtjp.projectred.transmission.IRedwireEmitter;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;

@InterfaceList({ @Interface(iface = "mrtjp.projectred.api.IBundledEmitter", modid = "ProjRed|Core"),
		@Interface(iface = "mrtjp.projectred.api.IConnectable", modid = "ProjRed|Core"), })
public class GPProjectRedFMP extends GateIOProvider implements mrtjp.projectred.api.IBundledEmitter,
		mrtjp.projectred.api.IConnectable {

	@Override
	public boolean canConnectCorner(int arg0) {
		return false;
	}

	@Override
	public boolean connectCorner(mrtjp.projectred.api.IConnectable arg0, int arg1, int arg2) {
		return connectStraight(arg0, arg1, arg2);
	}

	@Override
	public boolean connectInternal(mrtjp.projectred.api.IConnectable arg0, int arg1) {
		return connectStraight(arg0, arg1, 0);
	}

	@Override
	public boolean connectStraight(mrtjp.projectred.api.IConnectable arg0, int arg1, int arg2) {
		int side = socket.getRotationRel(arg1);
		EnumConnectionType type = socket.getConnectionTypeAtSide(side);
		if (arg0 instanceof IRedwireEmitter && type.isRedstone())
			return true;
		else if (arg0 instanceof IBundledEmitter && type.isBundled())
			return true;
		return false;
	}

	@Override
	public byte[] getBundledSignal(int arg0) {
		int rot = socket.getRotationRel(arg0);
		EnumConnectionType type = socket.getConnectionTypeAtSide(rot);
		if (!type.isBundled())
			return null;
		return socket.getOutput()[rot];
	}
}