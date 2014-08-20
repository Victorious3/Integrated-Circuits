package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.SubLogicPart;
import vic.mod.integratedcircuits.TileEntityPCBLayout;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;

public class PacketPCBChangePart extends PacketPCB<PacketPCBChangePart>
{
	private int x, y, id, data;
	
	public PacketPCBChangePart(){}
	
	public PacketPCBChangePart(int x, int y, int id, int data, int tx, int ty, int tz)
	{
		super(tx, ty, tz);
		this.x = x; this.y = y; 
		this.id = id; 
		this.data = data; 
	}
	
	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
		x = buffer.readInt();
		y = buffer.readInt();
		id = buffer.readInt();
		data = buffer.readInt();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		buffer.writeInt(x);
		buffer.writeInt(y);
		buffer.writeInt(id);
		buffer.writeInt(data);
	}

	@Override
	public void process(EntityPlayer player, Side side) 
	{
		TileEntityPCBLayout te = (TileEntityPCBLayout)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(te != null)
		{
			te.pcbMatrix[0][x][y] = id;
			te.pcbMatrix[1][x][y] = data;
			if(side == Side.SERVER)
			{
				SubLogicPart.getPart(x, y, te).onPlaced();
				IntegratedCircuits.networkWrapper.sendToAllAround(new PacketPCBUpdate(te.pcbMatrix, xCoord, yCoord, zCoord), 
					new TargetPoint(te.getWorldObj().getWorldInfo().getVanillaDimension(), xCoord, yCoord, zCoord, 8));
			}
		}
	}
}
