package moe.nightfall.vic.integratedcircuits;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketProvider;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketWrapper;
import moe.nightfall.vic.integratedcircuits.compat.BPRedstoneProvider;
import moe.nightfall.vic.integratedcircuits.compat.NEIAddon;
import moe.nightfall.vic.integratedcircuits.compat.gateio.GateIO;
import moe.nightfall.vic.integratedcircuits.gate.Gate7Segment;
import moe.nightfall.vic.integratedcircuits.gate.GateCircuit;
import moe.nightfall.vic.integratedcircuits.ic.CircuitPart;
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
import moe.nightfall.vic.integratedcircuits.tile.BlockPCBLayout;
import moe.nightfall.vic.integratedcircuits.tile.BlockSocket;
import moe.nightfall.vic.integratedcircuits.tile.FMPartGate;
import moe.nightfall.vic.integratedcircuits.tile.PartFactory;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityAssembler;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityPCBLayout;
import moe.nightfall.vic.integratedcircuits.tile.TileEntitySocket;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.apache.logging.log4j.Logger;

import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
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
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.api.redstone.IBundledRedstoneProvider;

@Mod(modid = "integratedcircuits", dependencies = "required-after:CodeChickenCore; after:ComputerCraft", guiFactory = "moe.nightfall.vic.integratedcircuits.client.gui.IntegratedCircuitsGuiFactory")
public class IntegratedCircuits {

	public static Class<? extends TileEntity> socketClass;

	// TODO Some of those might be obsolete
	public static boolean isPRLoaded = false;
	public static boolean isAWLoaded = false;
	public static boolean isBPLoaded = false;
	public static boolean isFMPLoaded = false;
	public static boolean isRLLoaded = false;
	public static boolean isMFRLoaded = false;
	public static boolean isCCLoaded = false;

	public static boolean developmentEnvironment;
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

	public static BlockSocket blockGate;
	public static BlockPCBLayout blockPCBLayout;
	public static BlockAssembler blockAssembler;
	public static CreativeTabs creativeTab;

	public static final API API = new API();

	@Instance(Constants.MOD_ID)
	public static IntegratedCircuits instance;

