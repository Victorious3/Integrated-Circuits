package vic.mod.integratedcircuits;

import vic.mod.integratedcircuits.net.PacketPCBChangeName;
import vic.mod.integratedcircuits.net.PacketPCBChangePart;
import vic.mod.integratedcircuits.net.PacketPCBReload;
import vic.mod.integratedcircuits.net.PacketPCBUpdate;
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
	public static ItemFloppyDisk itemFloppyDisk;
	
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
    	MiscUtils.registerPacket(PacketPCBUpdate.class, Side.CLIENT, 0);
    	MiscUtils.registerPacket(PacketPCBChangePart.class, Side.SERVER, 1);
    	MiscUtils.registerPacket(PacketPCBReload.class, null, 2);
    	MiscUtils.registerPacket(PacketPCBChangeName.class, null, 3);
    	
    	itemCircuit = new ItemCircuit();
    	GameRegistry.registerItem(itemCircuit, partCircuit, modID);
    	
    	itemFloppyDisk = new ItemFloppyDisk();
    	GameRegistry.registerItem(itemFloppyDisk, modID + "_floppy", modID);
    	
    	blockPCBLayout = new BlockPCBLayout();
    	GameRegistry.registerBlock(blockPCBLayout, modID + ".pcblayout");
    	
    	GameRegistry.registerTileEntity(TileEntityPCBLayout.class, modID + ".pcblayoutcad");
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	MultiPartRegistry.registerParts(new PartFactory(), new String[]{partCircuit});
    	proxy.initialize();
    }
}
