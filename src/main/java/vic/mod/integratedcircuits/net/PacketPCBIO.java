package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.MiscUtils;
import vic.mod.integratedcircuits.TileEntityPCBLayout;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
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
					NBTTagCompound compMatrix = new NBTTagCompound();
					MiscUtils.writePCBMatrix(compMatrix, te.getMatrix());
					comp.setTag("matrix", compMatrix);
					comp.setInteger("size", te.getMatrix()[0].length - 2);
					comp.setString("author", player.getCommandSenderName());
					floppy.setTagCompound(comp);
					te.setInventorySlotContents(0, floppy);
				}
			}
			else
			{
				ItemStack floppy = te.getStackInSlot(0);
				if(floppy != null)
				{
					NBTTagCompound comp = floppy.getTagCompound();
					if(comp == null) return;
					String name = comp.getString("name");
					if(comp.hasKey("matrix")) te.setMatrix(MiscUtils.readPCBMatrix(comp.getCompoundTag("matrix")));	
					else 
					{
						int size = te.getMatrix()[0].length;
						te.setMatrix(new int[0][size][size]);
					}
					IntegratedCircuits.networkWrapper.sendToAllAround(new PacketPCBChangeName(name, xCoord, yCoord, zCoord), 
						new TargetPoint(te.getWorldObj().getWorldInfo().getVanillaDimension(), xCoord, yCoord, zCoord, 8));
					IntegratedCircuits.networkWrapper.sendToAllAround(new PacketPCBUpdate(te.getMatrix(), xCoord, yCoord, zCoord), 
						new TargetPoint(te.getWorldObj().getWorldInfo().getVanillaDimension(), xCoord, yCoord, zCoord, 8));
				}
			}
		}
	}
}
