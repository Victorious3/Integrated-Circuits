package vic.mod.integratedcircuits;

import java.util.Arrays;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.registry.GameRegistry;

public class IntegratedCircuitsRecipes 
{
	public static void loadRecipes()
	{
		GameRegistry.addRecipe(new ItemStack(IntegratedCircuits.blockPCBLayout), 
			"iii",
			"i#i",
			"rrr",
					'i', Items.iron_ingot,
					'#', Blocks.glass_pane,
					'r', Items.redstone
		);
		
		GameRegistry.addRecipe(new ItemStack(IntegratedCircuits.blockAssembler), 
			"###",
			"#d#",
			"rrr",
					'd', Items.diamond,
					'#', Blocks.glass_pane,
					'r', Items.redstone
		);
		
		GameRegistry.addRecipe(new ItemStack(IntegratedCircuits.itemFloppyDisk),
			"iii",
			"i#i",
			"iii",
					'i', Items.iron_ingot,
					'#', Items.redstone
		);
		
		GameRegistry.addRecipe(new ShapelessRecipes(new ItemStack(IntegratedCircuits.itemCircuit), Arrays.asList(new ItemStack(IntegratedCircuits.itemPCB, 1, 1)))
		{
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
				ItemStack ret = new ItemStack(IntegratedCircuits.itemCircuit);
				NBTTagCompound comp = (NBTTagCompound)stack.getTagCompound().copy();
				ret.setTagCompound(comp);
				return ret;
			}
		});
	}
}
