package vic.mod.integratedcircuits.misc;

import static net.minecraftforge.common.util.ForgeDirection.EAST;
import static net.minecraftforge.common.util.ForgeDirection.NORTH;
import static net.minecraftforge.common.util.ForgeDirection.SOUTH;
import static net.minecraftforge.common.util.ForgeDirection.WEST;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MiscUtils 
{
	@SideOnly(Side.CLIENT)
	public static EntityPlayer thePlayer() 
	{
		return Minecraft.getMinecraft().thePlayer;
	}

	private static ForgeDirection[] order = {NORTH, EAST, SOUTH, WEST};
	private static int[] index = {-1, -1, 0, 2, 3, 1, -1};
	
	public static ForgeDirection rotn(ForgeDirection fd, int offset)
	{
		int pos = index[fd.ordinal()];
		int newPos = pos + offset;
		pos = newPos > 3 ? newPos - 4 : newPos < 0 ? newPos + 4 : newPos;
		return order[pos];
	}
	
	public static ForgeDirection rot(ForgeDirection fd)
	{
		return rotn(fd, 1);
	}
	
	public static ForgeDirection getDirection(int side)
	{
		return order[side];
	}
	
	public static int getSide(ForgeDirection dir)
	{
		return index[dir.ordinal()];
	}
	
	public static String getLocalizedDirection(ForgeDirection fd)
	{
		return I18n.format("fdirection." + fd.name().toLowerCase() + ".name");
	}
	
	public static AxisAlignedBB getRotatedInstance(AxisAlignedBB def, int rotation)
	{
		def = def.copy();
		def.offset(-0.5, -0.5, -0.5);
		switch (rotation) {
		case 2 : def = AxisAlignedBB.getBoundingBox(def.minZ, def.minY, -def.maxX, def.maxZ, def.maxY, -def.minX);
		case 3 : def = AxisAlignedBB.getBoundingBox(-def.maxX, def.minY, -def.maxZ, -def.minX, def.maxY, -def.minZ);
		case 1 : def = AxisAlignedBB.getBoundingBox(-def.maxZ, def.minY, def.minX, -def.minZ, def.maxY, def.maxX);
		}
		def.offset(0.5, 0.5, 0.5);	
		return def;
	}

	public static boolean canPlaceGateOnSide(World world, int x, int y, int z, int side)
	{
		if(!world.blockExists(x, y, z)) return false;
		Block block = world.getBlock(x, y, z);
		if(block == null) return false;
		return block.isSideSolid(world, x, y, z, ForgeDirection.getOrientation(side));
	}
}
