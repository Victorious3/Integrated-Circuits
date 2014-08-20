package vic.mod.integratedcircuits;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.net.PacketPCBUpdate;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public class TileEntityPCBLayout extends TileEntity implements ICircuit
{
	public int[][][] pcbMatrix;
	public String name;
	int playersUsing;
	
	public void setup(int width, int height)
	{
		pcbMatrix = new int[2][width + 2][height + 2];
	}

	@Override
	public void updateEntity() 
	{
		//Update the matrix in case there is at least one player watching.
		super.updateEntity();
		if(!worldObj.isRemote && playersUsing > 0)
		{
			for(int x = 0; x < pcbMatrix[0].length; x++)
			{
				for(int y = 0; y < pcbMatrix[0][x].length; y++)
				{
					SubLogicPart.getPart(x, y, this).onUpdateTick();
				}
			}
			IntegratedCircuits.networkWrapper.sendToAllAround(new PacketPCBUpdate(getMatrix(), xCoord, yCoord, zCoord), 
				new TargetPoint(worldObj.getWorldInfo().getVanillaDimension(), xCoord, yCoord, zCoord, 8));
		}
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
	
	public void onContainerOpened()
	{
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 0, ++playersUsing);
	}
	
	public void onContainerClosed()
	{
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 0, --playersUsing);
	}

	@Override
	public boolean receiveClientEvent(int id, int par)
	{
		if(id == 0)
		{
			playersUsing = par;
			return true;
		}
		return false;
	}

	@Override
	public int[][][] getMatrix() 
	{
		return pcbMatrix;
	}

	@Override
	public boolean getInputFromSide(ForgeDirection dir, int frequency) 
	{
		return false;
	}

	@Override
	public void setOutputToSide(ForgeDirection dir, int frequency, boolean output) 
	{
		
	}
}
