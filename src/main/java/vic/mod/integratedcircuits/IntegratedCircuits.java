package vic.mod.integratedcircuits;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import vic.mod.integratedcircuits.item.ItemBase;
import vic.mod.integratedcircuits.item.ItemCircuit;
import vic.mod.integratedcircuits.item.ItemFloppyDisk;
import vic.mod.integratedcircuits.item.ItemLaser;
import vic.mod.integratedcircuits.item.ItemPCB;
import vic.mod.integratedcircuits.net.AbstractPacket;
import vic.mod.integratedcircuits.net.PacketAssemblerChangeItem;
import vic.mod.integratedcircuits.net.PacketAssemblerChangeLaser;
import vic.mod.integratedcircuits.net.PacketAssemblerStart;
import vic.mod.integratedcircuits.net.PacketAssemblerUpdate;
import vic.mod.integratedcircuits.net.PacketChangeSetting;
import vic.mod.integratedcircuits.net.PacketFloppyDisk;
import vic.mod.integratedcircuits.net.PacketPCBChangeInput;
import vic.mod.integratedcircuits.net.PacketPCBChangeName;
import vic.mod.integratedcircuits.net.PacketPCBChangePart;
import vic.mod.integratedcircuits.net.PacketPCBClear;
import vic.mod.integratedcircuits.net.PacketPCBIO;
import vic.mod.integratedcircuits.net.PacketPCBLoad;
import vic.mod.integratedcircuits.net.PacketPCBUpdate;
import vic.mod.integratedcircuits.part.PartFactory;
import vic.mod.integratedcircuits.proxy.CommonProxy;
import vic.mod.integratedcircuits.tile.BlockAssembler;
import vic.mod.integratedcircuits.tile.BlockPCBLayout;
import codechicken.multipart.MultiPartRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "integratedcircuits", dependencies = "required-after:ForgeMultipart;")
public class IntegratedCircuits
{
	public static boolean isPRLoaded = false;
	public static boolean isAWLoaded = false;
	public static boolean isBPLoaded = false;
	
	public static final String modID = "integratedcircuits";
	public static final String partCircuit = modID + "_circuit";
	
	public static ItemCircuit itemCircuit;
	public static ItemFloppyDisk itemFloppyDisk;
	public static ItemPCB itemPCB;
	public static ItemLaser itemLaser;
	
	public static ItemBase itemSilicon;
	public static ItemBase itemSiliconDrop;
	public static ItemBase itemCoalCompound;
	
	public static BlockPCBLayout blockPCBLayout;
	public static BlockAssembler blockAssembler;
	public static SimpleNetworkWrapper networkWrapper;
	
	public static CreativeTabs creativeTab;
	
	@Instance(modID)
	public static IntegratedCircuits instance;
    
	@SidedProxy(clientSide = "vic.mod.integratedcircuits.proxy.ClientProxy", serverSide = "vic.mod.integratedcircuits.proxy.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		Config.initialize(event.getSuggestedConfigurationFile());
		networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(modID);
		
		AbstractPacket.registerPacket(PacketPCBUpdate.class, Side.CLIENT, 0);
		AbstractPacket.registerPacket(PacketPCBChangePart.class, Side.SERVER, 1);
		AbstractPacket.registerPacket(PacketPCBClear.class, null, 2);
		AbstractPacket.registerPacket(PacketPCBChangeName.class, null, 3);
		AbstractPacket.registerPacket(PacketPCBIO.class, Side.SERVER, 4);
		AbstractPacket.registerPacket(PacketPCBChangeInput.class, null, 5);
		AbstractPacket.registerPacket(PacketPCBLoad.class, Side.CLIENT, 6);
		
		AbstractPacket.registerPacket(PacketAssemblerStart.class, null, 7);
		AbstractPacket.registerPacket(PacketAssemblerUpdate.class, Side.CLIENT, 9);
		AbstractPacket.registerPacket(PacketAssemblerChangeLaser.class, Side.CLIENT, 10);
		AbstractPacket.registerPacket(PacketAssemblerChangeItem.class, Side.CLIENT, 11);
		
		AbstractPacket.registerPacket(PacketChangeSetting.class, null, 12);
		AbstractPacket.registerPacket(PacketFloppyDisk.class, Side.CLIENT, 13);
		
		creativeTab = new CreativeTabs(modID + ".ctab") 
		{
			@Override
			public Item getTabIconItem() 
			{
				return itemCircuit;
			}
		};

		itemCircuit = new ItemCircuit();
		itemFloppyDisk = new ItemFloppyDisk();
		itemPCB = new ItemPCB();
		itemLaser = new ItemLaser();
		
		itemSilicon = new ItemBase("silicon");
		itemSiliconDrop = new ItemBase("silicon_drop");
		itemCoalCompound = new ItemBase("coalcompound");
		
		GameRegistry.registerItem(itemCircuit, partCircuit, modID);  		
		
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
		isPRLoaded = Loader.isModLoaded("ProjRed|Transmission");
		isAWLoaded = Loader.isModLoaded("armourersWorkshop");
		isBPLoaded = Loader.isModLoaded("bluepower");
		
		MultiPartRegistry.registerParts(new PartFactory(), new String[]{partCircuit});
		proxy.initialize();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		IntegratedCircuitsRecipes.loadRecipes();
	}
}