	@SidedProxy(clientSide = "moe.nightfall.vic.integratedcircuits.proxy.ClientProxy", serverSide = "moe.nightfall.vic.integratedcircuits.proxy.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) throws Exception {
		developmentEnvironment = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

		// Initialize API
		Field apiField = IntegratedCircuitsAPI.class.getDeclaredField("instance");
		apiField.setAccessible(true);
		apiField.set(null, API);

		logger = event.getModLog();
		logger.info("Loading Integrated Circutis " + Constants.MOD_VERSION);

		Config.preInitialize(event.getSuggestedConfigurationFile());
		CircuitPart.registerParts();
		Config.postInitialize();

		// Compatibility
		logger.info("Searching for compatible mods");
		logger.info("ProjRed|Transmission: " + (isPRLoaded = Loader.isModLoaded("ProjRed|Transmission")));
		logger.info("armourersWorkshop: " + (isAWLoaded = Loader.isModLoaded("armourersWorkshop")));
		logger.info("bluepower: " + (isBPLoaded = Loader.isModLoaded("bluepower")));
		logger.info("ForgeMultipart: " + (isFMPLoaded = Loader.isModLoaded("ForgeMultipart")));
		logger.info("RedLogic: " + (isRLLoaded = Loader.isModLoaded("RedLogic")));
		logger.info("MineFactoryReloaded: " + (isMFRLoaded = Loader.isModLoaded("MineFactoryReloaded")));
		logger.info("Computer Craft: " + (isCCLoaded = Loader.isModLoaded("ComputerCraft")));

		if (isFMPLoaded)
			logger.info("Forge Multi Part installation found! FMP Compatible gates will be added.");

		proxy.preInitialize();

		creativeTab = new CreativeTabs(Constants.MOD_ID + ".ctab") {
			@Override
			public Item getTabIconItem() {
				// TODO Render with socket
				return itemCircuit;
			}
		};

		IntegratedCircuitsAPI.getGateRegistry().registerGate("circuit", GateCircuit.class);
		IntegratedCircuitsAPI.getGateRegistry().registerGate("7segment", Gate7Segment.class);

		itemSocket = new ItemSocket();
		if (isFMPLoaded)
			itemSocketFMP = new ItemSocketFMP();

		itemCircuit = new ItemCircuit();
		item7Segment = new Item7Segment();
		itemFloppyDisk = new ItemFloppyDisk();
		itemPCB = new ItemPCB();
		itemPCBChip = new ItemBase("pcb_chip");
		itemLaser = new ItemBase("laser").setHasIcon(false);

		itemSolderingIron = new ItemBase("soldering_iron").setMaxDamage(25).setMaxStackSize(1).setNoRepair();

		itemSiliconDrop = new ItemBase("silicon_drop");
		itemScrewdriver = new ItemScrewdriver();

		if (!(isBPLoaded || isPRLoaded)) {
			itemSilicon = new ItemBase("silicon");
			itemCoalCompound = new ItemBase("coalcompound");
		}

		blockPCBLayout = new BlockPCBLayout();
		blockAssembler = new BlockAssembler();

		GameRegistry.registerBlock(blockPCBLayout, Constants.MOD_ID + ".pcblayout");
		GameRegistry.registerBlock(blockAssembler, Constants.MOD_ID + ".assembler");

		GameRegistry.registerTileEntity(TileEntityPCBLayout.class, Constants.MOD_ID + ".pcblayoutcad");
		GameRegistry.registerTileEntity(TileEntityAssembler.class, Constants.MOD_ID + ".assembler");

		// Register socket provider

		if (isFMPLoaded) {
			IntegratedCircuitsAPI.registerSocketProvider(new ISocketProvider() {
				@Override
				public ISocket getSocketAt(World world, BlockCoord pos, int side) {
					TileEntity te = world.getTileEntity(pos.x, pos.y, pos.z);
					if (te instanceof TileMultipart) {
						TileMultipart tm = (TileMultipart) te;
						TMultiPart multipart = tm.partMap(side);
						if (multipart instanceof ISocketWrapper)
							return ((ISocketWrapper) multipart).getSocket();
					}
					return null;
				}
			});
		}

		IntegratedCircuitsAPI.registerSocketProvider(new ISocketProvider() {
			@Override
			public ISocket getSocketAt(World world, BlockCoord pos, int side) {
				TileEntity te = world.getTileEntity(pos.x, pos.y, pos.z);
				if (te instanceof ISocketWrapper) {
					ISocketWrapper wrapper = (ISocketWrapper) te;
					if (wrapper.getSocket().getSide() == side)
						return wrapper.getSocket();
				}
				return null;
			}
		});

		GateIO.initialize();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		blockGate = API.getGateRegistry().createProxyInstance(BlockSocket.class);
		GameRegistry.registerBlock(blockGate, Constants.MOD_ID + ".gate");
		socketClass = API.getGateRegistry().createProxyClass(TileEntitySocket.class);
		GameRegistry.registerTileEntity(socketClass, Constants.MOD_ID + ".gate");

		if (isFMPLoaded) {
			PartFactory.register(Constants.MOD_ID + ".socket_fmp", API.getGateRegistry().createProxyClass(FMPartGate.class));
			PartFactory.initialize();
		}

		if (isCCLoaded) {
			ComputerCraftAPI.registerBundledRedstoneProvider((IBundledRedstoneProvider) blockGate);
			ComputerCraftAPI.registerPeripheralProvider((IPeripheralProvider) blockGate);
		}

		proxy.initialize();

		if (Loader.isModLoaded("NotEnoughItems") && !MiscUtils.isServer())
			new NEIAddon().initialize();

		FMLInterModComms.sendMessage("Waila", "register", "moe.nightfall.vic.integratedcircuits.compat.WailaAddon.registerAddon");
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		IntegratedCircuitsRecipes.loadRecipes();

		if (Config.enableTracker) {
			new Thread() {
				@Override
				public void run() {
					// I would have done it with commons, but it doesn't let me.
					// So this is pretty much copied from AW
					// https://github.com/RiskyKen/Armourers-Workshop
					try {
						String location = "http://bit.ly/1GIaUA6";
						HttpURLConnection conn = null;
						while (location != null && !location.isEmpty()) {
							URL url = new URL(location);
							if (conn != null)
								conn.disconnect();

							conn = (HttpURLConnection) url.openConnection();
							conn.setRequestProperty("User-Agent",
									"Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0");
							conn.setRequestProperty("Referer", "http://" + Constants.MOD_VERSION);
							conn.connect();
							location = conn.getHeaderField("Location");
						}

						if (conn == null)
							throw new NullPointerException();
						String newestVersion = new BufferedReader(new InputStreamReader(conn.getInputStream(),
								Charset.forName("UTF-8"))).readLine();
						// TODO version checker? I don't really like them but we
						// have the information now...
						logger.info("Your version: {}, Newest version: {}", Constants.MOD_VERSION, newestVersion);
						conn.disconnect();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.run();
		}

		// Register provider for bluepower
		if (isBPLoaded)
			new BPRedstoneProvider();

		logger.info("Done! This is an extremely early alpha version so please report any bugs occurring to https://github.com/Victorious3/Integrated-Circuits");
	}
}
