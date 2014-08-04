package vic.mod.integratedcircuits;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityPCBLayout extends TileEntity
{
	public int[][][] pcbMatrix;
	
	public void setup(int width, int height)
	{
		pcbMatrix = new int[2][width + 2][height + 2];
	}	

	@Override
	public void readFromNBT(NBTTagCompound compound) 
	{
		super.readFromNBT(compound);
		pcbMatrix = Misc.readPCBMatrix(compound);
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) 
	{
		super.writeToNBT(compound);
		Misc.writePCBMatrix(compound, pcbMatrix);
	}
}
