package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.TileEntityPCBLayout;
import cpw.mods.fml.relauncher.Side;

public class PacketPCBIO extends PacketPCB<PacketPCBIO>
{
	private boolean write;
	
	public PacketPCBIO(){}
	
	public PacketPCBIO(boolean write, int xCoord, int yCoord, int zCoord)
	{
		super(xCoord, yCoord, zCoord);
		this.write = write;
	}
	
	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
		this.write = buffer.readBoolean();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		buffer.writeBoolean(write);
	}

	@Override
	public void process(EntityPlayer player, Side side) 
	{
		TileEntityPCBLayout te = (TileEntityPCBLayout)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(te != null)
		{
			if(write)
			{
				ItemStack floppy = te.getStackInSlot(0);
				if(floppy != null)
				{
					NBTTagCompound comp = floppy.getTagCompound();
					if(comp == null) comp = new NBTTagCompound();
					comp.setString("name", te.name);
					floppy.setTagCompound(comp);
					te.setInventorySlotContents(0, floppy);
				}
			}
			else
			{
				
			}
		}
	}
}
