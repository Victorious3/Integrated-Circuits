package vic.mod.integratedcircuits.proxy;

import static vic.mod.integratedcircuits.IntegratedCircuits.logger;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import vic.mod.integratedcircuits.Config;
import vic.mod.integratedcircuits.Constants;
import vic.mod.integratedcircuits.DiskDrive;
import vic.mod.integratedcircuits.DiskDrive.IDiskDrive;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.LaserHelper.Laser;
import vic.mod.integratedcircuits.client.gui.GuiHandler;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.misc.RayTracer;
import vic.mod.integratedcircuits.net.AbstractPacket;
import vic.mod.integratedcircuits.net.MCDataOutputImpl;
import vic.mod.integratedcircuits.net.Packet7SegmentChangeMode;
import vic.mod.integratedcircuits.net.Packet7SegmentOpenGui;
import vic.mod.integratedcircuits.net.PacketAssemblerChangeItem;
import vic.mod.integratedcircuits.net.PacketAssemblerChangeLaser;
import vic.mod.integratedcircuits.net.PacketAssemblerStart;
import vic.mod.integratedcircuits.net.PacketAssemblerUpdate;
import vic.mod.integratedcircuits.net.PacketAssemblerUpdateInsufficient;
import vic.mod.integratedcircuits.net.PacketChangeSetting;
import vic.mod.integratedcircuits.net.PacketDataStream;
import vic.mod.integratedcircuits.net.PacketFloppyDisk;
import vic.mod.integratedcircuits.net.PacketPCBChangeInput;
import vic.mod.integratedcircuits.net.PacketPCBChangeName;
import vic.mod.integratedcircuits.net.PacketPCBChangePart;
import vic.mod.integratedcircuits.net.PacketPCBClear;
import vic.mod.integratedcircuits.net.PacketPCBIO;
import vic.mod.integratedcircuits.net.PacketPCBLoad;
import vic.mod.integratedcircuits.net.PacketPCBUpdate;
import vic.mod.integratedcircuits.tile.TileEntityAssembler;
import codechicken.lib.vec.BlockCoord;

