package vic.mod.integratedcircuits;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.CircuitData;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.net.PacketPCBChangeInput;
import vic.mod.integratedcircuits.net.PacketPCBUpdate;
import vic.mod.integratedcircuits.util.MiscUtils;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityPCBLayout extends TileEntityBase implements ICircuit, IDiskDrive
{
	public String name = "NO_NAME";
	private ItemStack floppyStack;
	private CircuitData circuitData;
	
	//Used for the GUI, client side.
	@SideOnly(Side.CLIENT)
	public float scale = 0.33F;
	@SideOnly(Side.CLIENT)
	public double offX = 63;
	@SideOnly(Side.CLIENT)
	public double offY = 145;
	
	public int[] i = new int[4];
	public int[] o = new int[4];
	
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
			if(getCircuitData().checkUpdate())
			{
				IntegratedCircuits.networkWrapper.sendToAllAround(new PacketPCBUpdate(getCircuitData(), xCoord, yCoord, zCoord), 
					new TargetPoint(worldObj.getWorldInfo().getVanillaDimension(), xCoord, yCoord, zCoord, 8));
			}		
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
		return (i[MiscUtils.getSide(dir)] & 1 << frequency) != 0;
	}
	
	public boolean getOutputToSide(ForgeDirection dir, int frequency) 
	{
		return (o[MiscUtils.getSide(dir)] & 1 << frequency) != 0;
	}
	
	@SideOnly(Side.CLIENT)
	public void setInputFromSide(ForgeDirection dir, int frequency, boolean output) 
	{
		int[] i = this.i.clone();
		if(output) i[MiscUtils.getSide(dir)] |= 1 << frequency;
		else i[MiscUtils.getSide(dir)] &= ~(1 << frequency);
		
		IntegratedCircuits.networkWrapper.sendToServer(new PacketPCBChangeInput(true, i, xCoord, yCoord, zCoord));
	}

	@Override
	public void setOutputToSide(ForgeDirection dir, int frequency, boolean output) 
	{
		if(output) o[MiscUtils.getSide(dir)] |= 1 << frequency;
		else o[MiscUtils.getSide(dir)] &= ~(1 << frequency);
		
		IntegratedCircuits.networkWrapper.sendToAllAround(new PacketPCBChangeInput(false, o, xCoord, yCoord, zCoord), 
			new TargetPoint(worldObj.getWorldInfo().getVanillaDimension(), xCoord, yCoord, zCoord, 8));
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
