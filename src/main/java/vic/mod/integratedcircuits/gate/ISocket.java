package vic.mod.integratedcircuits.gate;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import vic.mod.integratedcircuits.gate.ISocketBridge.ISocketBase;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.vec.BlockCoord;

/**
 * Contains all methods that have to be called by 
 * an {@link ISocketWrapper} and documentation.
 * 
 * @author Vic Nightfall
 */
public interface ISocket extends ISocketBase
{
	public void update();
	
	public void readFromNBT(NBTTagCompound compound);
	
	public void writeToNBT(NBTTagCompound compound);
	
	public void writeDesc(NBTTagCompound compound);

	public void readDesc(NBTTagCompound compound);
	
	public void read(MCDataInput packet);

	public void preparePlacement(EntityPlayer player, BlockCoord pos, int side, int meta);

	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack stack);

	public void onNeighborChanged();
	
	public void addDrops(List<ItemStack> list);
	
	public ItemStack pickItem(MovingObjectPosition target);
	
	public void scheduledTick();
	
	public void onRemoved();
	
	public static enum EnumConnectionType
	{
		SIMPLE, ANALOG, BUNDLED, NONE;
		
		public boolean isBundled() 
		{
			return this == BUNDLED;
		}
		
		public boolean isRedstone()
		{
			return this == SIMPLE || this == ANALOG;
		}
	}
}