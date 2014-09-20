package vic.mod.integratedcircuits.net;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.TileEntityAssembler;
import cpw.mods.fml.relauncher.Side;

public class PacketAssemblerStart extends PacketTileEntity<PacketAssemblerStart>
{
	public PacketAssemblerStart() {}
	
	public PacketAssemblerStart(int xCoord, int yCoord, int zCoord)
	{
		super(xCoord, yCoord, zCoord);
	}
	
	@Override
	public void process(EntityPlayer player, Side side) 
	{
		TileEntityAssembler te = (TileEntityAssembler)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(te != null)
		{
			ItemStack floppy = te.getDisk();
			if(floppy == null) return;
			if(te.getStackInSlot(1) != null) return;
			ItemStack circuit = new ItemStack(IntegratedCircuits.itemCircuit);
			NBTTagCompound fcomp = floppy.getTagCompound();
			if(fcomp == null || !fcomp.hasKey("circuit")) return;
			
			NBTTagCompound icomp = new NBTTagCompound();
			icomp.setTag("circuit", fcomp.getTag("circuit").copy());
			icomp.setInteger("con", fcomp.getInteger("con"));
			icomp.setString("name", fcomp.getString("name"));
			int size = fcomp.getInteger("size");
			icomp.setInteger("tier", size == 18 ? 1 : size == 34 ? 2 : 3);	
			
			circuit.stackTagCompound = icomp;
			te.setInventorySlotContents(1, circuit);
		}
	}
}
