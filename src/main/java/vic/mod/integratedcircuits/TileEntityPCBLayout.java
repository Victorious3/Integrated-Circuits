package vic.mod.integratedcircuits;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.net.PacketPCBUpdate;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public class TileEntityPCBLayout extends TileEntityBase implements ICircuit, IDiskDrive
{
	private int[][][] pcbMatrix;
	public String name = "NO_NAME";
	private ItemStack floppyStack;
	
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
					SubLogicPart.getPart(x, y, this).onTick();
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
		pcbMatrix = MiscUtils.readPCBMatrix(compound);
		NBTTagCompound stackCompound = compound.getCompoundTag("floppyStack");
		floppyStack = ItemStack.loadItemStackFromNBT(stackCompound);
		name = compound.getString("name");
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) 
	{
		super.writeToNBT(compound);
		MiscUtils.writePCBMatrix(compound, pcbMatrix);
		NBTTagCompound stackCompound = new NBTTagCompound();
		if(floppyStack != null) floppyStack.writeToNBT(stackCompound);
		compound.setTag("floppyStack", stackCompound);
		compound.setString("name", name);
	}
	
	@Override
	public int[][][] getMatrix() 
	{
		return pcbMatrix;
	}
	
	@Override
	public void setMatrix(int[][][] matrix) 
	{
		this.pcbMatrix = matrix;
	}

	@Override
	public boolean getInputFromSide(ForgeDirection dir, int frequency) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setOutputToSide(ForgeDirection dir, int frequency, boolean output) 
	{
		// TODO Auto-generated method stub
	}

	@Override
	public int getSizeInventory() 
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int id) 
	{
		return id == 0 ? floppyStack : null;
	}

	@Override
	public ItemStack decrStackSize(int id, int amount) 
	{
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int id) 
	{
		return getStackInSlot(id);
	}

	@Override
	public void setInventorySlotContents(int id, ItemStack stack) 
	{
		if(id == 0) floppyStack = stack;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		markDirty();
	}

	@Override
	public String getInventoryName() 
	{
		return null;
	}

	@Override
	public boolean hasCustomInventoryName() 
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit() 
	{
		return 1;
	}

	@Override
	public boolean isItemValidForSlot(int id, ItemStack stack) 
	{
		return id == 0 && (stack == null || stack.getItem() == null || stack.getItem() == IntegratedCircuits.itemFloppyDisk);
	}

	@Override
	public AxisAlignedBB getBoundingBox() 
	{
		return MiscUtils.getRotatedInstance(AxisAlignedBB.getBoundingBox(1 / 16F, 1 / 16F, -1 / 16F, 13 / 16F, 3 / 16F, 1 / 16F), rotation);
	}

	@Override
	public ItemStack getDisk() 
	{
		return getStackInSlot(0);
	}

	@Override
	public void setDisk(ItemStack stack) 
	{
		setInventorySlotContents(0, stack);
	}
}
