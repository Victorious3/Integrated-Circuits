package vic.mod.integratedcircuits;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import vic.mod.integratedcircuits.util.MiscUtils;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

public class CommonProxy 
{
	public void initialize()
	{
		MinecraftForge.EVENT_BUS.register(this);
		registerNetwork();
	}
	
	public void registerNetwork()
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(IntegratedCircuits.instance, new GuiHandler());
	}
	
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(event.action != Action.RIGHT_CLICK_BLOCK) return;
		Block block = event.world.getBlock(event.x, event.y, event.z);
		if(!(block.hasTileEntity(event.world.getBlockMetadata(event.x, event.y, event.z)))) return;
		TileEntity te = (TileEntity)event.world.getTileEntity(event.x, event.y, event.z);
		if(!(te instanceof IDiskDrive)) return;
		IDiskDrive drive = (IDiskDrive) te;
		
		ItemStack stack = event.entityPlayer.getCurrentEquippedItem();
		
		MovingObjectPosition target = MiscUtils.rayTrace(event.entityPlayer, 1F);	
		AxisAlignedBB box = DiskDriveUtils.getDiskDriveBoundingBox(drive, event.x, event.y, event.z, target.hitVec);
		if(box == null) return;
		
		if(!event.world.isRemote)
		{
			if(stack == null)
			{
				ItemStack floppy = drive.getDisk();
				drive.setDisk(null);
				event.entityPlayer.setCurrentItemOrArmor(0, floppy);
			}
			else if(stack.getItem() != null && stack.getItem() == IntegratedCircuits.itemFloppyDisk)
			{
				drive.setDisk(stack);
				event.entityPlayer.setCurrentItemOrArmor(0, null);
			}
			event.useBlock = Result.DENY;
			event.useItem = Result.DENY;
		}
	}
}
