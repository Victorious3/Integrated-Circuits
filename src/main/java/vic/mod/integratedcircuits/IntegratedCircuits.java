package vic.mod.integratedcircuits;

import codechicken.multipart.MultiPartRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = "integratedcircuits", dependencies = "required-after:ProjRed|Transmission; required-after:ProjRed|Integration;")
public class IntegratedCircuits
{
	public static final String modID = "integratedcircuits";
	public static final String partCircuit = modID + "_circuit";
	
	public static ItemCircuit itemCircuit;
	
	@Instance(modID)
	public static IntegratedCircuits instance;
    
	@SidedProxy(clientSide = "vic.mod.integratedcircuits.ClientProxy", serverSide = "vic.mod.integratedcircuits.CommonProxy")
	public static CommonProxy proxy;

    @EventHandler
	public void preInit(FMLPreInitializationEvent event) 
    {
    	SubLogicPart.simulation();
    	itemCircuit = new ItemCircuit();
    	GameRegistry.registerItem(itemCircuit, partCircuit, modID);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	MultiPartRegistry.registerParts(new Content(), new String[]{partCircuit});
    	proxy.initialize();
    }
}
