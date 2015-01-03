package vic.mod.integratedcircuits.misc;

import static net.minecraftforge.common.util.ForgeDirection.EAST;
import static net.minecraftforge.common.util.ForgeDirection.NORTH;
import static net.minecraftforge.common.util.ForgeDirection.SOUTH;
import static net.minecraftforge.common.util.ForgeDirection.WEST;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.google.common.collect.HashBiMap;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MiscUtils 
{
	private static ForgeDirection[] order = {NORTH, EAST, SOUTH, WEST};
	private static int[] index = {-1, -1, 0, 2, 3, 1, -1};
	public static HashBiMap<String, Integer> colors = HashBiMap.create();
	
	static 
	{
		colors.put("dyeBlack",    15);
		colors.put("dyeRed",      14);
		colors.put("dyeGreen",    13);
		colors.put("dyeBrown",    12);
		colors.put("dyeBlue",     11);
		colors.put("dyePurple",   10);
		colors.put("dyeCyan",      9);
		colors.put("dyeLightGray", 8);
		colors.put("dyeGray",      7);
		colors.put("dyePink",      6);
		colors.put("dyeLime",      5);
		colors.put("dyeYellow",    4);
		colors.put("dyeLightBlue", 3);
		colors.put("dyeMagenta",   2);
		colors.put("dyeOrange",    1);
		colors.put("dyeWhite",     0);
	}
	
	public static int getColor(ItemStack stack)
	{
		for(int id : OreDictionary.getOreIDs(stack))
		{
			Integer color = colors.get(OreDictionary.getOreName(id));
			if(color != null) return color;
		}
		return -1;
	}
	
	public static String getLocalizedColor(int color)
	{
		if(color == 8)
			return I18n.format("item.fireworksCharge.silver");
		else return I18n.format("item.fireworksCharge." + WordUtils.uncapitalize(colors.inverse().get(color).substring(3)));
	}
	
	@SideOnly(Side.CLIENT)
	public static EntityPlayer thePlayer() 
	{
		return Minecraft.getMinecraft().thePlayer;
	}
	
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
	
	//TODO Finish this.
	public static String formatFloat(int digits, float f)
	{
		String out = "";
		if(f % 1.0 != 0)
			out = String.format("%s", f);
		else out = String.format("%.0f", f);
		int index = out.indexOf(".");
		out = StringUtils.repeat('0', digits - out.length() + (index == -1 ? 0 : 1)) + out;
		return out;
	}
	
	public static boolean isClient()
	{
		return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
	}
	
	public static boolean isServer()
	{
		return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER;
	}
}
