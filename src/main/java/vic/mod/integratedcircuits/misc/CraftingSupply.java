package vic.mod.integratedcircuits.misc;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import vic.mod.integratedcircuits.TileEntityBase;
import cpw.mods.fml.common.registry.GameData;

public class CraftingSupply 
{
	private CraftingAmount cache;
	private TileEntityBase provider;
	private int from, to;
	private Item insufficient;
	
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
	
	//FIXME Screws up with multiple items missing;
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
		
		insufficient = amount.item;
		return false;
	}
	
	public static CraftingSupply readFromNBT(NBTTagCompound compound, TileEntityBase provider, CraftingAmount base, int from, int to)
	{
		CraftingSupply supply = new CraftingSupply(provider, from, to);
		NBTTagList list = compound.getTagList("supply", NBT.TAG_COMPOUND);
		for(int i = 0; i < list.tagCount(); i++)
		{
			NBTTagCompound comp = list.getCompoundTagAt(i);
			String id = comp.getString("id");
			double amount = comp.getDouble("amount");
			supply.cache.add(new ItemAmount(GameData.getItemRegistry().getRaw(id), amount));
		}
		supply.insufficient = GameData.getItemRegistry().getRaw(compound.getString("insufficient"));
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
		compound.setString("insufficient", GameData.getItemRegistry().getNameForObject(insufficient));
		return compound;
	}
	
	public Item getInsufficient()
	{
		return insufficient;
	}
	
	public void changeInsufficient(Item insufficient)
	{
		this.insufficient = insufficient;
	}
}
