package vic.mod.integratedcircuits;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
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
	
	public AxisAlignedBB getDiskDriveBoundingBox(TileEntityPCBLayout te, int x, int y, int z, Vec3 hitVec)
	{
		int rotation = te.rotation;
		AxisAlignedBB box = null;
		
		switch(rotation) {
		case 1 : box = AxisAlignedBB.getBoundingBox(x + 15 / 16F, y + 1 / 16F, z + 1 / 16F, x + 17 / 16F, y + 1 - 13 / 16F, z + 13 / 16F); break;
		case 2 : box = AxisAlignedBB.getBoundingBox(x + 3 / 16F, y + 1 / 16F, z + 15 / 16F, x + 1 - 1 / 16F, y + 1 - 13 / 16F, z + 17 / 16F); break;
		case 3 : box = AxisAlignedBB.getBoundingBox(x - 1 / 16F, y + 1 / 16F, z + 3 / 16F, x + 1 / 16F, y + 1 - 13 / 16F, z + 15 / 16F); break;
		default : box = AxisAlignedBB.getBoundingBox(x + 1 / 16F, y + 1 / 16F, z - 1 / 16F, x + 1 - 3 / 16F, y + 1 - 13 / 16F, z + 1 / 16F); break;
		}	
		if(!box.isVecInside(hitVec)) return null;
		return box;
	}
	
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(event.action != Action.RIGHT_CLICK_BLOCK) return;
		Block block = event.world.getBlock(event.x, event.y, event.z);
		if(!(block instanceof BlockPCBLayout)) return;
		TileEntityPCBLayout te = (TileEntityPCBLayout)event.world.getTileEntity(event.x, event.y, event.z);
		ItemStack stack = event.entityPlayer.getCurrentEquippedItem();
		
		MovingObjectPosition target = MiscUtils.rayTrace(event.entityPlayer, 1F);	
		AxisAlignedBB box = getDiskDriveBoundingBox(te, event.x, event.y, event.z, target.hitVec);
		if(box == null) return;
		
		if(!event.world.isRemote)
		{
			if(stack == null)
			{
				ItemStack floppy = te.getStackInSlot(0);
				te.setInventorySlotContents(0, null);
				event.entityPlayer.setCurrentItemOrArmor(0, floppy);
			}
			else if(stack.getItem() != null && stack.getItem() == IntegratedCircuits.itemFloppyDisk)
			{
				te.setInventorySlotContents(0, stack);
				event.entityPlayer.setCurrentItemOrArmor(0, null);
			}
			event.useBlock = Result.DENY;
			event.useItem = Result.DENY;
		}
	}
}
