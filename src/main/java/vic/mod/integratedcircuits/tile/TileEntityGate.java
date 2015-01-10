package vic.mod.integratedcircuits.tile;

import io.netty.buffer.Unpooled;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.gate.GateProvider;
import vic.mod.integratedcircuits.gate.GateProvider.IGateProvider;
import vic.mod.integratedcircuits.gate.GateRegistry;
import vic.mod.integratedcircuits.gate.PartGate;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.vec.BlockCoord;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public class TileEntityGate extends TileEntity implements IGateProvider
{
	private PartGate gate;
	
	public TileEntityGate() {}
	
	public TileEntityGate(PartGate part)
	{
		this.gate = part;
		part.setProvider(this);
	}
	
	@Override
	public void markRender() 
	{
		worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
	}

	@Override
	public void updateEntity() 
	{
		gate.update();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) 
	{
		super.readFromNBT(compound);
		gate = GateRegistry.createGateInstace(compound.getString("gate_id"));
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
		comp.setByteArray("data", packet.getByteBuf().array());
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, blockMetadata, comp);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) 
	{
		NBTTagCompound comp = pkt.func_148857_g();
		byte[] data = comp.getByteArray("data");
		PacketCustom in = new PacketCustom(Unpooled.copiedBuffer(data));
		gate.readDesc(in);
	}

	@Override
	public MCDataOutput getWriteStream(int disc) 
	{
		if(!worldObj.isRemote)
			return IntegratedCircuits.proxy.addStream(new TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 0)).writeByte(disc);
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
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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
}
