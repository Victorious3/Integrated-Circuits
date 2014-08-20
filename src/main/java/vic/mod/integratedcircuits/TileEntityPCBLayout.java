package vic.mod.integratedcircuits;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityPCBLayout extends TileEntity
{
	public int[][][] pcbMatrix;
	public String name;
	
	public void setup(int width, int height)
	{
		pcbMatrix = new int[2][width + 2][height + 2];
	}

	@Override
	public void updateEntity() 
	{
		//Update the matrix in case there is at least one player watching.
		super.updateEntity();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) 
	{
		super.readFromNBT(compound);
		pcbMatrix = Misc.readPCBMatrix(compound);
		name = compound.getString("name");
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) 
	{
		super.writeToNBT(compound);
		Misc.writePCBMatrix(compound, pcbMatrix);
		compound.setString("name", name);
	}

	@Override
	public Packet getDescriptionPacket() 
	{
		NBTTagCompound compound = new NBTTagCompound();
		this.writeToNBT(compound);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, compound);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) 
	{
		NBTTagCompound compound = pkt.func_148857_g();
	}
}
