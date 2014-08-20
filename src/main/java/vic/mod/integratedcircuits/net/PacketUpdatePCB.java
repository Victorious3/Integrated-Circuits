package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.TileEntityPCBLayout;
import cpw.mods.fml.relauncher.Side;

public class PacketUpdatePCB extends AbstractPacket<PacketUpdatePCB>
{
	private int x, y, z;
	private int[][][] matrix;
	
	public PacketUpdatePCB(){}
	
	public PacketUpdatePCB(int[][][] matrix, int x, int y, int z)
	{
		this.x = x; this.y = y; this.z = z;
		this.matrix = matrix;
	}
	
	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		this.x = buffer.readInt();
		this.y = buffer.readInt();
		this.z = buffer.readInt();
		
		byte size = buffer.readByte();
		matrix = new int[2][size][size];
		
		for(int i = 0; i < size * size; i++)
			matrix[0][i - i / size * size][i / size] = buffer.readInt();
		for(int i = 0; i < size * size; i++)
			matrix[1][i - i / size * size][i / size] = buffer.readInt();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		buffer.writeInt(x);
		buffer.writeInt(y);
		buffer.writeInt(z);
		
		byte size = (byte)matrix[0].length;
		buffer.writeByte(size);
		for(int i = 0; i < size * size; i++)
			buffer.writeInt(matrix[0][i - i / size * size][i / size]);
		for(int i = 0; i < size * size; i++)
			buffer.writeInt(matrix[1][i - i / size * size][i / size]);
	}

	@Override
	public void process(EntityPlayer player, Side side) 
	{
		TileEntityPCBLayout te = (TileEntityPCBLayout)player.worldObj.getTileEntity(x, y, z);
		if(te != null) te.pcbMatrix = matrix;
	}
}
