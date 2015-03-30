package vic.mod.integratedcircuits.gate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import vic.mod.integratedcircuits.gate.ISocketBridge.ISocketBase;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;

public abstract class Gate implements IGate
{
	private String name;
	protected ISocketBase provider;
	
	public Gate(String name)
	{
		this.name = name;
	}
	
	@Override
	public ISocketBase getProvider()
	{
		return provider;
	}
	
	@Override
	public void setProvider(Socket provider)
	{
		this.provider = provider;
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public void preparePlacement(EntityPlayer player, ItemStack stack) {}
	
	@Override
	public void load(NBTTagCompound tag) {}
	
	@Override
	public void save(NBTTagCompound tag) {}

	@Override
	public void readDesc(MCDataInput packet) {}
	
	@Override
	public void writeDesc(MCDataOutput packet) {}

	@Override
	public void read(byte discr, MCDataInput packet) {}
	
	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item) 
	{
		return false;
	}

	@Override
	public void onActivatedWithScrewdriver(EntityPlayer player, MovingObjectPosition hit, ItemStack item) {}
	
	@Override
	public void onRotated() {}

	@Override
	public void onAdded() 
	{
		notifyChanges();
	}

	@Override
	public void onRemoved()
	{
		provider.notifyBlocksAndChanges();
	}

	@Override
	public void onMoved() 
	{
		notifyChanges();
	}

	private void notifyChanges()
	{
		if(!provider.getWorld().isRemote) provider.updateInput();
		provider.notifyBlocksAndChanges();
	}

	@Override
	public ItemStack pickItem(MovingObjectPosition hit) 
	{
		return getItemStack();
	}
	
	@Override
	public void onNeighborChanged() {}
	
	@Override
	public void update() {}
	
	@Override
	public void scheduledTick() {}
	
	@Override
	public void updateInputPre() {}
	
	@Override
	public void updateInputPost() {}

	@Override
	public boolean hasComparatorInputAtSide(int side)
	{
		return false;
	}
}
