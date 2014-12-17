package vic.mod.integratedcircuits.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class RayTracer 
{
	/**
	 * Returns a player's ray.
	 * @param player
	 * @param partialTicks
	 * @return Vec3[] 0 = from, 1 = to
	 */
	public static Vec3[] getPlayerRay(EntityPlayer player, float partialTicks)
	{
		Vec3 start = getPositionVector(player, partialTicks);
		Vec3 look = player.getLook(partialTicks);
		double reach = getBlockReachDistance(player);
		Vec3 end = start.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
		return new Vec3[]{start, end};
	}
	
	public static Vec3 getPositionVector(EntityPlayer player, float partialTicks)
	{
		double d0 = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
		double d1 = player.prevPosY + (player.posY - player.prevPosY) * partialTicks + player.getEyeHeight();
		double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
		
		if(!(player instanceof EntityPlayerMP))
			d1 -= player.getDefaultEyeHeight();
		
		return Vec3.createVectorHelper(d0, d1, d2);
	}
	
	/**
	 * Returns the hit distance of a Player, works both on client and server side.
	 * @param player
	 * @return distance
	 */
	public static double getBlockReachDistance(EntityPlayer player)
	{
		if(player instanceof EntityPlayerMP)
			return ((EntityPlayerMP)player).theItemInWorldManager.getBlockReachDistance();
		return Minecraft.getMinecraft().playerController.getBlockReachDistance();
	}
	
	/**
	 * Get the nearest intersecting {@link AxisAlignedBB}, the {@link AxisAlignedBB} selected is returned as {@link MovingObjectPosition#hitInfo}.
	 * Returns {@code null} if none of the {@link AxisAlignedBB AxisAligensBBs} intersect. 
	 * @param player
	 * @param partialTicks
	 * @param alignedAABBs
	 * @return {@link MovingObjectPosition} or {@code null}
	 */
	public static MovingObjectPosition rayTraceAABB(EntityPlayer player, float partialTicks, AxisAlignedBB... alignedAABBs)
	{
		if(alignedAABBs.length == 0) return null;
		MovingObjectPosition nearest = null;
		
		Vec3[] playerRay = getPlayerRay(player, partialTicks);
		Vec3 start = playerRay[0];
		Vec3 end = playerRay[1];

		for(AxisAlignedBB aabb : alignedAABBs)
		{
			if(aabb == null) continue;
			MovingObjectPosition pos = aabb.calculateIntercept(start, end);
			if(pos == null) continue;
			if(nearest == null || pos.hitVec.distanceTo(start) < nearest.hitVec.distanceTo(start))
			{
				nearest = pos;
				nearest.hitInfo = aabb;
			}
		}		
		return nearest;
	}

	public static MovingObjectPosition rayTrace(EntityPlayer player, float partialTicks) 
	{
		Vec3[] playerRay = getPlayerRay(player, partialTicks);
		Vec3 start = playerRay[0];
		Vec3 end = playerRay[1];
		
		MovingObjectPosition target = player.worldObj.rayTraceBlocks(start, end);
		return target;
	}
}
