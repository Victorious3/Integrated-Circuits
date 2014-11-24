package vic.mod.integratedcircuits.misc;

import java.util.ArrayList;

public class CraftingAmount 
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
}
