package vic.mod.integratedcircuits.util;

import static net.minecraftforge.common.util.ForgeDirection.EAST;
import static net.minecraftforge.common.util.ForgeDirection.NORTH;
import static net.minecraftforge.common.util.ForgeDirection.SOUTH;
import static net.minecraftforge.common.util.ForgeDirection.WEST;
import vic.mod.integratedcircuits.IntegratedCircuits;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MiscUtils 
{
	@SideOnly(Side.CLIENT)
	public static EntityPlayer thePlayer() 
	{
		return Minecraft.getMinecraft().thePlayer;
	}

	public static <T extends IMessage & IMessageHandler<T, IMessage>> void registerPacket(Class<T> clazz, Side side, int id)
	{
		if(side == null || side == Side.CLIENT) IntegratedCircuits.networkWrapper.registerMessage(clazz, clazz, id, Side.CLIENT);
		if(side == null || side == Side.SERVER) IntegratedCircuits.networkWrapper.registerMessage(clazz, clazz, id, Side.SERVER);
	}

	private static ForgeDirection[] order = new ForgeDirection[]{NORTH, EAST, SOUTH, WEST};
	private static int[] index = new int[]{-1, -1, 0, 2, 3, 1, -1};
	
	public static ForgeDirection rotn(ForgeDirection fd, int offset)
	{
		int pos = index[fd.ordinal()];
		int newPos = pos + offset;
		pos = newPos > 3 ? newPos - 4 : newPos < 0 ? newPos + 4 : newPos;
		return order[pos];
	}
	
	public static ForgeDirection getDirection(int side)
	{
		return order[side];
	}
	
	public static int getSide(ForgeDirection dir)
	{
		return index[dir.ordinal()];
	}

	public static ForgeDirection rot(ForgeDirection fd)
	{
		return rotn(fd, 1);
	}
	
	/** Borrowed from BetterStorage **/
	public static MovingObjectPosition rayTrace(EntityPlayer player, float partialTicks) 
	{
		double range = ((player.worldObj.isRemote)
			? Minecraft.getMinecraft().playerController.getBlockReachDistance()
			: ((EntityPlayerMP)player).theItemInWorldManager.getBlockReachDistance());
		
		Vec3 start = Vec3.createVectorHelper(player.posX, player.posY + 1.62 - player.yOffset, player.posZ);
		Vec3 look = player.getLook(1.0F);
		Vec3 end = start.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range);
		
		MovingObjectPosition target = player.worldObj.rayTraceBlocks(start, end);
		return target;
	}
	
	public static AxisAlignedBB getRotatedInstance(AxisAlignedBB def, int rotation)
	{
		def.offset(-0.5, -0.5, -0.5);
		switch (rotation) {
		case 2 : def = AxisAlignedBB.getBoundingBox(def.minZ, def.minY, def.maxX * -1, def.maxZ, def.maxY, def.minX * -1);
		case 3 : def = AxisAlignedBB.getBoundingBox(def.maxX * -1, def.minY, def.maxZ * -1, def.minX * -1, def.maxY, def.minZ * -1);
		case 1 : def = AxisAlignedBB.getBoundingBox(def.maxZ * -1, def.minY, def.minX, def.minZ * -1, def.maxY, def.maxX);
		}
		def.offset(0.5, 0.5, 0.5);	
		return def;
	}
}
