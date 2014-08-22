package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.TileEntityPCBLayout;
import vic.mod.integratedcircuits.client.GuiPCBLayout;
import cpw.mods.fml.relauncher.Side;

public class PacketPCBUpdate extends PacketPCB<PacketPCBUpdate>
{
	private int[][][] matrix;
	
	public PacketPCBUpdate(){}
	
	public PacketPCBUpdate(int[][][] matrix, int tx, int ty, int tz)
	{
		super(tx, ty, tz);
		this.matrix = matrix;
	}
	
	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
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
		super.write(buffer);
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
		TileEntityPCBLayout te = (TileEntityPCBLayout)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(te != null) 
		{
			te.setMatrix(matrix);
			if(Minecraft.getMinecraft().currentScreen instanceof GuiPCBLayout)
				Minecraft.getMinecraft().currentScreen.initGui();
		}
	}
}
