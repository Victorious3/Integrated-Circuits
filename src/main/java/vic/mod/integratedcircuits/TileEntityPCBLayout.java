package vic.mod.integratedcircuits;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.net.PacketPCBUpdate;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public class TileEntityPCBLayout extends TileEntityBase implements ICircuit, IDiskDrive
{
	public String name = "NO_NAME";
	private ItemStack floppyStack;
	private CircuitData circuitData;
	
	public void setup(int size)
	{
		circuitData = new CircuitData(size, this);
	}

	@Override
	public void updateEntity() 
	{
		//Update the matrix in case there is at least one player watching.
		super.updateEntity();
		if(!worldObj.isRemote && playersUsing > 0)
		{
			getCircuitData().updateMatrix();
			IntegratedCircuits.networkWrapper.sendToAllAround(new PacketPCBUpdate(getCircuitData(), xCoord, yCoord, zCoord), 
				new TargetPoint(worldObj.getWorldInfo().getVanillaDimension(), xCoord, yCoord, zCoord, 8));
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) 
	{
		super.readFromNBT(compound);
		circuitData = CircuitData.readFromNBT(compound.getCompoundTag("circuit"), this);
		NBTTagCompound stackCompound = compound.getCompoundTag("floppyStack");
		floppyStack = ItemStack.loadItemStackFromNBT(stackCompound);
		name = compound.getString("name");
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) 
	{
		super.writeToNBT(compound);
		compound.setTag("circuit", circuitData.writeToNBT(new NBTTagCompound()));
		NBTTagCompound stackCompound = new NBTTagCompound();
		if(floppyStack != null) floppyStack.writeToNBT(stackCompound);
		compound.setTag("floppyStack", stackCompound);
		compound.setString("name", name);
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

	@Override
	public CircuitData getCircuitData() 
	{
		return circuitData;
	}

	@Override
	public void setCircuitData(CircuitData data) 
	{
		this.circuitData = data;
	}
}
