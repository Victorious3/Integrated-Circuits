package moe.nightfall.vic.integratedcircuits;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.logging.log4j.Logger;

import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.compat.gateio.GateIO;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPart;
import moe.nightfall.vic.integratedcircuits.gate.Gate7Segment;
import moe.nightfall.vic.integratedcircuits.gate.GateCircuit;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import moe.nightfall.vic.integratedcircuits.tile.BlockSocket;
import moe.nightfall.vic.integratedcircuits.tile.TileEntitySocket;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = "integratedcircuits", guiFactory = "moe.nightfall.vic.integratedcircuits.client.gui.IntegratedCircuitsGuiFactory")
public class IntegratedCircuits {

	public static boolean developmentEnvironment;
	public static Logger logger;

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

		proxy.preInitialize();

		creativeTab = new CreativeTabs(Constants.MOD_ID + ".ctab") {

			private ItemStack iconStack;

			@Override
			public ItemStack getIconItemStack() {
				if (iconStack == null)
					iconStack = new ItemStack(Content.itemCircuit, 1, Integer.MAX_VALUE);
				return iconStack;
			}

			@Override
			public Item getTabIconItem() {
				return null;
			}
		};

		IntegratedCircuitsAPI.getGateRegistry().registerGate("circuit", GateCircuit.class);
		IntegratedCircuitsAPI.getGateRegistry().registerGate("7segment", Gate7Segment.class);

		// Initialize content
		Content.init();

		/*
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
		*/

		// Need to wait for BC
		// if (isBCLoaded)
		// BCAddon.preInit();

		GateIO.initialize();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) throws Exception {
		// Initialize with reflection so that the transformer doesn't run upon
		// constructing this class.
		Content.blockSocket = BlockSocket.class.newInstance();

		GameRegistry.registerBlock(Content.blockSocket, Constants.MOD_ID + ".socket");
		GameRegistry.registerTileEntity(TileEntitySocket.class, Constants.MOD_ID + ".socket");

		/*
		if (isFMPLoaded) {
			PartFactory.register(Constants.MOD_ID + ".socket_fmp", FMPartSocket.class);
			PartFactory.initialize();
		}

		if (isCCLoaded) {
			ComputerCraftAPI.registerBundledRedstoneProvider((IBundledRedstoneProvider) Content.blockSocket);
			ComputerCraftAPI.registerPeripheralProvider((IPeripheralProvider) Content.blockSocket);
		}*/

		proxy.initialize();

		//if (isNEILoaded && !MiscUtils.isServer())
		//	new NEIAddon().initialize();

		//FMLInterModComms.sendMessage("Waila", "register", "moe.nightfall.vic.integratedcircuits.compat.WailaAddon.registerAddon");
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		Recipes.loadRecipes();

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
		// if (isBPLoaded)
		//	new BPRedstoneProvider();

		logger.info("Done! This is an extremely early alpha version so please report any bugs occurring to https://github.com/Victorious3/Integrated-Circuits");
	}
}
