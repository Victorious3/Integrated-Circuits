package moe.nightfall.vic.integratedcircuits.compat.gateio;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.SidedComponent;
import li.cil.oc.api.network.SimpleComponent;
import li.cil.oc.api.network.SimpleComponent.SkipInjection;
import moe.nightfall.vic.integratedcircuits.api.gate.GateIOProvider;
import moe.nightfall.vic.integratedcircuits.api.gate.IGatePeripheralProvider;
import moe.nightfall.vic.integratedcircuits.gate.peripheral.GatePeripheral;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;

@SkipInjection
@InterfaceList({
		@Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers"),
		@Interface(iface = "li.cil.oc.api.network.SidedComponent", modid = "OpenComputers"),
		@Interface(iface = "li.cil.oc.api.network.ManagedPeripheral", modid = "OpenComputers")
})
public class GPOpenComputers extends GateIOProvider implements SimpleComponent, SidedComponent, ManagedPeripheral {

	@Override
	@Method(modid = "OpenComputers")
	public boolean canConnectNode(ForgeDirection side) {
		if (socket.getGate() instanceof IGatePeripheralProvider) {
			IGatePeripheralProvider provider = (IGatePeripheralProvider) socket.getGate();
			return provider.hasPeripheral(side.ordinal());
		}
		return false;
	}

	@Override
	@Method(modid = "OpenComputers")
	public String getComponentName() {
		if (socket.getGate() instanceof IGatePeripheralProvider) {
			IGatePeripheralProvider provider = (IGatePeripheralProvider) socket.getGate();
			GatePeripheral peripheral = provider.getPeripheral();
			return peripheral.getType();
		}
		return null;
	}

	@Override
	@Method(modid = "OpenComputers")
	public String[] methods() {
		if (socket.getGate() instanceof IGatePeripheralProvider) {
			IGatePeripheralProvider provider = (IGatePeripheralProvider) socket.getGate();
			GatePeripheral peripheral = provider.getPeripheral();
			return peripheral.getMethodNames();
		}
		return null;
	}

	@Override
	@Method(modid = "OpenComputers")
	public Object[] invoke(String method, Context context, Arguments args) throws Exception {
		if (socket.getGate() instanceof IGatePeripheralProvider) {
			IGatePeripheralProvider provider = (IGatePeripheralProvider) socket.getGate();
			GatePeripheral peripheral = provider.getPeripheral();
			return peripheral.callMethod(method, args.toArray());
		}
		return null;
	}
}
