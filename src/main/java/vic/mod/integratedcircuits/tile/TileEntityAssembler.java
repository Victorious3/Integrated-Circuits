package vic.mod.integratedcircuits.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.DiskDrive.IDiskDrive;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.LaserHelper;
import vic.mod.integratedcircuits.client.TileEntityAssemblerRenderer;
import vic.mod.integratedcircuits.client.gui.GuiAssembler;
import vic.mod.integratedcircuits.client.gui.GuiPCBLayout;
import vic.mod.integratedcircuits.ic.CircuitData;
import vic.mod.integratedcircuits.misc.CraftingSupply;
import vic.mod.integratedcircuits.misc.IOptionsProvider;
import vic.mod.integratedcircuits.misc.InventoryUtils;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.net.PacketAssemblerChangeItem;
import vic.mod.integratedcircuits.net.PacketAssemblerStart;
import vic.mod.integratedcircuits.net.PacketFloppyDisk;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IHasWork;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@InterfaceList({
	@Interface(iface = "buildcraft.api.tiles.IControllable", modid = "BuildCraft|Core"),
	@Interface(iface = "buildcraft.api.tiles.IHasWork", modid = "BuildCraft|Core")
})
public class TileEntityAssembler extends TileEntityBase implements IDiskDrive, ISidedInventory, IOptionsProvider, IHasWork, IControllable
{
	public static final int IDLE = 0, RUNNING = 1, OUT_OF_MATERIALS = 2, OUT_OF_PCB = 3;
	public static final int SETTING_PULL = 0, SETTING_REDSTONE = 1;
	public static final int RS_ENABLED = 0, RS_INVERTED = 1, RS_DISABLED = 2;
	
	private static final ItemStack STACK_PCB = new ItemStack(IntegratedCircuits.itemPCB, 1);
	
	public int[][] refMatrix;
	private int statusCode;
	
	//Client
	@SideOnly(Side.CLIENT)
	public Framebuffer circuitFBO;
	public boolean isOccupied;
	public byte request = 1;
	
	private byte queue, position;
	public int size;
	private int power = -1;
	private int output = 0;
	private boolean powerOverride;
	
	public boolean[][] excMatrix;
	public CircuitData cdata;
	public LaserHelper laserHelper = new LaserHelper(this, 9);
	
	public ItemStack[] contents = new ItemStack[13];
	public CraftingSupply craftingSupply;
	private OptionSet<TileEntityAssembler> optionSet = new OptionSet<TileEntityAssembler>(this);
	
	@Override
	public void updateEntity() 
	{
		if(worldObj.isRemote && circuitFBO == null) TileEntityAssemblerRenderer.scheduleFramebuffer(this);
		if(worldObj.isRemote) return;
		
		if(power == -1) onNeighborBlockChange();
		
		if(refMatrix != null)
			laserHelper.update();
		
		if(output == 0) getWorldObj().notifyBlockChange(xCoord, yCoord, zCoord, getBlockType());
		if(output >= 0) output--;
		
		if(statusCode == OUT_OF_PCB && queue != 0) requestCircuitPlayload();
		else if(statusCode == IDLE && getOptionSet().getBoolean(SETTING_PULL))
		{
			if((isPowered() || powerOverride) && output < 0)
			{
				ItemStack stack = tryFetchPCB();
				if(stack != null) setInventorySlotContents(1, stack);
				requestCircuit((byte)1);
			}
		}
	}
	
	private boolean isPowered()
	{
		int rs = getOptionSet().getInt(SETTING_REDSTONE);
		return (rs == RS_ENABLED && power > 0) || (rs == RS_INVERTED && power == 0);
	}
	
	public int isProvidingPower()
	{
		return output > 0 && getOptionSet().getInt(SETTING_REDSTONE) != RS_DISABLED ? 15 : 0;
	}

