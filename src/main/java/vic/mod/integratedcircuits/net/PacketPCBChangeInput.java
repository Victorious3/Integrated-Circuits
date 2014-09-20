package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.TileEntityPCBLayout;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;

public class PacketPCBChangeInput extends PacketTileEntity<PacketPCBChangeInput>
{
	private boolean input;
	private int[] io;
	private int con;
	
	public PacketPCBChangeInput() {}
	
	public PacketPCBChangeInput(boolean input, int[] io, int con, int xCoord, int yCoord, int zCoord)
	{
		super(xCoord, yCoord, zCoord);
		this.io = io;
		this.input = input;
		this.con = con;
	}
	
	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
		input = buffer.readBoolean();
		con = buffer.readInt();
		io = new int[4];
		io[0] = buffer.readInt();
		io[1] = buffer.readInt();
		io[2] = buffer.readInt();
		io[3] = buffer.readInt();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		buffer.writeBoolean(input);
		buffer.writeInt(con);
		buffer.writeInt(io[0]);
		buffer.writeInt(io[1]);
		buffer.writeInt(io[2]);
		buffer.writeInt(io[3]);
	}

	@Override
	public void process(EntityPlayer player, Side side) 
	{
		TileEntityPCBLayout te = (TileEntityPCBLayout)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(te == null) return;
		if(input) te.i = io;
		else te.o = io;
		te.con = con;
		if(input && side == Side.SERVER)
		{
			te.getCircuitData().updateInput();
			IntegratedCircuits.networkWrapper.sendToAllAround(this, 
				new TargetPoint(te.getWorldObj().getWorldInfo().getVanillaDimension(), xCoord, yCoord, zCoord, 8));
		}	
	}
}
