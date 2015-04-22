package moe.nightfall.vic.integratedcircuits.compat;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.SidedComponent;
import li.cil.oc.api.network.SimpleComponent;
import mods.immibis.redlogic.api.wiring.IBundledEmitter;
import mods.immibis.redlogic.api.wiring.IBundledUpdatable;
import mods.immibis.redlogic.api.wiring.IBundledWire;
import mods.immibis.redlogic.api.wiring.IConnectable;
import mods.immibis.redlogic.api.wiring.IWire;
import moe.nightfall.vic.integratedcircuits.api.GateIOProvider;
import moe.nightfall.vic.integratedcircuits.api.IGatePeripheralProvider;
import moe.nightfall.vic.integratedcircuits.api.IGateRegistry;
import moe.nightfall.vic.integratedcircuits.api.ISocket;
import moe.nightfall.vic.integratedcircuits.api.ISocket.EnumConnectionType;
import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.gate.GatePeripheral;
import mrtjp.projectred.api.IBundledTile;
import mrtjp.projectred.transmission.BundledCablePart;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

import com.bluepowermod.api.wire.redstone.IBundledDevice;
import com.bluepowermod.api.wire.redstone.IBundledDeviceWrapper;

import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.common.Optional.Method;

public class GateIO
{
	public static void initialize() {
		IGateRegistry registry = IntegratedCircuitsAPI.getGateRegistry();
		registry.registerGateIOProvider(new GPProjectRed(), IntegratedCircuitsAPI.TILE);
		registry.registerGateIOProvider(new GPBluePower(), IntegratedCircuitsAPI.TILE);
		registry.registerGateIOProvider(new GPRedLogic(), IntegratedCircuitsAPI.TILE);
		registry.registerGateIOProvider(new GPOpenComputers(), IntegratedCircuitsAPI.TILE);
	}
	
	@Interface(iface = "mrtjp.projectred.api.IBundledTile", modid = "ProjRed|Core")
	public static class GPProjectRed extends GateIOProvider implements IBundledTile {
		
		@Override
		@Method(modid = "ProjRed|Core")
		public boolean canConnectBundled(int side) 
		{
			if((side & 6) == (socket.getSide() & 6)) return false;
			int rel = socket.getSideRel(side);
			
			//Dirty hack for P:R, will only return true if something can connect from that side
			//As there is no way to get the caller of this method, this will return true even
			//if the part connecting can't connect when a different part on the given side can.
			BlockCoord pos = socket.getPos().offset(side);
			TileEntity t = socket.getWorld().getTileEntity(pos.x, pos.y, pos.z);
			
			if(t instanceof TileMultipart)
			{
				TMultiPart mp = ((TileMultipart)t).partMap(socket.getSide());
				if(!(mp instanceof BundledCablePart)) return false;
			}
			
			return socket.getConnectionTypeAtSide(rel).isBundled();
		}

		@Override
		@Method(modid = "ProjRed|Core")
		public byte[] getBundledSignal(int dir)
		{
			return GateIO.getBundledSignal(socket, dir);
		}
	}
	
	@InterfaceList({
		@Interface(iface = "mods.immibis.redlogic.api.wiring.IBundledUpdatable", modid = "RedLogic"),
		@Interface(iface = "mods.immibis.redlogic.api.wiring.IBundledEmitter", modid = "RedLogic"),
		@Interface(iface = "mods.immibis.redlogic.api.wiring.IConnectable", modid = "RedLogic")
	})
	public static class GPRedLogic extends GateIOProvider implements IBundledUpdatable, IBundledEmitter, IConnectable {
		
		@Override
		@Method(modid = "RedLogic")
		public byte[] getBundledCableStrength(int blockFace, int toDirection) 
		{
			return GateIO.getBundledSignal(socket, toDirection);
		}

		@Override
		@Method(modid = "RedLogic")
		public void onBundledInputChanged() 
		{
			socket.updateInput();
		}
		
		@Override
		@Method(modid = "RedLogic")
		public boolean connects(IWire wire, int blockFace, int fromDirection) 
		{	
			if((fromDirection & 6) == (socket.getSide() & 6)) return false;
			int rel = socket.getSideRel(fromDirection);
			
			if(blockFace == -1) return false;
			EnumConnectionType type = socket.getConnectionTypeAtSide(rel);
			if(wire instanceof IBundledWire) return type.isBundled();
			else return type.isRedstone();
		}

		@Override
		@Method(modid = "RedLogic")
		public boolean connectsAroundCorner(IWire wire, int blockFace, int fromDirection) 
		{
			//TODO I could do something about this.
			return false;
		}
	}
	
	@InterfaceList({
		@Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers"),
		@Interface(iface = "li.cil.oc.api.network.SidedComponent", modid = "OpenComputers"),
		@Interface(iface = "li.cil.oc.api.network.ManagedPeripheral", modid = "OpenComputers")
	})
	public static class GPOpenComputers extends GateIOProvider implements SimpleComponent, SidedComponent, ManagedPeripheral {
		
		@Override
		@Method(modid = "OpenComputers")
		public boolean canConnectNode(ForgeDirection side) 
		{
			if(socket.getGate() instanceof IGatePeripheralProvider) 
			{
				IGatePeripheralProvider provider = (IGatePeripheralProvider)socket.getGate();
				return provider.hasPeripheral(side.ordinal());
			}
			return false;
		}

		@Override
		@Method(modid = "OpenComputers")
		public String getComponentName() 
		{
			if(socket.getGate() instanceof IGatePeripheralProvider) {
				IGatePeripheralProvider provider = (IGatePeripheralProvider)socket.getGate();
				GatePeripheral peripheral = provider.getPeripheral();
				return peripheral.getType();
			}
			return null;
		}

		@Override
		@Method(modid = "OpenComputers")
		public String[] methods() 
		{
			if(socket.getGate() instanceof IGatePeripheralProvider) {
				IGatePeripheralProvider provider = (IGatePeripheralProvider)socket.getGate();
				GatePeripheral peripheral = provider.getPeripheral();
				return peripheral.getMethodNames();
			}
			return null;
		}

		@Override
		@Method(modid = "OpenComputers")
		public Object[] invoke(String method, Context context, Arguments args) throws Exception 
		{
			if(socket.getGate() instanceof IGatePeripheralProvider) {
				IGatePeripheralProvider provider = (IGatePeripheralProvider)socket.getGate();
				GatePeripheral peripheral = provider.getPeripheral();
				return peripheral.callMethod(method, args.toArray());
			}
			return null;
		}
	}
	
	@Interface(iface = "com.bluepowermod.api.wire.redstone.IBundledDeviceWrapper", modid = "bluepower")
	public static class GPBluePower extends GateIOProvider implements IBundledDeviceWrapper {
		
		BPDevice bpDevice;
		
		@Override
		@Method(modid = "bluepower")
		public IBundledDevice getBundledDeviceOnSide(ForgeDirection side)
		{
			if(bpDevice == null) bpDevice = new BPDevice(socket);
			return bpDevice;
		}
		
	}
	
	public static byte[] getBundledSignal(ISocket socket, int dir) {
		
		if((dir & 6) == (socket.getSide() & 6)) return null;
		int rot = socket.getSideRel(dir);
		if(!socket.getConnectionTypeAtSide(rot).isBundled()) return null;
		return socket.getOutput()[rot];
	}
}
