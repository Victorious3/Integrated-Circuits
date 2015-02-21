package vic.mod.integratedcircuits.tile;

import io.netty.buffer.Unpooled;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.gate.GatePeripheral;
import vic.mod.integratedcircuits.gate.GateProvider;
import vic.mod.integratedcircuits.gate.GateProvider.IGateProvider;
import vic.mod.integratedcircuits.gate.GateRegistry;
import vic.mod.integratedcircuits.gate.IGatePeripheralProvider;
import vic.mod.integratedcircuits.gate.PartGate;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
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
	@Interface(iface = "li.cil.oc.api.network.ManagedPeripheral", modid = "OpenComputers")
})
public class TileEntityGate extends TileEntity implements IGateProvider, IBundledTile, IBundledUpdatable, IBundledEmitter, IConnectable, SimpleComponent, SidedComponent, ManagedPeripheral
{
	public PartGate gate;
	
	@Override
	public void markRender() 
	{
		worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
	}

	@Override
	public void updateEntity() 
	{
		if(gate != null) gate.update();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) 
	{
		super.readFromNBT(compound);
		gate = GateRegistry.createGateInstace(compound.getString("gate_id"));
		gate.setProvider(this);
		gate.load(compound);
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) 
	{
		super.writeToNBT(compound);
		compound.setString("gate_id", gate.getName());
		gate.save(compound);
	}

	@Override
	public Packet getDescriptionPacket() 
	{
		NBTTagCompound comp = new NBTTagCompound();
		PacketCustom packet = new PacketCustom("", 1);
		gate.writeDesc(packet);
		comp.setString("gate_id", gate.getName());
		comp.setByteArray("data", packet.getByteBuf().array());
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, blockMetadata, comp);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) 
	{
		NBTTagCompound comp = pkt.func_148857_g();
		byte[] data = comp.getByteArray("data");
		PacketCustom in = new PacketCustom(Unpooled.copiedBuffer(data));
		if(gate == null) 
		{
			gate = GateRegistry.createGateInstace(comp.getString("gate_id"));
			gate.setProvider(this);
		}
		gate.readDesc(in);
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
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
	}

	@Override
	public void notifyPartChange() {}

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
		worldObj.setBlockToAir(xCoord, yCoord, zCoord);
	}

	@Override
	public byte[] updateBundledInput(int side)
	{
		return GateProvider.calculateBundledInput(this, side);
	}
	
	@Override
	public int updateRedstoneInput(int side)
	{
		return GateProvider.calculateRedstoneInput(this, side);
	}

	@Override
	public void scheduleTick(int delay) 
	{
		worldObj.scheduleBlockUpdate(xCoord, yCoord, zCoord, getBlockType(), delay);
	}

	@Override
	public PartGate getGate() 
	{
		return gate;
	}
	
	//ProjectRed
	
	@Override
	public boolean canConnectBundled(int side) 
	{
		if((side & 6) == (gate.getSide() & 6)) return false;
		int rel = gate.getSideRel(side);
		
		//Dirty hack for P:R, will only return true if something can connect from that side
		//As there is no way to get the caller of this method, this will return true even
		//if the part connecting can't connect when a different part on the given side can.
		BlockCoord pos = getPos().offset(side);
		TileEntity t = worldObj.getTileEntity(pos.x, pos.y, pos.z);
		
		if(t instanceof TileMultipart)
		{
			TMultiPart mp = ((TileMultipart)t).partMap(gate.getSide());
			if(!(mp instanceof BundledCablePart)) return false;
		}
		
		return gate.canConnectBundledImpl(rel);
	}
	
	@Override
	public byte[] getBundledSignal(int arg0) 
	{
		if((arg0 & 6) == (gate.getSide() & 6)) return null;
		int rot = gate.getSideRel(arg0);
		if(!gate.canConnectBundledImpl(rot)) return null;
		return gate.getBundledOutput(rot);
	}
	
	//RedLogic
	
	@Override
	public byte[] getBundledCableStrength(int blockFace, int toDirection) 
	{
		return getBundledSignal(toDirection);
	}

	@Override
	public void onBundledInputChanged() 
	{
		gate.updateInput();
	}
	
	@Override
	public boolean connects(IWire wire, int blockFace, int fromDirection) 
	{	
		if((fromDirection & 6) == (gate.getSide() & 6)) return false;
		int rel = gate.getSideRel(fromDirection);
		
		if(blockFace == -1) return false;
		if(wire instanceof IBundledWire) return gate.canConnectBundledImpl(rel);
		else return gate.canConnectRedstoneImpl(rel);
	}

	@Override
	public boolean connectsAroundCorner(IWire wire, int blockFace, int fromDirection) 
	{
		//TODO I could do something about this.
		return false;
	}
	
	//---

	@Override
	public int strongPowerLevel(int side) 
	{
		return 0;
	}

	@Override
	public ItemStack getItemStack() 
	{
		return gate.getItemStack(gate.getItemType().getItem());
	}

	@Override
	public boolean isMultipart() 
	{
		return false;
	}
	
	//Open Computers
	
	@Override
	public boolean canConnectNode(ForgeDirection side) 
	{
		if(getGate() instanceof IGatePeripheralProvider) {
			IGatePeripheralProvider provider = (IGatePeripheralProvider)getGate();
			return provider.hasPeripheral(side.ordinal());
		}
		return false;
	}

	@Override
	public String getComponentName() 
	{
		if(getGate() instanceof IGatePeripheralProvider) {
			IGatePeripheralProvider provider = (IGatePeripheralProvider)getGate();
			GatePeripheral peripheral = provider.getPeripheral();
			return peripheral.getType();
		}
		return null;
	}

	@Override
	public String[] methods() 
	{
		if(getGate() instanceof IGatePeripheralProvider) {
			IGatePeripheralProvider provider = (IGatePeripheralProvider)getGate();
			GatePeripheral peripheral = provider.getPeripheral();
			return peripheral.getMethodNames();
		}
		return null;
	}

	@Override
	@Method(modid = "OpenComputers")
	public Object[] invoke(String method, Context context, Arguments args) throws Exception 
	{
		if(getGate() instanceof IGatePeripheralProvider) {
			IGatePeripheralProvider provider = (IGatePeripheralProvider)getGate();
			GatePeripheral peripheral = provider.getPeripheral();
			return peripheral.callMethod(method, args.toArray());
		}
		return null;
	}
}
