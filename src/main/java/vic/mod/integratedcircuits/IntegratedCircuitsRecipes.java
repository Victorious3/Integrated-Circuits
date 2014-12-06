package vic.mod.integratedcircuits;

import java.util.Arrays;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import cpw.mods.fml.common.registry.GameData;
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
					'i', IntegratedCircuits.itemSiliconDrop,
					'#', Items.redstone
		);
		
		GameRegistry.addRecipe(new ItemStack(IntegratedCircuits.itemPCB),
			"iii",
			"iii",
			"iii",
					'i', IntegratedCircuits.itemSiliconDrop
		);

		//Oredict
		Item bpSilicon = GameData.getItemRegistry().getObject("bluepower:item.silicon_wafer");
		if(bpSilicon != null) OreDictionary.registerOre("silicon", bpSilicon);
			
		Item prSilicon = GameData.getItemRegistry().getObject("ProjRed|Core:projectred.core.part");
		if(prSilicon != null) OreDictionary.registerOre("silicon", new ItemStack(prSilicon, 1, 12));
		
		OreDictionary.registerOre("silicon", IntegratedCircuits.itemSilicon);
		
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(IntegratedCircuits.itemSiliconDrop, 8), "silicon"));
		
		if(!(IntegratedCircuits.isPRLoaded && IntegratedCircuits.isBPLoaded))
		{
			GameRegistry.addRecipe(new ItemStack(IntegratedCircuits.itemCoalCompound),
				"###",
				"#c#",
				"###",
						'#', Blocks.sand,
						'c', Items.coal
			);
			GameRegistry.addSmelting(IntegratedCircuits.itemCoalCompound, new ItemStack(IntegratedCircuits.itemSilicon, 8), 0.5F);
		}
		
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
