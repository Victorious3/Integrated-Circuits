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
import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.GateIOProvider;
import moe.nightfall.vic.integratedcircuits.api.gate.IGatePeripheralProvider;
import moe.nightfall.vic.integratedcircuits.api.gate.IGateRegistry;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket.EnumConnectionType;
import moe.nightfall.vic.integratedcircuits.gate.GatePeripheral;
import moe.nightfall.vic.integratedcircuits.tile.TileEntitySocket;
import mrtjp.projectred.api.IBundledTile;
import mrtjp.projectred.transmission.BundledCablePart;
import mrtjp.projectred.transmission.IRedwireEmitter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import powercrystals.minefactoryreloaded.api.rednet.IRedNetOmniNode;
import powercrystals.minefactoryreloaded.api.rednet.connectivity.RedNetConnectionType;
import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

import com.bluepowermod.api.wire.redstone.IBundledDevice;
import com.bluepowermod.api.wire.redstone.IBundledDeviceWrapper;

import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.common.Optional.Method;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.api.redstone.IBundledRedstoneProvider;

public class GateIO
{
	public static void initialize() {
		IGateRegistry registry = IntegratedCircuitsAPI.getGateRegistry();
		
		registry.registerGateIOProvider(new GPProjectRed(), IntegratedCircuitsAPI.TILE);
		registry.registerGateIOProvider(new GPProjectRedFMP(), IntegratedCircuitsAPI.TILE_FMP);
		registry.registerGateIOProvider(new GPBluePower(), IntegratedCircuitsAPI.TILE, IntegratedCircuitsAPI.TILE_FMP);
		registry.registerGateIOProvider(new GPRedLogic(), IntegratedCircuitsAPI.TILE);
		registry.registerGateIOProvider(new GPOpenComputers(), IntegratedCircuitsAPI.TILE);
		registry.registerGateIOProvider(new GPMinefactoryReloaded(), IntegratedCircuitsAPI.BLOCK);
		registry.registerGateIOProvider(new GPComputerCraft(), IntegratedCircuitsAPI.BLOCK);
	}
	
	@InterfaceList({
		@Interface(iface = "dan200.computercraft.api.redstone.IBundledRedstoneProvider", modid = "ComputerCraft"),
		@Interface(iface = "dan200.computercraft.api.peripheral.IPeripheralProvider", modid = "ComputerCraft")
	})
	public static class GPComputerCraft extends GateIOProvider implements IBundledRedstoneProvider, IPeripheralProvider {
		
		@Override
		@Method(modid = "ComputerCraft")
		public int getBundledRedstoneOutput(World world, int x, int y, int z, int side) 
		{
			TileEntitySocket te = (TileEntitySocket)world.getTileEntity(x, y, z);
			ISocket socket = te.getSocket();
			
			if((side & 6) == (socket.getSide() & 6)) return -1;
			int rel = socket.getSideRel(side);
			
			//convert analog to digital
			int out = 0;
			for(int i = 0; i < 16; i++)
				out |= (socket.getBundledOutput(side, i) != 0 ? 1 : 0) << i;
			return out;
		}
		
		@Override
		@Method(modid = "ComputerCraft")
		public IPeripheral getPeripheral(World world, int x, int y, int z, int side) 
		{
			ISocket socket = ((TileEntitySocket)world.getTileEntity(x, y, z)).getSocket();
			if(socket.getGate() instanceof IGatePeripheralProvider)
			{
				IGatePeripheralProvider provider = (IGatePeripheralProvider)socket.getGate();
				return provider.hasPeripheral(side) ? provider.getPeripheral() : null;
			}
			return null;
		}
	}
	
	@Interface(iface = "powercrystals.minefactoryreloaded.api.rednet.IRedNetOmniNode", modid = "MineFactoryReloaded")
	public static class GPMinefactoryReloaded extends GateIOProvider implements IRedNetOmniNode {

		@Override
		@Method(modid = "MineFactoryReloaded")
		public RedNetConnectionType getConnectionType(World world, int x, int y, int z, ForgeDirection fd) 
		{
			TileEntitySocket te = (TileEntitySocket)world.getTileEntity(x, y, z);
			ISocket socket = te.getSocket();
			
			int side = fd.ordinal();
			if((side & 6) == (socket.getSide() & 6)) return RedNetConnectionType.None;
			int rel = socket.getSideRel(side);
			
			EnumConnectionType type = socket.getConnectionTypeAtSide(rel);
			if(type.isBundled()) return RedNetConnectionType.PlateAll;
			else if(type.isRedstone()) return RedNetConnectionType.PlateSingle;
			return RedNetConnectionType.None;
		}
		
