package moe.nightfall.vic.integratedcircuits;

import moe.nightfall.vic.integratedcircuits.item.Item7Segment;
import moe.nightfall.vic.integratedcircuits.item.ItemCircuit;
import moe.nightfall.vic.integratedcircuits.item.ItemFloppyDisk;
import moe.nightfall.vic.integratedcircuits.item.ItemPCB;
import moe.nightfall.vic.integratedcircuits.item.ItemScrewdriver;
import moe.nightfall.vic.integratedcircuits.item.ItemSocket;
import moe.nightfall.vic.integratedcircuits.item.ItemSocketFMP;
import moe.nightfall.vic.integratedcircuits.tile.BlockAssembler;
import moe.nightfall.vic.integratedcircuits.tile.BlockPCBLayout;
import moe.nightfall.vic.integratedcircuits.tile.BlockSocket;
import net.minecraft.item.Item;

public final class Content {

	public static ItemSocket itemSocket;
	public static ItemSocketFMP itemSocketFMP;
	public static ItemCircuit itemCircuit;
	public static Item7Segment item7Segment;
	public static ItemFloppyDisk itemFloppyDisk;
	public static ItemPCB itemPCB;

	public static Item itemLaser;
	public static Item itemSolderingIron;
	public static Item itemSilicon;
	public static Item itemSiliconDrop;
	public static Item itemCoalCompound;
	public static Item itemPCBChip;
	public static ItemScrewdriver itemScrewdriver;

	public static BlockSocket blockSocket;
	public static BlockPCBLayout blockPCBLayout;
	public static BlockAssembler blockAssembler;

	private Content() {
	};
}
