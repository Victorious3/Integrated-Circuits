package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.TileEntityPCBLayout;
import vic.mod.integratedcircuits.ic.CircuitData;
import cpw.mods.fml.relauncher.Side;

public class PacketPCBLoad extends PacketPCB<PacketPCBLoad>
{
	private CircuitData data;
	
	public PacketPCBLoad() {}
	
	/** Used by the Disk IO to send the complete CircuitData to the client **/
	public PacketPCBLoad(CircuitData data, int xCoord, int yCoord, int zCoord)
	{
		super(xCoord, yCoord, zCoord);
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
		buffer.writeNBTTagCompoundToBuffer(data.writeToNBT(new NBTTagCompound()));		
	}

	@Override
	public void process(EntityPlayer player, Side side) 
	{
		TileEntityPCBLayout layout = (TileEntityPCBLayout)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(layout != null)
		{
			data.setParent(layout);
			layout.setCircuitData(data);
		}
	}
}
