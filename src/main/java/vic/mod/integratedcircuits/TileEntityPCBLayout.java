package vic.mod.integratedcircuits;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.net.PacketPCBUpdate;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public class TileEntityPCBLayout extends TileEntity implements ICircuit, IInventory
{
	private int[][][] pcbMatrix;
	public String name = "NO_NAME";
	public int rotation;
	public int playersUsing;
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
					SubLogicPart.getPart(x, y, this).onUpdateTick();
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
		rotation = compound.getInteger("rotation");
		NBTTagCompound stackCompound = compound.getCompoundTag("floppyStack");
		floppyStack = ItemStack.loadItemStackFromNBT(stackCompound);
		name = compound.getString("name");
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) 
	{
		super.writeToNBT(compound);
		MiscUtils.writePCBMatrix(compound, pcbMatrix);
		compound.setInteger("rotation", rotation);
		NBTTagCompound stackCompound = new NBTTagCompound();
		if(floppyStack != null) floppyStack.writeToNBT(stackCompound);
		compound.setTag("floppyStack", stackCompound);
		compound.setString("name", name);
	}

	@Override
	public Packet getDescriptionPacket() 
	{
		NBTTagCompound compound = new NBTTagCompound();
		writeToNBT(compound);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, compound);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) 
	{
		NBTTagCompound compound = pkt.func_148857_g();
		readFromNBT(compound);
	}

	@Override
	public boolean receiveClientEvent(int id, int par)
	{
		if(id == 0)
		{
			playersUsing = par;
			return true;
		}
		return false;
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
	public boolean isUseableByPlayer(EntityPlayer player) 
	{
		return player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
	}

	@Override
	public void openInventory() 
	{
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 0, ++playersUsing);
	}

	@Override
	public void closeInventory() 
	{
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 0, --playersUsing);
	}

	@Override
	public boolean isItemValidForSlot(int id, ItemStack stack) 
	{
		return id == 0 && (stack == null || stack.getItem() == null || stack.getItem() == IntegratedCircuits.itemFloppyDisk);
	}
}
