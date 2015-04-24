package moe.nightfall.vic.integratedcircuits.item.recipe;

import java.util.Arrays;

import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;

public class RecipeCircuit extends ShapelessRecipes {
	public RecipeCircuit() {
		super(new ItemStack(IntegratedCircuits.itemCircuit), Arrays.asList(new ItemStack(IntegratedCircuits.itemPCB, 1,
				1)));
		RecipeSorter.register(Constants.MOD_ID + ":circuit", getClass(), Category.SHAPELESS,
				"after:minecraft:shapeless");
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting crafting) {
		ItemStack stack = null;
		for (int i = 0; i < crafting.getSizeInventory(); i++) {
			ItemStack tmp = crafting.getStackInSlot(i);
			if (tmp != null && tmp.getItem() == IntegratedCircuits.itemPCB && tmp.hasTagCompound()
					&& tmp.getTagCompound().hasKey("circuit")) {
				stack = tmp;
				break;
			}
		}
		if (stack == null)
			return null;
		ItemStack ret = new ItemStack(IntegratedCircuits.itemCircuit);
		NBTTagCompound comp = (NBTTagCompound) stack.getTagCompound().copy();
		ret.setTagCompound(comp);
		return ret;
	}
}
