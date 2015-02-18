package vic.mod.integratedcircuits.item.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import vic.mod.integratedcircuits.Constants;
import vic.mod.integratedcircuits.item.ItemPartGate;
import codechicken.microblock.ItemMicroPart;

public class RecipeFMPGate implements IRecipe
{
	private final ItemStack cover = ItemMicroPart.create(1, "tile.stone");
	
	public RecipeFMPGate() 
	{
		RecipeSorter.register(Constants.MOD_ID + ":fmpgate", getClass(), Category.SHAPED, "after:minecraft:shaped");
	}
	
	@Override
	public boolean matches(InventoryCrafting crafting, World world) 
	{
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				if(i == 1 && j == 1) continue;
				ItemStack stack = crafting.getStackInRowAndColumn(i, j);
				if(stack == null) return false;
				if(!stack.isItemEqual(cover)) return false;
			}
		}
		
		ItemStack stack = crafting.getStackInRowAndColumn(1, 1);
		if(stack != null && stack.getItem() instanceof ItemPartGate)
		{
			ItemPartGate partItem = (ItemPartGate)stack.getItem();
			if(!partItem.isMultipartItem()) return true;
		}
		return false;
	}
	
	@Override
	public int getRecipeSize() 
	{
		return 9;
	}
	
	@Override
	public ItemStack getRecipeOutput() 
	{
		return null;
	}
	
	@Override
	public ItemStack getCraftingResult(InventoryCrafting crafting) 
	{
		ItemStack stack = crafting.getStackInRowAndColumn(1, 1);
		ItemStack stack2 = new ItemStack(((ItemPartGate)stack.getItem()).getParent().getItemFMP());
		if(stack.getTagCompound() != null)
			stack2.setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
		stack2.setItemDamage(stack.getItemDamage());
		return stack2;
	}
}
