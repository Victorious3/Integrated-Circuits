package vic.mod.integratedcircuits;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import org.apache.logging.log4j.Logger;

import vic.mod.integratedcircuits.compat.NEIAddon;
import vic.mod.integratedcircuits.gate.GateRegistry;
import vic.mod.integratedcircuits.gate.Part7Segment;
import vic.mod.integratedcircuits.gate.PartCircuit;
import vic.mod.integratedcircuits.gate.fmp.PartFactory;
import vic.mod.integratedcircuits.item.Item7Segment;
import vic.mod.integratedcircuits.item.ItemBase;
import vic.mod.integratedcircuits.item.ItemCircuit;
import vic.mod.integratedcircuits.item.ItemFloppyDisk;
import vic.mod.integratedcircuits.item.ItemPCB;
import vic.mod.integratedcircuits.item.ItemScrewdriver;
import vic.mod.integratedcircuits.proxy.CommonProxy;
import vic.mod.integratedcircuits.tile.BlockAssembler;
import vic.mod.integratedcircuits.tile.BlockGate;
import vic.mod.integratedcircuits.tile.BlockPCBLayout;
import vic.mod.integratedcircuits.tile.TileEntityAssembler;
import vic.mod.integratedcircuits.tile.TileEntityGate;
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
import dan200.computercraft.api.ComputerCraftAPI;

@Mod(modid = "integratedcircuits", dependencies = "required-after:CodeChickenCore; after:ComputerCraft")
public class IntegratedCircuits
{
	public static boolean isPRLoaded = false;
	public static boolean isAWLoaded = false;
	public static boolean isBPLoaded = false;
	public static boolean isFMPLoaded = false;
	public static boolean isRLLoaded = false;
	public static boolean isMFRLoaded = false;
	
	public static Logger logger;
	
	public static GateRegistry.ItemGatePair itemCircuit;
	public static GateRegistry.ItemGatePair item7Segment;
	
	public static ItemFloppyDisk itemFloppyDisk;
	public static ItemPCB itemPCB;
	public static ItemBase itemLaser;
	
	public static ItemBase itemSilicon;
	public static ItemBase itemSiliconDrop;
	public static ItemBase itemCoalCompound;
	public static ItemBase itemPCBChip;
	public static ItemScrewdriver itemScrewdriver;
	
	public static BlockGate blockGate;
	public static BlockPCBLayout blockPCBLayout;
	public static BlockAssembler blockAssembler;
	public static CreativeTabs creativeTab;

	@Instance(Constants.MOD_ID)
	public static IntegratedCircuits instance;
    
	@SidedProxy(clientSide = "vic.mod.integratedcircuits.proxy.ClientProxy", serverSide = "vic.mod.integratedcircuits.proxy.CommonProxy")
	public static CommonProxy proxy;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		logger = event.getModLog();
		logger.info("Loading Integrated Circutis " + Constants.MOD_VERSION);
		
		Config.initialize(event.getSuggestedConfigurationFile());
		
		//Compatibility
		logger.info("Searching for compatible mods");
		logger.info("ProjRed|Transmission: " + (isPRLoaded  = Loader.isModLoaded("ProjRed|Transmission")));
		logger.info("armourersWorkshop: "    + (isAWLoaded  = Loader.isModLoaded("armourersWorkshop")));
		logger.info("bluepower: "            + (isBPLoaded  = Loader.isModLoaded("bluepower")));
		logger.info("ForgeMultipart: "       + (isFMPLoaded = Loader.isModLoaded("ForgeMultipart")));
		logger.info("RedLogic: "             + (isRLLoaded  = Loader.isModLoaded("RedLogic")));
		logger.info("MineFactoryReloaded: "  + (isMFRLoaded = Loader.isModLoaded("MineFactoryReloaded")));
		
		if(isFMPLoaded) logger.info("Forge Multi Part installation found! FMP Compatible gates will be added.");
		
		proxy.preInitialize();
		
		creativeTab = new CreativeTabs(Constants.MOD_ID + ".ctab") 
		{
			@Override
			public Item getTabIconItem() 
			{
				return itemCircuit.getItem();
			}
		};
		
		itemCircuit = GateRegistry.registerGate(new PartCircuit(), ItemCircuit.class);
		item7Segment = GateRegistry.registerGate(new Part7Segment(), Item7Segment.class);
		
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

		blockGate = new BlockGate();
		blockPCBLayout = new BlockPCBLayout();
		blockAssembler = new BlockAssembler();
		
		GameRegistry.registerBlock(blockGate, Constants.MOD_ID + ".gate");
		GameRegistry.registerBlock(blockPCBLayout, Constants.MOD_ID + ".pcblayout");
		GameRegistry.registerBlock(blockAssembler, Constants.MOD_ID + ".assembler");
		
		GameRegistry.registerTileEntity(TileEntityPCBLayout.class, Constants.MOD_ID + ".pcblayoutcad");
		GameRegistry.registerTileEntity(TileEntityAssembler.class, Constants.MOD_ID + ".assembler");
		GameRegistry.registerTileEntity(TileEntityGate.class, Constants.MOD_ID + ".gate");
		
		//Computercraft
		ComputerCraftAPI.registerBundledRedstoneProvider(blockGate);
		ComputerCraftAPI.registerPeripheralProvider(blockGate);
		
		if(Loader.isModLoaded("NotEnoughItems")) new NEIAddon().initialize();
	}
    
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		if(isFMPLoaded) PartFactory.initialize();
		proxy.initialize();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		IntegratedCircuitsRecipes.loadRecipes();
		logger.info("Done! This is an extremely early alpha version so please report any bugs occuring to https://github.com/Victorious3/Integrated-Circuits");
	}
}
