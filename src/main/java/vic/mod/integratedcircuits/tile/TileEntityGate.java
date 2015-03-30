package vic.mod.integratedcircuits.tile;

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
import mrtjp.projectred.api.IBundledTile;
import mrtjp.projectred.transmission.BundledCablePart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.gate.BPDevice;
import vic.mod.integratedcircuits.gate.GatePeripheral;
import vic.mod.integratedcircuits.gate.IGatePeripheralProvider;
import vic.mod.integratedcircuits.gate.ISocket.EnumConnectionType;
import vic.mod.integratedcircuits.gate.ISocketWrapper;
import vic.mod.integratedcircuits.gate.Socket;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

import com.bluepowermod.api.wire.redstone.IBundledDevice;
import com.bluepowermod.api.wire.redstone.IBundledDeviceWrapper;

import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.common.Optional.Method;

@InterfaceList({
	@Interface(iface = "mrtjp.projectred.api.IBundledTile", modid = "ProjRed|Core"),
	@Interface(iface = "mods.immibis.redlogic.api.wiring.IBundledUpdatable", modid = "RedLogic"),
	@Interface(iface = "mods.immibis.redlogic.api.wiring.IBundledEmitter", modid = "RedLogic"),
	@Interface(iface = "mods.immibis.redlogic.api.wiring.IConnectable", modid = "RedLogic"),
	@Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers"),
	@Interface(iface = "li.cil.oc.api.network.SidedComponent", modid = "OpenComputers"),
	@Interface(iface = "li.cil.oc.api.network.ManagedPeripheral", modid = "OpenComputers"),
	@Interface(iface = "com.bluepowermod.api.wire.redstone.IBundledDeviceWrapper", modid = "bluepower")
})
public class TileEntityGate extends TileEntity implements 
	ISocketWrapper, IBundledTile, IBundledUpdatable, IBundledEmitter, 
	IConnectable, SimpleComponent, SidedComponent, ManagedPeripheral,
	IBundledDeviceWrapper
{
	public Socket socket = new Socket(this);
	
	// TODO Re-implement
	public BPDevice bpDevice;
	public boolean isDestroyed;
	
	@Override
	public void markRender() 
	{
		worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
	}

	@Override
	public void updateEntity() 
	{
		socket.update();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) 
	{
		super.readFromNBT(compound);
		socket.readFromNBT(compound);
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) 
	{
		super.writeToNBT(compound);
		socket.writeToNBT(compound);	
	}

	@Override
	public Packet getDescriptionPacket() 
	{
		NBTTagCompound comp = new NBTTagCompound();
		socket.writeDesc(comp);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, blockMetadata, comp);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) 
	{
		NBTTagCompound comp = pkt.func_148857_g();
		socket.readDesc(comp);
	}

	@Override
	public MCDataOutput getWriteStream(int disc) 
	{
		if(!worldObj.isRemote)
			return IntegratedCircuits.proxy.addStream(getWorld(), getPos()).writeByte(disc);
		throw new IllegalArgumentException("Cannot use getWriteStream on a client world");
	}

	@Override
	public World getWorld() 
	{
		return worldObj;
	}

	@Override
	public void notifyBlocksAndChanges() 
	{
		markDirty();
		notifyPartChange();
		BlockCoord pos = getPos().copy().offset(socket.getSide());
		worldObj.notifyBlocksOfNeighborChange(pos.x, pos.y, pos.z, getBlockType());
	}

	@Override
	public void notifyPartChange() 
	{
		worldObj.notifyBlockChange(xCoord, yCoord, zCoord, getBlockType());
	}

	@Override
	public BlockCoord getPos() 
	{
		return new BlockCoord(xCoord, yCoord, zCoord);
	}

	@Override
	public TileEntity getTileEntity() 
	{
		return this;
	}

	@Override
	public void destroy() 
	{
		isDestroyed = true;
		worldObj.setBlockToAir(xCoord, yCoord, zCoord);
	}

	@Override
	public byte[] updateBundledInput(int side)
	{
		// TODO implement
		return new byte[16];
	}
	
	@Override
	public int updateRedstoneInput(int side)
	{
		// TODO implement
		return 0;
	}

	@Override
	public void scheduleTick(int delay) 
	{
		worldObj.scheduleBlockUpdate(xCoord, yCoord, zCoord, getBlockType(), delay);
	}

	
	@Override
	public int strongPowerLevel(int side) 
	{
		return 0;
	}
	
	//ProjectRed
	
	@Override
	@Method(modid = "ProjRed|Core")
	public boolean canConnectBundled(int side) 
	{
		if((side & 6) == (socket.getSide() & 6)) return false;
		int rel = socket.getSideRel(side);
		
		//Dirty hack for P:R, will only return true if something can connect from that side
		//As there is no way to get the caller of this method, this will return true even
		//if the part connecting can't connect when a different part on the given side can.
		BlockCoord pos = getPos().offset(side);
		TileEntity t = worldObj.getTileEntity(pos.x, pos.y, pos.z);
		
		if(t instanceof TileMultipart)
		{
			TMultiPart mp = ((TileMultipart)t).partMap(socket.getSide());
			if(!(mp instanceof BundledCablePart)) return false;
		}
		
		return socket.getConnectionTypeAtSide(rel).isBundled();
	}
	
	@Override
	public byte[] getBundledSignal(int arg0) 
	{
		if((arg0 & 6) == (socket.getSide() & 6)) return null;
		int rot = socket.getSideRel(arg0);
		if(!socket.getConnectionTypeAtSide(rot).isBundled()) return null;
		return socket.getOutput()[rot];
	}
	
	//RedLogic
	
	@Override
	@Method(modid = "RedLogic")
	public byte[] getBundledCableStrength(int blockFace, int toDirection) 
	{
		return getBundledSignal(toDirection);
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
	
	//Open Computers
	
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

	@Override
	@Method(modid = "bluepower")
	public IBundledDevice getBundledDeviceOnSide(ForgeDirection side)
	{
		return bpDevice;
	}

	@Override
	public void updateInput()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Socket getSocket()
	{
		return socket;
	}
}