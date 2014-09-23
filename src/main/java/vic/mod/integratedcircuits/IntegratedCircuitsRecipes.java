package vic.mod.integratedcircuits;

import mrtjp.projectred.core.PartDefs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
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
					'r', PartDefs.REDINGOT().makeStack()
		);
		
		GameRegistry.addRecipe(new ItemStack(IntegratedCircuits.blockAssembler), 
			"###",
			"#d#",
			"rrr",
					'd', Items.diamond,
					'#', Blocks.glass_pane,
					'r', PartDefs.REDINGOT().makeStack()
		);
		
		GameRegistry.addRecipe(new ItemStack(IntegratedCircuits.itemFloppyDisk),
			"iii",
			"i#i",
			"iii",
					'i', Items.iron_ingot,
					'#', PartDefs.INFUSEDSILICON().makeStack()
		);
		
	}
}
