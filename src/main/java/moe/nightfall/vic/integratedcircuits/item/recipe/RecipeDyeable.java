package moe.nightfall.vic.integratedcircuits.item.recipe;

import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.api.IDyeable;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;

public class RecipeDyeable implements IRecipe
{
	int color = 0;
	ItemStack stack = null;
	
	public RecipeDyeable() 
	{
		RecipeSorter.register(Constants.MOD_ID + ":dyeable", getClass(), Category.SHAPELESS, "after:minecraft:shapeless");
	}
	
	@Override
	public int getRecipeSize() 
	{
		return 2;
	}
	
	@Override
	public ItemStack getRecipeOutput() 
	{
		return null;
	}
	
	@Override
	public boolean matches(InventoryCrafting crafting, World world) 
	{
		ItemStack colorStack = null;
		color = -1;
		for(int i = 0; i < crafting.getSizeInventory(); i++)
		{
			ItemStack stack = crafting.getStackInSlot(i);
			if(stack == null) continue;
			int color2 = MiscUtils.getColor(stack);
			if(color2 > 0)
			{
				if(color < 0) 
				{
					color = color2;
					colorStack = stack;
				}
				else return false;
			}
		}
		if(color == -1) return false;
		
		stack = null;
		for(int i = 0; i < crafting.getSizeInventory(); i++)
		{
			ItemStack stack = crafting.getStackInSlot(i);
			if(stack == null || stack == colorStack) continue;
			if(stack.getItem() instanceof IDyeable && this.stack == null)
			{
				IDyeable dyeable = (IDyeable)stack.getItem();
				if(dyeable.canDye(color, stack))
					this.stack = stack;
				else return false;
			}
			else return false;
		}
		return stack != null;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting crafting) 
	{
		if(color == -1 || stack == null) return null;
		return new ItemStack(stack.getItem(), 1, color);
	}
}
