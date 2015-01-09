package vic.mod.integratedcircuits;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import vic.mod.integratedcircuits.compat.NEIAddon;
import vic.mod.integratedcircuits.item.Item7Segment;
import vic.mod.integratedcircuits.item.ItemBase;
import vic.mod.integratedcircuits.item.ItemCircuit;
import vic.mod.integratedcircuits.item.ItemFloppyDisk;
import vic.mod.integratedcircuits.item.ItemPCB;
import vic.mod.integratedcircuits.item.ItemScrewdriver;
import vic.mod.integratedcircuits.part.Part7Segment;
import vic.mod.integratedcircuits.part.PartCircuit;
import vic.mod.integratedcircuits.part.fmp.PartFactory;
import vic.mod.integratedcircuits.proxy.CommonProxy;
import vic.mod.integratedcircuits.tile.BlockAssembler;
import vic.mod.integratedcircuits.tile.BlockPCBLayout;
import vic.mod.integratedcircuits.tile.TileEntityAssembler;
import vic.mod.integratedcircuits.tile.TileEntityPCBLayout;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = "integratedcircuits", dependencies = "required-after:ForgeMultipart;")
public class IntegratedCircuits
{
	public static boolean isPRLoaded = false;
	public static boolean isAWLoaded = false;
	public static boolean isBPLoaded = false;
	
	public static final String modID = "integratedcircuits";
	
	public static ItemCircuit itemCircuit;
	public static Item7Segment item7Segment;
	public static ItemFloppyDisk itemFloppyDisk;
	public static ItemPCB itemPCB;
	public static ItemBase itemLaser;
	
	public static ItemBase itemSilicon;
	public static ItemBase itemSiliconDrop;
	public static ItemBase itemCoalCompound;
	public static ItemBase itemPCBChip;
	public static ItemScrewdriver itemScrewdriver;
	
	public static BlockPCBLayout blockPCBLayout;
	public static BlockAssembler blockAssembler;
	public static CreativeTabs creativeTab;
	
	@Instance(modID)
	public static IntegratedCircuits instance;
    
	@SidedProxy(clientSide = "vic.mod.integratedcircuits.proxy.ClientProxy", serverSide = "vic.mod.integratedcircuits.proxy.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		isPRLoaded = Loader.isModLoaded("ProjRed|Transmission");
		isAWLoaded = Loader.isModLoaded("armourersWorkshop");
		isBPLoaded = Loader.isModLoaded("bluepower");
		
		if(Loader.isModLoaded("NotEnoughItems")) new NEIAddon().initialize();
		
		Config.initialize(event.getSuggestedConfigurationFile());
		proxy.preInitialize();
		
		creativeTab = new CreativeTabs(modID + ".ctab") 
		{
			@Override
			public Item getTabIconItem() 
			{
				return itemCircuit;
			}
		};
		
		itemCircuit = new ItemCircuit(new PartCircuit());
		item7Segment = new Item7Segment(new Part7Segment());
		
		itemFloppyDisk = new ItemFloppyDisk();
		itemPCB = new ItemPCB();
		itemPCBChip = new ItemBase("pcb_chip");
		itemLaser = new ItemBase("laser").setHasIcon(false);
		
		itemSiliconDrop = new ItemBase("silicon_drop");
		itemScrewdriver = new ItemScrewdriver();
		
		if(!(isBPLoaded || isPRLoaded))
		{
			itemSilicon = new ItemBase("silicon");
			itemCoalCompound = new ItemBase("coalcompound");
		}

		blockPCBLayout = new BlockPCBLayout();
		blockAssembler = new BlockAssembler();
		
		GameRegistry.registerBlock(blockPCBLayout, modID + ".pcblayout");
		GameRegistry.registerBlock(blockAssembler, modID + ".assembler");
		
		GameRegistry.registerTileEntity(TileEntityPCBLayout.class, modID + ".pcblayoutcad");
		GameRegistry.registerTileEntity(TileEntityAssembler.class, modID + ".assembler");
	}
    
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		PartFactory.initialize();
		proxy.initialize();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		IntegratedCircuitsRecipes.loadRecipes();
	}
}
