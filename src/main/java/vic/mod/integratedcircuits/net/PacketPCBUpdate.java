package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.CircuitData;
import vic.mod.integratedcircuits.TileEntityPCBLayout;
import cpw.mods.fml.relauncher.Side;

public class PacketPCBUpdate extends PacketPCB<PacketPCBUpdate>
{
	private CircuitData data;
	
	public PacketPCBUpdate(){}
	
	public PacketPCBUpdate(CircuitData data, int tx, int ty, int tz)
	{
		super(tx, ty, tz);
		this.data = data;
	}
	
	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
		data = CircuitData.readFromNBT(buffer.readNBTTagCompoundFromBuffer());	
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		buffer.writeNBTTagCompoundToBuffer(data.writeToNBTRaw(new NBTTagCompound()));
	}

	@Override
	public void process(EntityPlayer player, Side side) 
	{
		TileEntityPCBLayout te = (TileEntityPCBLayout)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		data.setParent(te);
		if(te != null) te.setCircuitData(data);
	}
}
