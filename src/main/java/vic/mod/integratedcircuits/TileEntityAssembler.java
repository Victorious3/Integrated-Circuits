package vic.mod.integratedcircuits;

import net.minecraft.client.shader.Framebuffer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.Constants.NBT;
import vic.mod.integratedcircuits.client.TileEntityAssemblerRenderer;
import vic.mod.integratedcircuits.util.MiscUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityAssembler extends TileEntityBase implements IDiskDrive, ISidedInventory
{
	@SideOnly(Side.CLIENT)
	public Framebuffer circuitFBO;
	
	public int[][] matrix;
	public int size;
	public ItemStack[] contents = new ItemStack[11];

	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);
		for(int i = 0; i < contents.length; i++)
		{
			if(compound.getCompoundTag("stack_" + i).hasNoTags())
				contents[i] = null;
			else contents[i] = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("stack_" + i));
		}
		if(worldObj.isRemote) 
		{
			loadMatrix();
			TileEntityAssemblerRenderer.updateFramebuffer(this);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound compound)
	{
		super.writeToNBT(compound);
		for(int i = 0; i < contents.length; i++)
		{
			compound.setTag("stack_" + i, contents[i] != null ? contents[i].writeToNBT(new NBTTagCompound()) : new NBTTagCompound());
		}
	}
	
	public void loadMatrix()
	{
		if(getDisk() == null) matrix = null;
		else
		{
			ItemStack stack = getDisk();
			if(stack.getTagCompound() != null && stack.getTagCompound().hasKey("circuit"))
			{
				NBTTagCompound comp = stack.getTagCompound();
				size = comp.getInteger("size");
				matrix = new int[size][size];
				NBTTagCompound circuit = comp.getCompoundTag("circuit");
				
				NBTTagList idlist = circuit.getTagList("id", NBT.TAG_INT_ARRAY);
				for(int i = 0; i < idlist.tagCount(); i++)
				{
					matrix[i] = idlist.func_150306_c(i);
				}
			}
			else matrix = null;
		}
	}

	@Override
	public void updateEntity() 
	{
		if(worldObj.isRemote && circuitFBO == null) TileEntityAssemblerRenderer.updateFramebuffer(this);
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
