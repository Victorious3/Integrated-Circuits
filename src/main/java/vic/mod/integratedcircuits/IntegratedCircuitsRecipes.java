package vic.mod.integratedcircuits;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import vic.mod.integratedcircuits.item.recipe.RecipeCircuit;
import vic.mod.integratedcircuits.item.recipe.RecipeDyeable;
import vic.mod.integratedcircuits.item.recipe.RecipeFMPGate;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;

public class IntegratedCircuitsRecipes 
{
	public static void loadRecipes()
	{
		//Oredict
		Item bpSilicon = GameData.getItemRegistry().getObject("bluepower:silicon_wafer");
		if(bpSilicon != null) OreDictionary.registerOre("silicon", bpSilicon);
				
		Item prCorePart = GameData.getItemRegistry().getObject("ProjRed|Core:projectred.core.part");
		if(prCorePart != null) OreDictionary.registerOre("silicon", new ItemStack(prCorePart, 1, 12));
				
		if(!(IntegratedCircuits.isPRLoaded || IntegratedCircuits.isBPLoaded))
		{
			OreDictionary.registerOre("silicon", IntegratedCircuits.itemSilicon);
			
			GameRegistry.addRecipe(new ItemStack(IntegratedCircuits.itemCoalCompound),
				"###",
				"#c#",
				"###",
						'#', Blocks.sand,
						'c', Items.coal
			);
			
			GameRegistry.addRecipe(new ItemStack(IntegratedCircuits.item7Segment.getItem()),
				"srs",
				"r#r",
				"sps",
						'r', Items.redstone,
						's', Blocks.stone,
						'#', Blocks.glass_pane,
						'p', IntegratedCircuits.itemPCBChip
			);
			
			GameRegistry.addSmelting(IntegratedCircuits.itemCoalCompound, new ItemStack(IntegratedCircuits.itemSilicon, 8), 0.5F);
		}
		else
		{
			Item bpStoneWafer = GameData.getItemRegistry().getObject("bluepower:stone_tile");
			if(bpStoneWafer != null) OreDictionary.registerOre("stoneWafer", bpStoneWafer);
			
			Item bpStoneWire= GameData.getItemRegistry().getObject("bluepower:stone_wire");
			if(bpStoneWafer != null) OreDictionary.registerOre("stoneWire", bpStoneWire);

			if(prCorePart != null) 
			{
				OreDictionary.registerOre("stoneWafer", new ItemStack(prCorePart, 1, 0));
				OreDictionary.registerOre("stoneWire", new ItemStack(prCorePart, 1, 2));
			}
			
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(IntegratedCircuits.item7Segment.getItemFMP()),
				"srs",
				"r#r",
				"sps",
						'r', "stoneWire",
						's', "stoneWafer",
						'#', Blocks.glass_pane,
						'p', IntegratedCircuits.itemPCBChip
			));
		}
		
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(IntegratedCircuits.itemSiliconDrop, 8), "silicon"));
		
		GameRegistry.addRecipe(new ItemStack(IntegratedCircuits.itemPCBChip),
			"iri",
			"r#r",
			"iri",
					'i', IntegratedCircuits.itemSiliconDrop,
					'r', Items.redstone,
					'#', IntegratedCircuits.itemPCB
		);
		
		GameRegistry.addRecipe(new ItemStack(IntegratedCircuits.itemPCBChip),
			"rir",
			"i#i",
			"rir",
					'i', IntegratedCircuits.itemSiliconDrop,
					'r', Items.redstone,
					'#', IntegratedCircuits.itemPCB
		);
		
		GameRegistry.addRecipe(new ItemStack(IntegratedCircuits.blockPCBLayout), 
			"iii",
			"i#i",
			"sps",
					'i', Items.iron_ingot,
					'#', Blocks.glass_pane,
					's', Blocks.stone,
					'p', IntegratedCircuits.itemPCBChip
		);
		
		GameRegistry.addRecipe(new ItemStack(IntegratedCircuits.blockAssembler), 
			"###",
			"#d#",
			"sps",
					'd', Items.diamond,
					'#', Blocks.glass_pane,
					's', Blocks.stone,
					'p', IntegratedCircuits.itemPCBChip
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
		
		GameRegistry.addRecipe(new ItemStack(IntegratedCircuits.itemScrewdriver),
			"i  ",
			" id",
			" di",
					'i', Items.iron_ingot,
					'd', IntegratedCircuits.itemSiliconDrop
		);
		
		GameRegistry.addRecipe(new ItemStack(IntegratedCircuits.itemLaser),
			"oii",
			"p##",
			"oii",
					'i', Items.redstone,
					'#', Items.diamond,
					'o', Blocks.obsidian,
					'p', IntegratedCircuits.itemPCBChip
		);
		
		GameRegistry.addRecipe(new ItemStack(IntegratedCircuits.itemLaser),
			"i#i",
			"i#i",
			"opo",
					'i', Items.redstone,
					'#', Items.diamond,
					'o', Blocks.obsidian,
					'p', IntegratedCircuits.itemPCBChip
		);
		
		//TODO NEI integration? Rewrite using multiple recipes?
		GameRegistry.addRecipe(new RecipeDyeable());
		GameRegistry.addRecipe(new RecipeCircuit());
		
		if(IntegratedCircuits.isFMPLoaded)
			GameRegistry.addRecipe(new RecipeFMPGate());
	}
}