		@Override
		@Method(modid = "MineFactoryReloaded")
		public void onInputsChanged(World world, int x, int y, int z, ForgeDirection fd, int[] inputValues) 
		{
			TileEntitySocket te = (TileEntitySocket)world.getTileEntity(x, y, z);
			ISocket socket = te.getSocket();
			
			int side = fd.ordinal();
			if((side & 6) == (socket.getSide() & 6)) return;
			int rel = socket.getSideRel(side);
			
			socket.updateInputPre();
			for(int i = 0; i < 16; i++)
				socket.setInput(rel, i, (byte)(inputValues[i] & 0xFF));
			socket.updateInputPost();	
		}

		@Override
		@Method(modid = "MineFactoryReloaded")
		public void onInputChanged(World world, int x, int y, int z, ForgeDirection fd, int inputValue) 
		{
			TileEntitySocket te = (TileEntitySocket)world.getTileEntity(x, y, z);
			ISocket socket = te.getSocket();
			
			int side = fd.ordinal();
			if((side & 6) == (socket.getSide() & 6)) return;
			int rel = socket.getSideRel(side);
			
			socket.updateInputPre();
			socket.setInput(rel, 0, (byte)(inputValue & 0xFF));
			socket.updateInputPost();
		}

		@Override
		@Method(modid = "MineFactoryReloaded")
		public int[] getOutputValues(World world, int x, int y, int z, ForgeDirection fd) 
		{		
			TileEntitySocket te = (TileEntitySocket)world.getTileEntity(x, y, z);
			ISocket socket = te.getSocket();
			
			int side = fd.ordinal();
			if((side & 6) == (socket.getSide() & 6)) return new int[16];
			int rel = socket.getSideRel(side);
			
			//Convert byte array output to int array, just for you MFR
			int[] out = new int[16];
			byte[] bout = socket.getOutput()[rel];
			for(int i = 0; i < 16; i++)
				out[i] = bout[i] & 255;
			
			return out;
		}

		@Override
		@Method(modid = "MineFactoryReloaded")
		public int getOutputValue(World world, int x, int y, int z, ForgeDirection fd, int subnet) 
		{
			TileEntitySocket te = (TileEntitySocket)world.getTileEntity(x, y, z);
			ISocket socket = te.getSocket();
			
			int side = fd.ordinal() ^ 1;
			if((side & 6) == (socket.getSide() & 6)) return 0;
			int rel = socket.getSideRel(side);
			
			return socket.getRedstoneOutput(rel);
		}
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
		@Interface(iface = "mrtjp.projectred.api.IBundledEmitter", modid = "ProjRed|Core"),
		@Interface(iface = "mrtjp.projectred.api.IConnectable", modid = "ProjRed|Core"),
	})
	public static class GPProjectRedFMP extends GateIOProvider implements mrtjp.projectred.api.IBundledEmitter, mrtjp.projectred.api.IConnectable {
		
		@Override
		public boolean canConnectCorner(int arg0) 
		{
			return false;
		}

		@Override
		public boolean connectCorner(mrtjp.projectred.api.IConnectable arg0, int arg1, int arg2) 
		{
			return connectStraight(arg0, arg1, arg2);
		}

		@Override
		public boolean connectInternal(mrtjp.projectred.api.IConnectable arg0, int arg1) 
		{
			return connectStraight(arg0, arg1, 0);
		}

		@Override
		public boolean connectStraight(mrtjp.projectred.api.IConnectable arg0, int arg1, int arg2) 
		{
			int side = socket.getRotationRel(arg1);
			EnumConnectionType type = socket.getConnectionTypeAtSide(side);
			if(arg0 instanceof IRedwireEmitter && type.isRedstone()) return true;
			else if(arg0 instanceof IBundledEmitter && type.isBundled()) return true;
			return false;
		}
		
		@Override
		public byte[] getBundledSignal(int arg0) 
		{
			int rot = socket.getRotationRel(arg0);
			EnumConnectionType type = socket.getConnectionTypeAtSide(rot);
			if(!type.isBundled()) return null;
			return socket.getOutput()[rot];
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
		
		private BPDevice bpDevice;
		
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
