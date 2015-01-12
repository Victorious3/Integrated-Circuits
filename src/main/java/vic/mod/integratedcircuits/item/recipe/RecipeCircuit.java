package vic.mod.integratedcircuits.item.recipe;

import java.util.Arrays;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import vic.mod.integratedcircuits.IntegratedCircuits;

public class RecipeCircuit extends ShapelessRecipes
{
	public RecipeCircuit() 
	{
		super(new ItemStack(IntegratedCircuits.itemCircuit.getItem()), Arrays.asList(new ItemStack(IntegratedCircuits.itemPCB, 1, 1)));
		RecipeSorter.register(IntegratedCircuits.modID + ":circuit", getClass(), Category.SHAPELESS, "after:minecraft:shapeless");
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting crafting) 
	{
		ItemStack stack = null;
		for(int i = 0; i < crafting.getSizeInventory(); i++)
		{
			ItemStack tmp = crafting.getStackInSlot(i);
			if(tmp != null && tmp.getItem() == IntegratedCircuits.itemPCB && tmp.hasTagCompound() && tmp.getTagCompound().hasKey("circuit"))
			{
				stack = tmp;
				break;
			}
		}
		if(stack == null) return null;
		ItemStack ret = new ItemStack(IntegratedCircuits.itemCircuit.getItem());
		NBTTagCompound comp = (NBTTagCompound)stack.getTagCompound().copy();
		ret.setTagCompound(comp);
		return ret;
	}
}
