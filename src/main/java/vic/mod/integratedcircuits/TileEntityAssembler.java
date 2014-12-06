package vic.mod.integratedcircuits;

import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.Constants.NBT;
import vic.mod.integratedcircuits.client.TileEntityAssemblerRenderer;
import vic.mod.integratedcircuits.client.gui.GuiAssembler;
import vic.mod.integratedcircuits.client.gui.GuiPCBLayout;
import vic.mod.integratedcircuits.ic.CircuitData;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.net.PacketAssemblerChangeItem;
import vic.mod.integratedcircuits.net.PacketAssemblerChangeSetting;
import vic.mod.integratedcircuits.net.PacketAssemblerStart;
import vic.mod.integratedcircuits.net.PacketFloppyDisk;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityAssembler extends TileEntityBase implements IDiskDrive, ISidedInventory
{
	//TODO change the LaserHelper so that it uses status codes
	public static final int IDLE = 0, RUNNING = 1, OUT_OF_MATERIALS = 2, OUT_OF_PCB = 3;
	public static final int SETTING_PULL = 0;
	private static final ItemStack STACK_PCB = new ItemStack(IntegratedCircuits.itemPCB, 1);
	
	public int[][] refMatrix;
	private int statusCode;
	
	//Settings
	public boolean automaticPull;
	
	//Client
	@SideOnly(Side.CLIENT)
	public Framebuffer circuitFBO;
	public boolean isOccupied;
	public byte request = 1;
	
	private byte queue, position;
	public boolean[][] excMatrix;
	public CircuitData cdata;
	public int size;
	public ItemStack[] contents = new ItemStack[13];
	
	public LaserHelper laserHelper = new LaserHelper(this, 9);
	
	@Override
	public void updateEntity() 
	{
		if(worldObj.isRemote && circuitFBO == null) TileEntityAssemblerRenderer.scheduleFramebuffer(this);
		if(worldObj.isRemote) return;
		
		if(refMatrix != null)
			laserHelper.update();
		
		if(statusCode == OUT_OF_PCB && queue != 0) requestCircuitPlayload();
	}
	
	public void updateStatus(int status)
	{
		if(statusCode != status)
		{
			statusCode = status;
			if(!worldObj.isRemote)
				worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 1, status);
		}
	}
	
	public int getStatus()
	{
		return statusCode;
	}
	
	public void changeSettingPayload(int setting, int par)
	{
		switch(setting) {
		case SETTING_PULL : automaticPull = par == 1; break;
		}
	}
	
	public void changeSetting(int setting, int par)
	{
		if(worldObj.isRemote)
			IntegratedCircuits.networkWrapper.sendToServer(new PacketAssemblerChangeSetting(xCoord, yCoord, zCoord, setting, par));
		else changeSettingPayload(setting, par);
	}

	@Override
	public boolean receiveClientEvent(int id, int par) 
	{
		if(id == 1)
		{
			if(worldObj.isRemote) statusCode = par;
			return true;
		}
		else if(id == 2)
		{
			if(worldObj.isRemote) position = (byte)par;
			return true;
		}
		
		return super.receiveClientEvent(id, par);
	}
	
	public void requestCircuit(byte amount)
	{
		if(queue != 0) return;
		setQueueSize(amount);
		if(!requestCircuitPlayload()) setQueueSize((byte)0);
		position = 0;
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 2, position);
	}

	private boolean requestCircuitPlayload()
	{
		if(cdata != null && laserHelper.getLaserAmount() > 0)
		{
			if(tryFetchItem(STACK_PCB.copy(), 1, 1) == null) 
			{
				if(!(automaticPull && tryFetchPCB()))
				{
					updateStatus(OUT_OF_PCB);
					return true;
				}
			}
			laserHelper.reset();
			laserHelper.start();
			updateStatus(RUNNING);
			IntegratedCircuits.networkWrapper.sendToDimension(new PacketAssemblerStart(xCoord, yCoord, zCoord, queue), worldObj.provider.dimensionId);
			return true;
		}
		return false;
	}
	
	public ItemStack tryFetchItem(ItemStack stack, int from, int to)
	{
		for(int i = from; i <= to; i++)
		{
			ItemStack stack2 = getStackInSlot(i);
			if(stack2 == null) continue; 
			if(stack.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack, stack2))
			{
				if(stack2.stackSize >= stack.stackSize)
				{
					decrStackSize(i, stack.stackSize);
					return stack;
				}
			}
		}
		return null;
	}
	
	public boolean tryPutItem(ItemStack stack, int from, int to)
	{
		if(stack == null) return true;
		for(int i = from; i <= to; i++)
		{
			ItemStack stack2 = getStackInSlot(i);
			if(stack2 != null && stack.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack, stack2))
			{
				if(stack2.getMaxStackSize() >= stack2.stackSize + stack.stackSize)
				{
					stack2.stackSize += stack.stackSize;
					onSlotChange(i);
					return true;
				}
			}
		}
		for(int i = from; i <= to; i++)
		{
			ItemStack stack2 = getStackInSlot(i);
			if(stack2 == null)
			{
				setInventorySlotContents(i, stack);
				return true;
			}
		}
		return false;
	}
	
	public boolean tryFetchPCB()
	{
		ItemStack pcb = getStackInSlot(1);
		if(pcb != null)
		{
			if(!tryPutItem(pcb, 2, 9)) return false;
			setInventorySlotContents(1, null);
		}
		pcb = tryFetchItem(STACK_PCB.copy(), 2, 9);
		if(pcb == null) return false;
		return true;
	}
	
	public void setQueueSize(byte queue)
	{
		this.queue = queue;
	}
	
	public byte getQueueSize()
	{
		return queue;
	}
	
	public void clearQueue()
	{
		if(getStatus() != RUNNING)
		{
			laserHelper.reset();
			updateStatus(IDLE);
			IntegratedCircuits.networkWrapper.sendToDimension(new PacketAssemblerStart(xCoord, yCoord, zCoord, queue), worldObj.provider.dimensionId);
		}	
		position = 0;
		queue = 0;
	}

	public byte getQueuePosition()
	{
		return position;
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
			worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 2, ++position);
		}
		if(position == queue || queue == 0) 
		{
			queue = 0;
			updateStatus(IDLE);
		}
		else requestCircuitPlayload();
		
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
	
	public void dropContents()
	{
		for(ItemStack stack : contents)
		{
			if(stack == null) continue;
			worldObj.spawnEntityInWorld(new EntityItem(worldObj, xCoord, yCoord, zCoord, stack));
		}
	}

	@Override
	public void setDisk(ItemStack stack) 
	{
		setInventorySlotContents(0, stack);
		if(!worldObj.isRemote) 
			IntegratedCircuits.networkWrapper.sendToDimension(new PacketFloppyDisk(xCoord, yCoord, zCoord, stack), worldObj.provider.dimensionId);
		loadMatrixFromDisk();
		if(worldObj.isRemote && Minecraft.getMinecraft().currentScreen instanceof GuiPCBLayout)
		{
			cdata.calculateCost();
			((GuiAssembler)Minecraft.getMinecraft().currentScreen).refreshUI();
		}
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
		
		queue = compound.getByte("queue");
		position = compound.getByte("positon");
		statusCode = compound.getInteger("status");
		automaticPull = compound.getBoolean("automaticPull");
		
		loadMatrix(compound);
		if(compound.hasKey("tmp"))
		{
			excMatrix = new boolean[size][size];
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
		
		compound.setByte("queue", queue);
		compound.setByte("positon", position);
		compound.setInteger("status", statusCode);
		compound.setBoolean("automaticPull", automaticPull);
		
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
