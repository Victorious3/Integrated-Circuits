package vic.mod.integratedcircuits.misc;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import vic.mod.integratedcircuits.TileEntityBase;
import cpw.mods.fml.common.registry.GameData;

public class CraftingSupply 
{
	private CraftingAmount base;
	private CraftingAmount cache;
	private TileEntityBase provider;
	private int from, to;
	private ItemAmount insufficient;
	
	public CraftingSupply(CraftingAmount amount, TileEntityBase provider, int from, int to)
	{
		this.base = amount;
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
		for(ItemAmount ia : amount.getCraftingAmount())
			if(!isSupplied(ia)) return false;
		for(ItemAmount ia : amount.getCraftingAmount())
			consume(ia);
		return true;
	}
	
	public void consume(ItemAmount amount)
	{
		ItemAmount cached = cache.get(amount.item);
		cached.amount -= amount.amount;
	}
	
	public boolean isSupplied(ItemAmount amount)
	{
		ItemAmount cached = null;
		if(cache.contains(amount.item))
		{
			cached = cache.get(amount.item);
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
		CraftingSupply supply = new CraftingSupply(base, provider, from, to);
		NBTTagList list = compound.getTagList("supply", NBT.TAG_COMPOUND);
		for(int i = 0; i < list.tagCount(); i++)
		{
			NBTTagCompound comp = list.getCompoundTagAt(i);
			String id = comp.getString("id");
			double amount = comp.getDouble("amount");
			supply.cache.add(new ItemAmount(GameData.getItemRegistry().getRaw(id), amount));
		}
		return supply;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		NBTTagList list = new NBTTagList();
		for(ItemAmount amount : cache.getCraftingAmount())
		{
			NBTTagCompound comp = new NBTTagCompound();
			comp.setString("id", GameData.getItemRegistry().getNameForObject(amount.item));
			comp.setDouble("amount", amount.amount);
			list.appendTag(comp);
		}
		compound.setTag("supply", list);
		return compound;
	}
	
	public ItemAmount getInsufficient()
	{
		return insufficient;
	}
}