	public void onNeighborBlockChange()
	{
		int nPower = worldObj.getStrongestIndirectPower(xCoord, yCoord, zCoord);
		if(nPower != power)
		{
			boolean o = power > 0, n = nPower > 0;
			int rsmode = getOptionSet().getInt(SETTING_REDSTONE);
			if(o != n && ((n && rsmode == RS_ENABLED) || (!n && rsmode == RS_INVERTED)) && output < 0)
			{
				if(getStatus() == IDLE) 
				{
					ItemStack stack = tryFetchPCB();
					if(stack != null) setInventorySlotContents(1, stack);
					requestCircuit((byte)1);
				}
			}
			power = nPower;
		}
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
	
	@Override
	public OptionSet getOptionSet() 
	{
		return optionSet;
	}

	@Override
	public void onSettingChanged(int setting) 
	{
		if(worldObj.isRemote && Minecraft.getMinecraft().currentScreen instanceof GuiAssembler)
			((GuiAssembler)Minecraft.getMinecraft().currentScreen).refreshUI();
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
			craftingSupply.clear();
			if(InventoryUtils.tryFetchItem(this, STACK_PCB.copy(), 1, 1) == null) 
			{
				if(!(optionSet.getBoolean(SETTING_PULL) && tryFetchPCB() != null))
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
	
	/** Clears the PCB slot and returns an empty PCB if one was found **/
	public ItemStack tryFetchPCB()
	{
		ItemStack pcb = getStackInSlot(1);
		if(pcb != null)
		{
			if(STACK_PCB.isItemEqual(pcb)) return pcb;
			if(!InventoryUtils.tryPutItem(this, pcb, 2, 9)) return null;
			setInventorySlotContents(1, null);
		}
		pcb = InventoryUtils.tryFetchItem(this, STACK_PCB.copy(), 2, 9);
		return pcb;
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
		position = 0;
		queue = 0;
		if(getStatus() != RUNNING && getStatus() != OUT_OF_MATERIALS)
		{
			laserHelper.reset();
			updateStatus(IDLE);
			IntegratedCircuits.networkWrapper.sendToDimension(new PacketAssemblerStart(xCoord, yCoord, zCoord, queue), worldObj.provider.dimensionId);
		}
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
			//Give off a redstone pulse
			output = 2;
			worldObj.notifyBlockChange(xCoord, yCoord, zCoord, getBlockType());
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
	
	@Override
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
		
		powerOverride = compound.getBoolean("powerOverride");
		output = compound.getInteger("output");
		queue = compound.getByte("queue");
		position = compound.getByte("positon");
		statusCode = compound.getInteger("status");
		optionSet = OptionSet.readFromNBT(compound, this);
		
		loadMatrix(compound);
		if(compound.hasKey("tmp"))
		{
			excMatrix = new boolean[size][size];
			byte[] temp = compound.getByteArray("tmp");
			for(int i = 0; i < temp.length; i++)
				excMatrix[i / size][i % size] = temp[i] != 0;
		}
		
		laserHelper.readFromNBT(compound);
		
		if(MiscUtils.isClient() && (getStackInSlot(1) != null || laserHelper.isRunning)) 
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
		
		compound.setBoolean("powerOverride", powerOverride);
		compound.setInteger("output", output);
		compound.setByte("queue", queue);
		compound.setByte("positon", position);
		compound.setInteger("status", statusCode);
		optionSet.writeToNBT(compound);
		
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
			craftingSupply = CraftingSupply.readFromNBT(compound, this, cdata.getCost(), 2, 9);
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
			craftingSupply.writeToNBT(compound);
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

	private static final int[] accessibleSlots = {1, 2, 3, 4, 5, 6, 7, 8, 9};
	
	@Override
	public int[] getAccessibleSlotsFromSide(int side) 
	{
		if(getConnectionOnSide(side) > -1) return accessibleSlots;
		return new int[0];
	}
	
	private int getConnectionOnSide(int side)
	{
		ForgeDirection dir = ForgeDirection.getOrientation(side);
		if(dir == ForgeDirection.UP) return -1;
		else if(dir == ForgeDirection.DOWN) return 0;
		dir = MiscUtils.rotn(ForgeDirection.getOrientation(side), -rotation);
		if(dir == ForgeDirection.SOUTH) return 1;
		else if(dir != ForgeDirection.NORTH) return 0;
		return -1;
	}

	@Override
	public boolean isItemValidForSlot(int id, ItemStack stack) 
	{
		if(id < 1 || id > 9) return false;
		if(id == 1 && getStackInSlot(1) == null && STACK_PCB.isItemEqual(stack)) return true;
		return id != 1;
	}
	
	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side) 
	{
		int con = getConnectionOnSide(side);
		return con == 0;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) 
	{
		int con = getConnectionOnSide(side);
		boolean isPCB = stack.getItem() == IntegratedCircuits.itemPCB && stack.getItemDamage() == 1;
		if(con == 0) return slot != 1 && !isPCB;
		else if(con == 1) return isPCB;
		return false;
	}

	@Override
	public Mode getControlMode() 
	{
		if(powerOverride || isPowered()) return Mode.On;
		return Mode.Off;
	}

	@Override
	public void setControlMode(Mode mode) 
	{
		if(mode == Mode.On) 
		{
			powerOverride = true;
			getOptionSet().changeSetting(SETTING_PULL, true);
		}
		else if(mode == Mode.Off) powerOverride = false;
	}

	@Override
	public boolean acceptsControlMode(Mode mode) 
	{
		return mode == Mode.Off || mode == Mode.On;
	}

	@Override
	public boolean hasWork() 
	{
		return getStatus() != IDLE;
	}
}
