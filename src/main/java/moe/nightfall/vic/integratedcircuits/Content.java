package moe.nightfall.vic.integratedcircuits;

import moe.nightfall.vic.integratedcircuits.item.Item7Segment;
import moe.nightfall.vic.integratedcircuits.item.ItemBase;
import moe.nightfall.vic.integratedcircuits.item.ItemCircuit;
import moe.nightfall.vic.integratedcircuits.item.ItemFloppyDisk;
import moe.nightfall.vic.integratedcircuits.item.ItemPCB;
import moe.nightfall.vic.integratedcircuits.item.ItemScrewdriver;
import moe.nightfall.vic.integratedcircuits.item.ItemSocket;
import moe.nightfall.vic.integratedcircuits.item.ItemSocketFMP;
import moe.nightfall.vic.integratedcircuits.tile.BlockAssembler;
import moe.nightfall.vic.integratedcircuits.tile.BlockPCBLayout;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityAssembler;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityPCBLayout;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import cpw.mods.fml.common.registry.GameRegistry;

public final class Content {

	public static Item itemSocket;
	public static Item itemSocketFMP;
	public static Item itemCircuit;
	public static Item item7Segment;
	public static Item itemFloppyDisk;
	public static Item itemPCB;

	public static Item itemLaser;
	public static Item itemSolderingIron;
	public static Item itemSilicon;
	public static Item itemSiliconDrop;
	public static Item itemCoalCompound;
	public static Item itemPCBChip;
	public static Item itemScrewdriver;

	public static Block blockSocket;
	public static Block blockPCBLayout;
	public static Block blockAssembler;

	private Content() {
	};

	public static void init() {

		itemSocket = new ItemSocket();
		if (IntegratedCircuits.isFMPLoaded)
			Content.itemSocketFMP = new ItemSocketFMP();

		itemCircuit = new ItemCircuit();
		item7Segment = new Item7Segment();
		itemFloppyDisk = new ItemFloppyDisk();
		itemPCB = new ItemPCB();
		itemPCBChip = new ItemBase("pcb_chip");
		itemLaser = new ItemBase("laser").setHasIcon(false);

		itemSolderingIron = new ItemBase("soldering_iron").setMaxDamage(25).setMaxStackSize(1).setNoRepair();

		itemSiliconDrop = new ItemBase("silicon_drop");
		itemScrewdriver = new ItemScrewdriver();

		if (!(IntegratedCircuits.isBPLoaded || IntegratedCircuits.isPRLoaded)) {
			Content.itemSilicon = new ItemBase("silicon");
			Content.itemCoalCompound = new ItemBase("coalcompound");
		}

		blockPCBLayout = new BlockPCBLayout();
		blockAssembler = new BlockAssembler();

		GameRegistry.registerBlock(Content.blockPCBLayout, Constants.MOD_ID + ".pcblayout");
		GameRegistry.registerBlock(Content.blockAssembler, Constants.MOD_ID + ".assembler");

		GameRegistry.registerTileEntity(TileEntityPCBLayout.class, Constants.MOD_ID + ".pcblayoutcad");
		GameRegistry.registerTileEntity(TileEntityAssembler.class, Constants.MOD_ID + ".assembler");
	}
}
