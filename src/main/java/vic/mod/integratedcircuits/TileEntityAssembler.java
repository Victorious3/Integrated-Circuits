package vic.mod.integratedcircuits;

import net.minecraft.client.shader.Framebuffer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.Constants.NBT;
import vic.mod.integratedcircuits.client.TileEntityAssemblerRenderer;
import vic.mod.integratedcircuits.ic.CircuitData;
import vic.mod.integratedcircuits.net.PacketAssemblerChangeItem;
import vic.mod.integratedcircuits.net.PacketFloppyDisk;
import vic.mod.integratedcircuits.util.MiscUtils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityAssembler extends TileEntityBase implements IDiskDrive, ISidedInventory
{
	public int[][] refMatrix;
	
	@SideOnly(Side.CLIENT)
	public Framebuffer circuitFBO;
	public boolean isOccupied;
	
	public boolean[][] excMatrix;
	public CircuitData cdata;
	public int size;
	public ItemStack[] contents = new ItemStack[13];
	
	public LaserHelper laserHelper = new LaserHelper(this, 9);

	@Override
	public void updateEntity() 
	{
		if(worldObj.isRemote && circuitFBO == null) TileEntityAssemblerRenderer.scheduleFramebuffer(this);
		if(!worldObj.isRemote && refMatrix != null)
			laserHelper.update();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);
		NBTTagList slotList = compound.getTagList("contents", NBT.TAG_COMPOUND);
		for(int i = 0; i < 13; i++)
		{
			if(slotList.getCompoundTagAt(i).hasNoTags())
				contents[i] = null;
			else contents[i] = ItemStack.loadItemStackFromNBT(slotList.getCompoundTagAt(i));
		}
		
		loadMatrix(compound);
		excMatrix = new boolean[size][size];
		if(compound.hasKey("tmp"))
		{
			byte[] temp = compound.getByteArray("tmp");
			for(int i = 0; i < temp.length; i++)
				excMatrix[i / size][i % size] = temp[i] != 0;
		}
		
		laserHelper.readFromNBT(compound);
		
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && (getStackInSlot(1) != null || laserHelper.isRunning)) 
		{
			isOccupied = true;
			TileEntityAssemblerRenderer.scheduleFramebuffer(this);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound compound)
	{
		super.writeToNBT(compound);
		NBTTagList slotList = new NBTTagList();
		for(int i = 0; i < 13; i++)
		{
			slotList.appendTag(contents[i] != null ? 
				contents[i].writeToNBT(new NBTTagCompound()) : new NBTTagCompound());
		}
		compound.setTag("contents", slotList);
		
		saveMatrix(compound);
		if(excMatrix != null)
		{
			byte[] temp = new byte[size * size];
			for(int x = 0; x < size; x++)
				for(int y = 0; y < size; y++)
					temp[x + y * size] = (byte)(excMatrix[x][y] ? 1 : 0);
			compound.setByteArray("tmp", temp);
		}
		
		laserHelper.writeToNBT(compound);
	}
	
	private void loadMatrix(NBTTagCompound compound)
	{
		if(compound.hasKey("circuit"))
		{
			NBTTagCompound circuit = compound.getCompoundTag("circuit");
			cdata = CircuitData.readFromNBT(circuit);
			size = cdata.getSize();
			
			refMatrix = new int[size][size];
			
			NBTTagList idlist = circuit.getTagList("id", NBT.TAG_INT_ARRAY);
			for(int i = 0; i < idlist.tagCount(); i++)
				refMatrix[i] = idlist.func_150306_c(i);
		}
	}
	
	private void saveMatrix(NBTTagCompound compound)
	{
		if(refMatrix != null)
		{
			NBTTagCompound circuit = new NBTTagCompound();
			cdata.writeToNBT(circuit);
			compound.setTag("circuit", circuit);
		}
	}
	
	public void loadMatrixFromDisk()
	{
		if(getDisk() != null)
		{
			ItemStack stack = getDisk();
			NBTTagCompound comp = stack.getTagCompound();
			if(comp != null && comp.hasKey("circuit"))
				loadMatrix(comp);
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
			comp.setInteger("size", size);
			contents[1].setTagCompound(comp);
		}
		markDirty();
	}

	@Override
	public void invalidate() 
	{
		super.invalidate();
		if(worldObj.isRemote && circuitFBO != null) 
		{
			circuitFBO.deleteFramebuffer();
			TileEntityAssemblerRenderer.fboArray.remove(circuitFBO);
		}	
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
		ItemStack temp = getStackInSlot(id);
		ItemStack stack = null;
		if(contents[id] != null)
		{
			if(contents[id].stackSize <= amount)
			{
				stack = this.contents[id];
				contents[id] = null;
			}
			else
			{
				stack = contents[id].splitStack(amount);
				if(contents[id].stackSize == 0) contents[id] = null;
			}
			this.markDirty();
		}
		if(!ItemStack.areItemStacksEqual(temp, contents[id])) onSlotChange(id);
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int id) 
	{
		return getStackInSlot(id);
	}

	@Override
	public void setInventorySlotContents(int id, ItemStack stack) 
	{
		boolean change = !ItemStack.areItemStacksEqual(contents[id], stack);
		contents[id] = stack;
		if(change) onSlotChange(id);
		markDirty();
	}
	
	public void onSlotChange(int id)
	{
		if(worldObj.isRemote) return;
		if(id > 8 && id < 13) laserHelper.createLaser(id - 9, getStackInSlot(id));
		else if(id == 1) 
			IntegratedCircuits.networkWrapper.sendToDimension(new PacketAssemblerChangeItem(xCoord, yCoord, zCoord, getStackInSlot(id) != null), worldObj.provider.dimensionId);
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
		if(!worldObj.isRemote) IntegratedCircuits.networkWrapper.sendToDimension(new PacketFloppyDisk(xCoord, yCoord, zCoord, stack), worldObj.provider.dimensionId);
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
