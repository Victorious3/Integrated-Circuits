package vic.mod.integratedcircuits.misc;

import java.util.ArrayList;

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
	
	public boolean contains(ItemAmount amount)
	{
		for(ItemAmount a : items)
			if(a.hasEqualItem(amount)) return true;
		return false;
	}
	
	public ItemAmount get(ItemAmount amount)
	{
		for(ItemAmount a : items)
			if(a.hasEqualItem(amount)) return a;
		return null;
	}
}
