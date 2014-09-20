package vic.mod.integratedcircuits;

import java.util.LinkedList;

import net.minecraft.client.shader.Framebuffer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.util.MiscUtils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityAssembler extends TileEntityBase implements IDiskDrive, ISidedInventory
{
	@SideOnly(Side.CLIENT)
	public Framebuffer circuitFBO;
	
	//Used to unload the FBOs when the world does. If there is a better way to do this, tell me.
	@SideOnly(Side.CLIENT)
	public static LinkedList<Framebuffer> fboArray = new LinkedList<Framebuffer>();
	
	public int[][][] matrix;
	public ItemStack[] contents = new ItemStack[2];
	
	@SideOnly(Side.CLIENT)
	public void updateFramebuffer()
	{
//		if(matrix == null) return;
		if(circuitFBO == null)
		{
			circuitFBO = new Framebuffer(64, 64, false);
			fboArray.add(circuitFBO);
		}
		circuitFBO.framebufferClear();
		circuitFBO.bindFramebuffer(false);
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glViewport(0, 0, 64, 64);
		GL11.glOrtho(0, 64, 64, 0, -1, 1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
        
		GL11.glColor3f(0, 0.3F, 0);
		
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2i(0, 0);
		GL11.glVertex2i(0, 20);
		GL11.glVertex2i(20, 20);
		GL11.glVertex2i(20, 0);
		GL11.glEnd();
		
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2i(20, 20);
		GL11.glVertex2i(20, 45);
		GL11.glVertex2i(45, 45);
		GL11.glVertex2i(45, 20);
		GL11.glEnd();
		
		GL11.glLineWidth(1F);
		GL11.glColor3f(0, 0.8F, 0);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2i(1, 1);
		GL11.glVertex2i(15, 30);
		GL11.glEnd();
		
		GL11.glBegin(GL11.GL_POINTS);
		GL11.glVertex2i(25, 25);
		GL11.glEnd();
		
		GL11.glBegin(GL11.GL_POINTS);
		GL11.glVertex2i(63, 63);
		GL11.glEnd();
		
		circuitFBO.unbindFramebuffer();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) 
	{
		super.readFromNBT(compound);
		for(int i = 0; i < 1; i++)
		{
			if(compound.getCompoundTag("stack_" + i).hasNoTags())
				contents[i] = null;
			else contents[i] = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("stack_" + i));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) 
	{
		super.writeToNBT(compound);
		for(int i = 0; i < 1; i++)
		{
			compound.setTag("stack_" + i, contents[i] != null ? contents[i].writeToNBT(new NBTTagCompound()) : new NBTTagCompound());
		}
	}

	@Override
	public void updateEntity() 
	{
		if(circuitFBO == null && FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) updateFramebuffer();
	}

	@Override
	public void invalidate() 
	{
		super.invalidate();
		if(worldObj.isRemote && circuitFBO != null) 
		{
			circuitFBO.deleteFramebuffer();
			fboArray.remove(circuitFBO);
		}
	}

	@Override
	public int getSizeInventory() 
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int id) 
	{
		return contents[id];
	}

	@Override
	public ItemStack decrStackSize(int id, int amount) 
	{
		if(contents[id] == null) return null;
		contents[id].stackSize -= amount;
		ItemStack ret = contents[id];
		if(contents[id].stackSize < 1) contents[id] = null;
		this.markDirty();
		return ret;
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
