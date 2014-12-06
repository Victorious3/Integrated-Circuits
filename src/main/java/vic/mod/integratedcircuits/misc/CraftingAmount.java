package vic.mod.integratedcircuits.misc;

import java.util.ArrayList;

import net.minecraft.item.Item;

public class CraftingAmount implements Cloneable
{
	private ArrayList<ItemAmount> items = new ArrayList<ItemAmount>();
	
	public void add(ItemAmount itemAmount)
	{
		for(ItemAmount a : items)
		{
			if(a.item == itemAmount.item) 
			{
				a.amount += itemAmount.amount;
				return;
			}
		}
		items.add(itemAmount);
	}
	
	public ArrayList<ItemAmount> getCraftingAmount()
	{
		return items;
	}
	
	public boolean contains(Item item)
	{
		for(ItemAmount a : items)
		{
			if(a.item == item) return true;
		}
		return false;
	}
	
	public ItemAmount get(Item item)
	{
		for(ItemAmount a : items)
		{
			if(a.item == item) return a;
		}
		return null;
	}
}
