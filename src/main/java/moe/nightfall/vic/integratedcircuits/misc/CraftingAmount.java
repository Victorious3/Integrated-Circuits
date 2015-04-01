package moe.nightfall.vic.integratedcircuits.misc;

import java.util.ArrayList;

import com.google.common.collect.Lists;

public class CraftingAmount implements Cloneable
{
	private ArrayList<ItemAmount> items = Lists.newArrayList();
	
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
