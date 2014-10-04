package vic.mod.integratedcircuits;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.Constants.NBT;
import vic.mod.integratedcircuits.LaserHelper.Laser;
import vic.mod.integratedcircuits.ic.CircuitPart;
import vic.mod.integratedcircuits.util.MiscUtils;
import vic.mod.integratedcircuits.util.RenderUtils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityAssembler extends TileEntityBase implements IDiskDrive, ISidedInventory
{
	public int[][] refMatrix;
	public int[][] matrix;
	public int size;
	public ItemStack[] contents = new ItemStack[11];
	
	@SideOnly(Side.CLIENT)
	public Tessellator verts;
	
	public LaserHelper laserHelper = new LaserHelper(this);

	@Override
	public void updateEntity() 
	{
		if(!worldObj.isRemote && refMatrix != null)
		{
			for(int i = 0; i < 4; i++)
			{
				Laser laser = laserHelper.getLaser(i);
				if(laser == null || laser.isDone) continue;
				laser.update(0);
				if(laser.canUpdate()) laser.findNext();
			}
		}
	}

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
		loadMatrix();
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
		if(getDisk() == null) refMatrix = null;
		else
		{
			ItemStack stack = getDisk();
			if(stack.getTagCompound() != null && stack.getTagCompound().hasKey("circuit"))
			{
				NBTTagCompound comp = stack.getTagCompound();
				size = comp.getInteger("size");
				
				refMatrix = new int[size][size];
				matrix = new int[size][size];
				
				NBTTagCompound circuit = comp.getCompoundTag("circuit");
				
				NBTTagList idlist = circuit.getTagList("id", NBT.TAG_INT_ARRAY);
				for(int i = 0; i < idlist.tagCount(); i++)
				{
					refMatrix[i] = idlist.func_150306_c(i);
				}
			}
			else refMatrix = null;
		}
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			verts = new Tessellator();
			verts.startDrawingQuads();		
			verts.setColorRGBA_F(0, 0.2F, 0, 1);
			RenderUtils.addBox(verts, 0, 0, 0, size, 2, size);
		}
		else laserHelper.reset();
	}
	
	@SideOnly(Side.CLIENT)
	/** Will draw a single gate to the vertex buffer **/
	public void loadGateAt(int x, int y)
	{
		if(refMatrix == null) return;
		int id = refMatrix[x][y];
		int wireID = CircuitPart.getIdFromClass(CircuitPart.PartWire.class);
		int ioID = CircuitPart.getIdFromClass(CircuitPart.PartIOBit.class);
		if(id == wireID)
		{
			verts.setColorRGBA_F(0, 0.6F, 0, 1);
			RenderUtils.addBox(verts, x + 6 / 16F, 2, y + 6 / 16F, 4 / 16F, 0.5, 4 / 16F);
			if(x - 1 >= 0 && refMatrix[x - 1][y] != 0)
				RenderUtils.addBox(verts, x, 2, y + 6 / 16F, 6 / 16F, 0.5, 4 / 16F);
			if(x + 1 < size && refMatrix[x + 1][y] != 0)
				RenderUtils.addBox(verts, x + 10 / 16F, 2, y + 6 / 16F, 6 / 16F, 0.5, 4 / 16F);
			if(y - 1 >= 0 && refMatrix[x][y - 1] != 0)
				RenderUtils.addBox(verts, x + 6 / 16F, 2, y, 4 / 16F, 0.5, 6 / 16F);
			if(y + 1 < size && refMatrix[x][y + 1] != 0)
				RenderUtils.addBox(verts, x + 6 / 16F, 2, y + 10 / 16F, 4 / 16F, 0.5, 6 / 16F);
		}
		else if(id != 0)
		{
			if(id == ioID) verts.setColorRGBA(175, 148, 56, 255);
			else verts.setColorRGBA_F(0, 0, 0, 1);
			RenderUtils.addBox(verts, x, 2, y, 1, 0.75, 1);
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
		loadMatrix();
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
