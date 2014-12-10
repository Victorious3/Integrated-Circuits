package vic.mod.integratedcircuits.misc;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import vic.mod.integratedcircuits.TileEntityBase;

public class CraftingSupply 
{
	private CraftingAmount cache;
	private TileEntityBase provider;
	private int from, to;
	private ItemAmount insufficient;
	
	public CraftingSupply(TileEntityBase provider, int from, int to)
	{
		this.cache = new CraftingAmount();
		this.provider = provider;
		this.from = from;
		this.to = to;
	}
	
	public void clear()
	{
		cache.getCraftingAmount().clear();
	}
	
	public boolean request(CraftingAmount amount)
	{
		if(insufficient != null && !amount.contains(insufficient))
			return false;
		insufficient = null;
		for(ItemAmount ia : amount.getCraftingAmount())
			if(!isSupplied(ia)) return false;
		for(ItemAmount ia : amount.getCraftingAmount())
			consume(ia);
		return true;
	}
	
	public void consume(ItemAmount amount)
	{
		ItemAmount cached = cache.get(amount);
		cached.amount -= amount.amount;
	}
	
	public boolean isSupplied(ItemAmount amount)
	{
		ItemAmount cached = null;
		if(cache.contains(amount))
		{
			cached = cache.get(amount);
			if(amount.amount <= cached.amount) return true;
		}
		
		double am = amount.amount;
		if(cached != null) am -= cached.amount;
		int request = (int)Math.ceil(am);
		
		if(InventoryUtils.tryFetchItem(provider, new ItemStack(amount.item), from, to) != null)
		{
			cache.add(new ItemAmount(amount.item, request));
			return true;
		}
		
		insufficient = amount;
		return false;
	}
	
	public static CraftingSupply readFromNBT(NBTTagCompound compound, TileEntityBase provider, CraftingAmount base, int from, int to)
	{
		CraftingSupply supply = new CraftingSupply(provider, from, to);
		NBTTagList list = compound.getTagList("supply", NBT.TAG_COMPOUND);
		for(int i = 0; i < list.tagCount(); i++)
		{
			NBTTagCompound comp = list.getCompoundTagAt(i);
			supply.cache.add(ItemAmount.readFromNBT(comp));
		}
		if(compound.hasKey("insufficient"))
			supply.insufficient = ItemAmount.readFromNBT(compound.getCompoundTag("insufficient"));
		return supply;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		NBTTagList list = new NBTTagList();
		for(ItemAmount amount : cache.getCraftingAmount())
		{
			NBTTagCompound comp = new NBTTagCompound();
			list.appendTag(amount.writeToNBT(comp));
		}
		compound.setTag("supply", list);
		if(insufficient != null)
			compound.setTag("insufficient", insufficient.writeToNBT(new NBTTagCompound()));
		return compound;
	}
	
	public ItemAmount getInsufficient()
	{
		return insufficient;
	}
	
	public void changeInsufficient(ItemAmount insufficient)
	{
		this.insufficient = insufficient;
	}
}
