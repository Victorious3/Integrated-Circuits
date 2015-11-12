package moe.nightfall.vic.integratedcircuits;

import codechicken.microblock.ItemMicroPart;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import moe.nightfall.vic.integratedcircuits.item.recipe.RecipeCircuit;
import moe.nightfall.vic.integratedcircuits.item.recipe.RecipeDyeable;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class Recipes {
	public static void loadRecipes() {
		// Oredict
		Item bpSilicon = GameData.getItemRegistry().getObject("bluepower:silicon_wafer");
		if (bpSilicon != null)
			OreDictionary.registerOre("silicon", bpSilicon);

		Item prCorePart = GameData.getItemRegistry().getObject("ProjRed|Core:projectred.core.part");
		if (prCorePart != null)
			OreDictionary.registerOre("silicon", new ItemStack(prCorePart, 1, 12));

		if (!(IntegratedCircuits.isPRLoaded || IntegratedCircuits.isBPLoaded)) {
			OreDictionary.registerOre("silicon", Content.itemSilicon);

			GameRegistry.addRecipe(new ItemStack(Content.itemCoalCompound),
					"###",
					"#c#",
					"###",

					'#', Blocks.sand,
					'c', Items.coal);

			GameRegistry.addRecipe(new ItemStack(Content.item7Segment),
					"srs",
					"r#r",
					"sps",

					'r', Items.redstone,
					's', Blocks.stone,
					'#', Blocks.glass_pane,
					'p', Content.itemPCBChip);

			GameRegistry.addSmelting(Content.itemCoalCompound, new ItemStack(Content.itemSilicon,
					8), 0.5F);
		} else {
			Item bpStoneWafer = GameData.getItemRegistry().getObject("bluepower:stone_tile");
			if (bpStoneWafer != null)
				OreDictionary.registerOre("stoneWafer", bpStoneWafer);

			Item bpStoneWire = GameData.getItemRegistry().getObject("bluepower:redstone_wire_tile");
			if (bpStoneWire != null)
				OreDictionary.registerOre("stoneWire", bpStoneWire);

			if (prCorePart != null) {
				OreDictionary.registerOre("stoneWafer", new ItemStack(prCorePart, 1, 0));
				OreDictionary.registerOre("stoneWire", new ItemStack(prCorePart, 1, 2));
			}

			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Content.item7Segment),
					"srs",
					"r#r",
					"sps",

					'r', "stoneWire",
					's', "stoneWafer",
					'#', Blocks.glass_pane,
					'p', Content.itemPCBChip));
		}

		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Content.itemSiliconDrop, 8), "silicon"));

		GameRegistry.addRecipe(new ItemStack(Content.itemPCBChip),
				"iri",
				"r#r",
				"iri",

				'i', Content.itemSiliconDrop,
				'r', Items.redstone,
				'#', Content.itemPCB);

		GameRegistry.addRecipe(new ItemStack(Content.itemPCBChip),
				"rir",
				"i#i",
				"rir",

				'i', Content.itemSiliconDrop,
				'r', Items.redstone,
				'#', Content.itemPCB);

		GameRegistry.addRecipe(new ItemStack(Content.blockPCBLayout),
				"iii",
				"i#i",
				"sps",

				'i', Items.iron_ingot,
				'#', Blocks.glass_pane,
				's', Blocks.stone,
				'p', Content.itemPCBChip);

		GameRegistry.addRecipe(new ItemStack(Content.blockAssembler),
				"###",
				"#d#",
				"sps",

				'd', Items.diamond,
				'#', Blocks.glass_pane,
				's', Blocks.stone,
				'p', Content.itemPCBChip);

		GameRegistry.addRecipe(new ItemStack(Content.blockPrinter), "iii", "#d#", "sps",

		'i', Items.iron_ingot, '#', Blocks.piston, 's', Blocks.stone, 'p', Content.itemPCBChip);

		GameRegistry.addRecipe(new ItemStack(Content.itemFloppyDisk),
				"iii",
				"i#i",
				"iii",

				'i', Content.itemSiliconDrop,
				'#', Items.redstone);

		GameRegistry.addRecipe(new ItemStack(Content.itemPCB),
				"iii",
				"iii",
				"iii",

				'i', Content.itemSiliconDrop);

		GameRegistry.addRecipe(new ItemStack(Content.itemScrewdriver),
				"i  ",
				" id",
				" di",

				'i', Items.iron_ingot,
				'd', Content.itemSiliconDrop);

		GameRegistry.addRecipe(new ItemStack(Content.itemSolderingIron),
				"i  ",
				" ir",
				" ri",

				'i', Items.iron_ingot,
				'r', Items.redstone);

		GameRegistry.addRecipe(new ItemStack(Content.itemLaser),
				"oii",
				"p##",
				"oii",

				'i', Items.redstone,
				'#', Items.diamond,
				'o', Blocks.obsidian,
				'p', Content.itemPCBChip);

		GameRegistry.addRecipe(new ItemStack(Content.itemLaser),
				"i#i",
				"i#i",
				"opo",

				'i', Items.redstone,
				'#', Items.diamond,
				'o', Blocks.obsidian,
				'p', Content.itemPCBChip);

		GameRegistry.addRecipe(new ItemStack(Content.itemSocket),
				"iri",
				"###",

				'i', Content.itemSiliconDrop,
				'r', Items.redstone,
				'#', Blocks.stone_slab);

		if (IntegratedCircuits.isFMPLoaded) {

			GameRegistry.addRecipe(new ItemStack(Content.itemSocketFMP),
					"iri",
					"###",

					'i', Content.itemSiliconDrop,
					'r', Items.redstone,
					'#', ItemMicroPart.create(1, "tile.stone"));
		}

		// TODO NEI integration? Rewrite using multiple recipes?
		GameRegistry.addRecipe(new RecipeDyeable());
		GameRegistry.addRecipe(new RecipeCircuit());
	}
}
