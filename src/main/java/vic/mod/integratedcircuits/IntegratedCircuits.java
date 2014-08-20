package vic.mod.integratedcircuits;

import vic.mod.integratedcircuits.net.PacketUpdatePCB;
import codechicken.multipart.MultiPartRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "integratedcircuits", dependencies = "required-after:ProjRed|Transmission; required-after:ProjRed|Integration;")
public class IntegratedCircuits
{
	public static final String modID = "integratedcircuits";
	public static final String partCircuit = modID + "_circuit";
	
	public static ItemCircuit itemCircuit;
	
	public static BlockPCBLayout blockPCBLayout;
	public static SimpleNetworkWrapper networkWrapper;
	
	@Instance(modID)
	public static IntegratedCircuits instance;
    
	@SidedProxy(clientSide = "vic.mod.integratedcircuits.ClientProxy", serverSide = "vic.mod.integratedcircuits.CommonProxy")
	public static CommonProxy proxy;

    @EventHandler
	public void preInit(FMLPreInitializationEvent event)
    {
    	networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(modID);
    	Misc.registerPacket(PacketUpdatePCB.class, Side.CLIENT, 0);
    	
    	itemCircuit = new ItemCircuit();
    	GameRegistry.registerItem(itemCircuit, partCircuit, modID);
    	
    	blockPCBLayout = new BlockPCBLayout();
    	GameRegistry.registerBlock(blockPCBLayout, modID + ".pcblayout");
    	
    	GameRegistry.registerTileEntity(TileEntityPCBLayout.class, modID + ".pcblayout");
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	MultiPartRegistry.registerParts(new Misc(), new String[]{partCircuit});
    	proxy.initialize();
    }
}
