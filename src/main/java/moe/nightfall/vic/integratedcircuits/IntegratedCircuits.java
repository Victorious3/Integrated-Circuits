package moe.nightfall.vic.integratedcircuits;

import moe.nightfall.vic.integratedcircuits.compat.BPRedstoneProvider;
import moe.nightfall.vic.integratedcircuits.compat.NEIAddon;
import moe.nightfall.vic.integratedcircuits.gate.Gate7Segment;
import moe.nightfall.vic.integratedcircuits.gate.GateCircuit;
import moe.nightfall.vic.integratedcircuits.gate.GateRegistry;
import moe.nightfall.vic.integratedcircuits.gate.fmp.FMPartGate;
import moe.nightfall.vic.integratedcircuits.gate.fmp.PartFactory;
import moe.nightfall.vic.integratedcircuits.item.Item7Segment;
import moe.nightfall.vic.integratedcircuits.item.ItemBase;
import moe.nightfall.vic.integratedcircuits.item.ItemCircuit;
import moe.nightfall.vic.integratedcircuits.item.ItemFloppyDisk;
import moe.nightfall.vic.integratedcircuits.item.ItemPCB;
import moe.nightfall.vic.integratedcircuits.item.ItemScrewdriver;
import moe.nightfall.vic.integratedcircuits.item.ItemSocket;
import moe.nightfall.vic.integratedcircuits.item.ItemSocketFMP;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import moe.nightfall.vic.integratedcircuits.tile.BlockAssembler;
import moe.nightfall.vic.integratedcircuits.tile.BlockGate;
import moe.nightfall.vic.integratedcircuits.tile.BlockPCBLayout;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityAssembler;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityGate;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityPCBLayout;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
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
				// TODO Render with socket
				return itemCircuit;
			}
		};
		
		GateRegistry.registerGate("circuit", GateCircuit.class);
		GateRegistry.registerGate("7segment", Gate7Segment.class);
		
		itemSocket = new ItemSocket();
		if(isFMPLoaded) itemSocketFMP = new ItemSocketFMP();
		
		itemCircuit = new ItemCircuit();
		item7Segment = new Item7Segment();
		
		if(isFMPLoaded) PartFactory.register(Constants.MOD_ID + ".socket_fmp", FMPartGate.class);
		
		itemFloppyDisk = new ItemFloppyDisk();
		itemPCB = new ItemPCB();
		itemPCBChip = new ItemBase("pcb_chip");
		itemLaser = new ItemBase("laser").setHasIcon(false);
		
		itemSolderingIron = new ItemBase("soldering_iron")
			.setMaxDamage(25)
			.setMaxStackSize(1)
			.setNoRepair();
		
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
		
		if(Loader.isModLoaded("NotEnoughItems") && !MiscUtils.isServer()) 
			new NEIAddon().initialize();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		if(isFMPLoaded) PartFactory.initialize();
		proxy.initialize();
		
		FMLInterModComms.sendMessage("Waila", "register", "vic.mod.integratedcircuits.compat.WailaAddon.registerAddon");
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		IntegratedCircuitsRecipes.loadRecipes();
		
		//Register provider for bluepower
		if(isBPLoaded) new BPRedstoneProvider();
		
		logger.info("Done! This is an extremely early alpha version so please report any bugs occurring to https://github.com/Victorious3/Integrated-Circuits");
	}
}
