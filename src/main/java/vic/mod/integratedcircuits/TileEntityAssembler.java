package vic.mod.integratedcircuits;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.Constants.NBT;
import vic.mod.integratedcircuits.ic.CircuitData;
import vic.mod.integratedcircuits.util.MiscUtils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityAssembler extends TileEntityBase implements IDiskDrive, ISidedInventory
{
	public int[][] refMatrix;
	public int[][] matrix;
	public CircuitData cdata;
	public int size, con, tier;
	public ItemStack[] contents = new ItemStack[15];
	public String name;
	
	public LaserHelper laserHelper = new LaserHelper(this, 11);

	@Override
	public void updateEntity() 
	{
		if(!worldObj.isRemote && refMatrix != null)
			laserHelper.update();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);
		for(int i = 0; i < 14; i++)
		{
			if(compound.getCompoundTag("stack_" + i).hasNoTags())
				contents[i] = null;
			else contents[i] = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("stack_" + i));
		}
		if(compound.hasKey("circuit")) loadMatrix(compound);
		laserHelper.readFromNBT(compound);
		
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && (getStackInSlot(1) != null || laserHelper.isRunning)) 
		{
			updateRender();
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound compound)
	{
		super.writeToNBT(compound);
		for(int i = 0; i < 14; i++)
		{
			compound.setTag("stack_" + i, contents[i] != null ? 
				contents[i].writeToNBT(new NBTTagCompound()) : new NBTTagCompound());
		}
		if(refMatrix != null) saveMatrix(compound);
		laserHelper.writeToNBT(compound);
	}
	
	private void loadMatrix(NBTTagCompound compound)
	{
		NBTTagCompound circuit = compound.getCompoundTag("circuit");
		cdata = CircuitData.readFromNBT(circuit);
		size = cdata.getSize();
		name = compound.getString("name");
		con = compound.getInteger("con");
		tier = compound.getInteger("tier");
		
		refMatrix = new int[size][size];
		matrix = new int[size][size];
				
		NBTTagList idlist = circuit.getTagList("id", NBT.TAG_INT_ARRAY);
		for(int i = 0; i < idlist.tagCount(); i++)
			refMatrix[i] = idlist.func_150306_c(i);
		
		int[] temp = compound.getIntArray("tmp");
		for(int i = 0; i < temp.length; i++)
			matrix[i / size][i % size] = temp[i];
	}
	
	private void saveMatrix(NBTTagCompound compound)
	{
		NBTTagCompound circuit = new NBTTagCompound();
		cdata.writeToNBT(circuit);
		compound.setTag("circuit", circuit);
		compound.setString("name", name);
		compound.setInteger("tier", tier);
		compound.setInteger("con", con);
		
		int[] temp = new int[size * size];
		for(int x = 0; x < size; x++)
			for(int y = 0; y < size; y++)
				temp[x + y * size] = matrix[x][y];
		compound.setIntArray("tmp", temp);
	}
	
	public void loadMatrixFromDisk()
	{
		if(getDisk() != null)
		{
			ItemStack stack = getDisk();
			NBTTagCompound comp = stack.getTagCompound();
			if(comp != null && comp.hasKey("circuit"))
			{
				loadMatrix(comp);
				name = comp.getString("name");
				con = comp.getInteger("con");
				tier = comp.getInteger("tier");
			}
			else refMatrix = null;
		}
	}
	
	public void onCircuitFinished()
	{
		if(getStackInSlot(1) == null)
		{
			contents[1] = new ItemStack(IntegratedCircuits.itemPCB, 1, 1);
			NBTTagCompound comp = new NBTTagCompound();
			comp.setTag("circuit", cdata.writeToNBT(new NBTTagCompound()));
			comp.setString("name", name);
			comp.setInteger("size", size);
			comp.setInteger("tier", tier);
			comp.setInteger("con", con);
			contents[1].setTagCompound(comp);
		}
		markDirty();
	}
	
	@SideOnly(Side.CLIENT)
	public void updateRender()
	{
		if(refMatrix != null)
		{
			for(int x = 0; x < size; x++)
				for(int y = 0; y < size; y++)
					if(matrix[x][y] != 0) loadGateAt(x, y);
		}
	}
	
	@SideOnly(Side.CLIENT)
	/** Will draw a single gate to the vertex buffer **/
	public void loadGateAt(int x, int y)
	{
		//FIXME I don't know, 5 FPS might be bad so DO SOMETHING ABOUT THAT!!!
	}

	@Override
	public int getSizeInventory() 
	{
		return contents.length;
	}

	@Override
	public ItemStack getStackInSlot(int id) 
	{
		return contents[id];
	}

	@Override
	public ItemStack decrStackSize(int id, int amount) 
	{
		if(contents[id] != null)
		{
			if(contents[id].stackSize <= amount)
			{
				ItemStack stack = this.contents[id];
				contents[id] = null;
				this.markDirty();
				return stack;
			}
			else
			{
				ItemStack stack = contents[id].splitStack(amount);
				if(contents[id].stackSize == 0) contents[id] = null;
				this.markDirty();
				return stack;
			}
		}
		else return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int id) 
	{
		return getStackInSlot(id);
	}

	@Override
	public void setInventorySlotContents(int id, ItemStack stack) 
	{
		contents[id] = stack;
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
		return 64;
	}

	@Override
	public boolean isItemValidForSlot(int id, ItemStack stack) 
	{
		return false;
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
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		loadMatrixFromDisk();
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) 
	{
		// TODO Auto-generated method stub
		return false;
	}
}
