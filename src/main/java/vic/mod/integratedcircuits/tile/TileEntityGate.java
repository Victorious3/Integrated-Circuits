package vic.mod.integratedcircuits.tile;

import java.io.ByteArrayOutputStream;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import vic.mod.integratedcircuits.gate.GateProvider;
import vic.mod.integratedcircuits.gate.GateProvider.IGateProvider;
import vic.mod.integratedcircuits.gate.PartGate;
import vic.mod.integratedcircuits.net.MCDataOutputImpl;
import vic.mod.integratedcircuits.net.PacketDataStream;
import vic.mod.integratedcircuits.proxy.CommonProxy;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;

public class TileEntityGate extends TileEntity implements IGateProvider
{
	private PartGate gate;
	public MCDataOutputImpl out;
	
	public TileEntityGate(PartGate part)
	{
		this.gate = part;
	}
	
	@Override
	public void markRender() 
	{
		worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
	}

	@Override
	public void updateEntity() 
	{
		if(!worldObj.isRemote && out != null)
			CommonProxy.networkWrapper.sendToDimension(new PacketDataStream(this), worldObj.provider.dimensionId);
		gate.update();
	}

	@Override
	public MCDataOutput getWriteStream(int disc) 
	{
		if(!worldObj.isRemote)
			return out = (MCDataOutputImpl)(new MCDataOutputImpl(new ByteArrayOutputStream()).writeByte(disc));
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
}
