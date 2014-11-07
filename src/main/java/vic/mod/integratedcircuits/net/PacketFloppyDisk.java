package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import vic.mod.integratedcircuits.IDiskDrive;
import cpw.mods.fml.relauncher.Side;

public class PacketFloppyDisk extends PacketTileEntity<PacketFloppyDisk>
{
	private ItemStack stack;
	
	public PacketFloppyDisk(){}
	
	public PacketFloppyDisk(int xCoord, int yCoord, int zCoord, ItemStack stack)
	{
		super(xCoord, yCoord, zCoord);
		this.stack = stack;
	}
	
	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
		NBTTagCompound tag = buffer.readNBTTagCompoundFromBuffer();
		if(!tag.hasNoTags()) 
			stack = ItemStack.loadItemStackFromNBT(tag);
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		buffer.writeNBTTagCompoundToBuffer(stack != null ? stack.writeToNBT(new NBTTagCompound()) : new NBTTagCompound());
	}

	@Override
	public void process(EntityPlayer player, Side side) 
	{
		TileEntity te = player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(te == null || !(te instanceof IDiskDrive)) return;
		IDiskDrive dd = (IDiskDrive)te;
		dd.setDisk(stack);
	}
}
