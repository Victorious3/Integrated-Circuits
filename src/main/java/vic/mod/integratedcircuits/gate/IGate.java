package vic.mod.integratedcircuits.gate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import vic.mod.integratedcircuits.gate.ISocket.EnumConnectionType;
import vic.mod.integratedcircuits.gate.ISocketBridge.ISocketBase;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Cuboid6;

public interface IGate
{
	public ISocketBase getProvider();
	
	public void setProvider(Socket provider);

	public void preparePlacement(EntityPlayer player, ItemStack stack);
	
	public void load(NBTTagCompound tag);
	
	public void save(NBTTagCompound tag);

	public void readDesc(MCDataInput packet);
	
	public void writeDesc(MCDataOutput packet);

	public void read(byte discr, MCDataInput packet);
	
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item);

	public void onActivatedWithScrewdriver(EntityPlayer player, MovingObjectPosition hit, ItemStack item);
	
	public void onRotated();

	public void onAdded();

	public void onRemoved();

	public void onMoved();
	
	public ItemStack getItemStack();

	public ItemStack pickItem(MovingObjectPosition hit);
	
	public Cuboid6 getDimension();
	
	public void onNeighborChanged();
	
	public void update();
	
	public void scheduledTick();
	
	public void updateInputPre();
	
	public void updateInputPost();

	public EnumConnectionType getConnectionTypeAtSide(int side);
	
	public boolean hasComparatorInputAtSide(int side);
}
