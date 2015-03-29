package vic.mod.integratedcircuits.gate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import vic.mod.integratedcircuits.client.IPartRenderer;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class PartGate
{
	private String name;
	protected Socket provider;
	
	public PartGate(String name)
	{
		this.name = name;
	}
	
	public ISocket getProvider()
	{
		return provider;
	}
	
	public void setProvider(Socket provider)
	{
		this.provider = provider;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void preparePlacement(EntityPlayer player, BlockCoord pos, int side, int meta) {}
	
	public void load(NBTTagCompound tag) {}
	
	public void save(NBTTagCompound tag) {}

	public void readDesc(MCDataInput packet) {}
	
	public void writeDesc(MCDataOutput packet) {}

	public void read(byte discr, MCDataInput packet) {}
	
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item) 
	{
		return false;
	}

	public void onActivatedWithScrewdriver(EntityPlayer player, MovingObjectPosition hit, ItemStack item) {}
	
	public void onRotated() {}

	public void onAdded() 
	{
		notifyChanges();
	}

	public void onRemoved()
	{
		provider.notifyBlocksAndChanges();
	}

	public void onMoved() 
	{
		notifyChanges();
	}

	private void notifyChanges()
	{
		if(!provider.getWorld().isRemote) provider.updateInput();
		provider.notifyBlocksAndChanges();
	}
	
	public abstract ItemStack getItemStack();

	public ItemStack pickItem(MovingObjectPosition hit) 
	{
		return getItemStack();
	}
	
	@SideOnly(Side.CLIENT)
	public abstract IPartRenderer getRenderer();
	
	public abstract Cuboid6 getDimension();
	
	public void onNeighborChanged() {}
	
	public void update() {}
	
	public void scheduledTick() {}
	
	public void updateInputPre() {}
	
	public void updateInputPost() {}

	public abstract boolean canConnectRedstone(int arg0);
	public abstract boolean canConnectBundled(int arg0);

	public abstract PartGate newInstance();
}