import com.google.common.collect.Maps;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class CommonProxy 
{
	public static int serverTicks;
	public static SimpleNetworkWrapper networkWrapper;
	private static HashMap<World, HashMap<BlockCoord, MCDataOutputImpl>> out = Maps.newHashMap();
	
	public void initialize()
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(IntegratedCircuits.instance, new GuiHandler());
	}
	
	public void preInitialize()
	{
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
		
		networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.MOD_ID);
		
		logger.debug("[Common Proxy]: Registering network packets");
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
		AbstractPacket.registerPacket(PacketAssemblerUpdateInsufficient.class, Side.CLIENT, 12);
		
		AbstractPacket.registerPacket(PacketChangeSetting.class, null, 13);
		AbstractPacket.registerPacket(PacketFloppyDisk.class, Side.CLIENT, 14);
		
		AbstractPacket.registerPacket(Packet7SegmentOpenGui.class, Side.CLIENT, 15);
		AbstractPacket.registerPacket(Packet7SegmentChangeMode.class, null, 16);
		
		AbstractPacket.registerPacket(PacketDataStream.class, Side.CLIENT, 17);
	}
	
	public MCDataOutputImpl addStream(World world, BlockCoord crd)
	{
		if(!out.containsKey(world))
			out.put(world, new HashMap<BlockCoord, MCDataOutputImpl>());
		HashMap<BlockCoord, MCDataOutputImpl> map = out.get(world);
		
		if(map.containsKey(crd))
			CommonProxy.networkWrapper.sendToDimension(new PacketDataStream(map.remove(crd), crd.x, crd.y, crd.z), world.provider.dimensionId);
		
		MCDataOutputImpl stream = new MCDataOutputImpl(new ByteArrayOutputStream());
		map.put(crd, stream);
		return stream;
	}
	
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event)
	{
		if(event.phase == Phase.END) 
		{
			serverTicks++;
			
			for(World world : out.keySet())
			{
				HashMap<BlockCoord, MCDataOutputImpl> map = out.get(world);
				for(Entry<BlockCoord, MCDataOutputImpl> entry : map.entrySet())
				{
					BlockCoord crd = entry.getKey();
					CommonProxy.networkWrapper.sendToDimension(new PacketDataStream(map.get(crd), crd.x, crd.y, crd.z), world.provider.dimensionId);
				}
				map.clear();
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerJoined(PlayerLoggedInEvent event)
	{
		if(Config.showStartupMessage)
		{
			ChatComponentText text = new ChatComponentText("[Integrated Circuits] This is an extremely early alpha version so please report any bugs occuring to the ");
			ChatComponentText url = new ChatComponentText("GitHub");
			url.getChatStyle().setUnderlined(true);
			url.getChatStyle().setColor(EnumChatFormatting.BLUE);
			url.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to visit ICs GitHub repo")));
			url.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Victorious3/Integrated-Circuits"));
			text.appendSibling(url);
			text.appendText(" repo.");
			if(event.player.canCommandSenderUseCommand(MinecraftServer.getServer().getOpPermissionLevel(), null))
			text.appendText(" You can disable this message by changing the config file. Thanks for your attention.");
			event.player.addChatComponentMessage(text);
		}
	}
	
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(event.action != Action.RIGHT_CLICK_BLOCK) return;
		Block block = event.world.getBlock(event.x, event.y, event.z);
		if(!(block.hasTileEntity(event.world.getBlockMetadata(event.x, event.y, event.z)))) return;
		TileEntity te = (TileEntity)event.world.getTileEntity(event.x, event.y, event.z);
		
		if(te instanceof IDiskDrive)
		{
			IDiskDrive drive = (IDiskDrive) te;
			
			ItemStack stack = event.entityPlayer.getCurrentEquippedItem();
			
			MovingObjectPosition target = RayTracer.rayTrace(event.entityPlayer, 1F);	
			AxisAlignedBB box = DiskDrive.getDiskDriveBoundingBox(drive, event.x, event.y, event.z, target.hitVec);
			if(box != null)
			{
				if(!event.world.isRemote)
				{
					if(stack == null)
					{
						ItemStack floppy = drive.getDisk();
						drive.setDisk(null);
						event.entityPlayer.setCurrentItemOrArmor(0, floppy);
					}
					else if(stack.getItem() != null && stack.getItem() == IntegratedCircuits.itemFloppyDisk && drive.getDisk() == null)
					{
						drive.setDisk(stack);
						event.entityPlayer.setCurrentItemOrArmor(0, null);
					}
				}
				event.useBlock = Result.DENY;
				event.useItem = Result.DENY;
			}
		}
		if(te instanceof TileEntityAssembler)
		{
			TileEntityAssembler assembler = (TileEntityAssembler)te;
			Pair<AxisAlignedBB, Integer> result = getLaserBoundingBox(assembler, event.x, event.y, event.z, event.entityPlayer, 1);
			if(result.getLeft() != null)
			{
				if(!event.world.isRemote)
				{
					ItemStack holding = event.entityPlayer.getHeldItem();
					ItemStack stack2 = holding;
					if(holding != null) 
					{
						stack2 = holding.copy();
						stack2.stackSize = 1;
					}
					assembler.laserHelper.createLaser(result.getRight(), stack2);
					if(holding == null)
						event.entityPlayer.inventory.setInventorySlotContents(event.entityPlayer.inventory.currentItem, new ItemStack(IntegratedCircuits.itemLaser));
					else if(holding.getItem() == IntegratedCircuits.itemLaser)
					{
						holding.stackSize--;
						if(holding.stackSize <= 0) holding = null;
					}
				}
				event.useBlock = Result.DENY;
				event.useItem = Result.DENY;
			}
		}
	}
	
	public Pair<AxisAlignedBB, Integer> getLaserBoundingBox(TileEntityAssembler te, int x, int y, int z, EntityPlayer player, float partialTicks)
	{
		if(te.getStatus() == te.RUNNING || !player.isSneaking()) return new ImmutablePair(null, null);
		boolean holdsEmpty = player.getHeldItem() == null;
		boolean holdsLaser = !holdsEmpty ? player.getHeldItem().getItem() == IntegratedCircuits.itemLaser : false;
		
		AxisAlignedBB base = AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 8 / 16F, 1).offset(x, y, z);
		AxisAlignedBB boxBase = AxisAlignedBB.getBoundingBox(11 / 16F, 8 / 16F, 11 / 16F, 15 / 16F, 15 / 16F, 15 / 16F);
		AxisAlignedBB box1 = null, box2 = null, box3 = null, box4 = null;
		
		Laser l1 = te.laserHelper.getLaser((te.rotation + 0) % 4);
		if(l1 != null && holdsEmpty || holdsLaser && l1 == null)
			box1 = MiscUtils.getRotatedInstance(boxBase, 2).offset(x, y, z);
		Laser l2 = te.laserHelper.getLaser((te.rotation + 1) % 4);
		if(l2 != null && holdsEmpty || holdsLaser && l2 == null) 
			box2 = MiscUtils.getRotatedInstance(boxBase, 1).offset(x, y, z);
		Laser l3 = te.laserHelper.getLaser((te.rotation + 2) % 4);
		if(l3 != null && holdsEmpty || holdsLaser && l3 == null)
			box3 = MiscUtils.getRotatedInstance(boxBase, 0).offset(x, y, z);
		Laser l4 = te.laserHelper.getLaser((te.rotation + 3) % 4);
		if(l4 != null && holdsEmpty || holdsLaser && l4 == null)
			box4 = MiscUtils.getRotatedInstance(boxBase, 3).offset(x, y, z);
		
		MovingObjectPosition mop = RayTracer.rayTraceAABB(player, partialTicks, base, box1, box2, box3, box4);
		if(mop == null || mop.hitInfo == base) return new ImmutablePair(null, null);
		
		int id = (te.rotation + (mop.hitInfo == box1 ? 0 : mop.hitInfo == box2 ? 1 : mop.hitInfo == box3 ? 2 : mop.hitInfo == box4 ? 3 : 0)) % 4;
		return new ImmutablePair((AxisAlignedBB)mop.hitInfo, id);
	}
}
