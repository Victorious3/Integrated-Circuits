package moe.nightfall.vic.integratedcircuits.misc;

import static net.minecraftforge.common.util.ForgeDirection.EAST;
import static net.minecraftforge.common.util.ForgeDirection.NORTH;
import static net.minecraftforge.common.util.ForgeDirection.SOUTH;
import static net.minecraftforge.common.util.ForgeDirection.WEST;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.text.WordUtils;

import com.google.common.collect.HashBiMap;

import codechicken.lib.vec.BlockCoord;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

public class MiscUtils {
	private static ForgeDirection[] order = { NORTH, EAST, SOUTH, WEST };
	private static int[] index = { -1, -1, 0, 2, 3, 1, -1 };
	public static HashBiMap<String, Integer> colors = HashBiMap.create();

	static {
		colors.put("dyeBlack", 15);
		colors.put("dyeRed", 14);
		colors.put("dyeGreen", 13);
		colors.put("dyeBrown", 12);
		colors.put("dyeBlue", 11);
		colors.put("dyePurple", 10);
		colors.put("dyeCyan", 9);
		colors.put("dyeLightGray", 8);
		colors.put("dyeGray", 7);
		colors.put("dyePink", 6);
		colors.put("dyeLime", 5);
		colors.put("dyeYellow", 4);
		colors.put("dyeLightBlue", 3);
		colors.put("dyeMagenta", 2);
		colors.put("dyeOrange", 1);
		colors.put("dyeWhite", 0);
	}

	public static int getColor(ItemStack stack) {
		for (int id : OreDictionary.getOreIDs(stack)) {
			Integer color = colors.get(OreDictionary.getOreName(id));
			if (color != null)
				return color;
		}
		return -1;
	}

	public static String getLocalizedColor(int color) {
		if (color == 8)
			return StatCollector.translateToLocal("item.fireworksCharge.silver");
		else
			return StatCollector.translateToLocal("item.fireworksCharge."
					+ WordUtils.uncapitalize(colors.inverse().get(color).substring(3)));
	}

	public static void playPlaceSound(World world, BlockCoord pos) {
		SoundType sound = world.getBlock(pos.x, pos.y, pos.z).stepSound;
		world.playSoundEffect(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, sound.func_150496_b(),
				(sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
	}

	@SideOnly(Side.CLIENT)
	public static EntityPlayer thePlayer() {
		return Minecraft.getMinecraft().thePlayer;
	}

	public static EntityPlayerMP getPlayerByUUID(UUID uuid) {
		for (Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
			EntityPlayerMP player = (EntityPlayerMP) o;
			if (uuid.equals(player.getGameProfile().getId()))
				return player;
		}
		return null;
	}

	public static EntityPlayerMP getPlayerByUsername(String username) {
		for (Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
			EntityPlayerMP player = (EntityPlayerMP) o;
			if (player.getCommandSenderName().equalsIgnoreCase(username))
				return player;
		}
		return null;
	}

	public static ForgeDirection rotn(ForgeDirection fd, int offset) {
		int pos = index[fd.ordinal()];
		int newPos = pos + offset;
		pos = newPos > 3 ? newPos - 4 : newPos < 0 ? newPos + 4 : newPos;
		return order[pos];
	}

	public static ForgeDirection rot(ForgeDirection fd) {
		return rotn(fd, 1);
	}

	public static ForgeDirection getDirection(int side) {
		return order[side];
	}

	public static int getSide(ForgeDirection dir) {
		return index[dir.ordinal()];
	}

	public static String getLocalizedDirection(ForgeDirection fd) {
		return I18n.format("fdirection." + fd.name().toLowerCase() + ".name");
	}

	public static AxisAlignedBB getRotatedInstance(AxisAlignedBB def, int rotation) {
		def = def.copy();
		def.offset(-0.5, -0.5, -0.5);
		switch (rotation) {
			case 2:
				def = AxisAlignedBB.getBoundingBox(def.minZ, def.minY, -def.maxX, def.maxZ, def.maxY, -def.minX);
			case 3:
				def = AxisAlignedBB.getBoundingBox(-def.maxX, def.minY, -def.maxZ, -def.minX, def.maxY, -def.minZ);
			case 1:
				def = AxisAlignedBB.getBoundingBox(-def.maxZ, def.minY, def.minX, -def.minZ, def.maxY, def.maxX);
		}
		def.offset(0.5, 0.5, 0.5);
		return def;
	}

	public static boolean canPlaceGateOnSide(World world, int x, int y, int z, int side) {
		if (!world.blockExists(x, y, z))
			return false;
		Block block = world.getBlock(x, y, z);
		if (block == null)
			return false;
		return block.isSideSolid(world, x, y, z, ForgeDirection.getOrientation(side));
	}

	public static void dropItem(World world, ItemStack stack, int x, int y, int z) {
		if (stack == null)
			return;
		EntityItem entityItem = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, stack);
		world.spawnEntityInWorld(entityItem);
	}

	public static boolean isClient() {
		return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
	}

	public static boolean isServer() {
		return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER;
	}

	public static float toBinary16Float(int bits) {
		int mant = bits & 0x03FF;
		int exp = bits & 0x7C00;

		if (exp == 0x7C00)
			exp = 0x3FC00;
		else if (exp != 0) {
			exp += 0x1C000;
			if (mant == 0 && exp > 0x1C400)
				return Float.intBitsToFloat((bits & 0x8000) << 16 | exp << 13 | 0x3FF);
		} else if (mant != 0) {
			exp = 0x1C400;
			do {
				mant <<= 1;
				exp -= 0x400;
			} while ((mant & 0x400) == 0);
			mant &= 0x3FF;
		}
		return Float.intBitsToFloat((bits & 0x8000) << 16 | (exp | mant) << 13);
	}

	public static String[] stringNewlineSplit(String toSplit) {
		// ASCII is strange.
		return toSplit.split("\\r\\n|\\n\\r|\\r|\\n");
	}

	public static String stringNormalizeLinefeed(String normalize) {
		return normalize.replaceAll("\\r\\n", "\n");
	}

	public static String[] stringSplitFormat(String toFormat, Object... toInsert) {
		return stringNewlineSplit(String.format(toFormat, toInsert));
	}

	public static List<String> splitTranslateToLocalFormatted(String toTranslate, Object... toInsert) {
		return Arrays.asList(stringNewlineSplit(StatCollector.translateToLocalFormatted(toTranslate, toInsert)));
	}

	public static List<String> appendToAll(Object toAppend, List<String> list) {
		for (int i = list.size() - 1; i >= 0; i--) {
			list.set(i, toAppend + list.get(i));
		}
		return list;
	}

	public static String translate(String unlocalizedName) {
		String localizedName = StatCollector.translateToLocal(unlocalizedName);
		if (localizedName.equals(unlocalizedName)) {
			localizedName = StatCollector.translateToFallback(unlocalizedName);
			if (localizedName.equals(unlocalizedName))
				return null;
		}
		return localizedName;
	}

	public static String translateFormatted(String unlocalizedName, Object... toInsert) {
		String localizedName = StatCollector.translateToLocalFormatted(unlocalizedName, toInsert);
		if (localizedName.equals(unlocalizedName)) {
			localizedName = StatCollector.translateToFallback(unlocalizedName);
			if (localizedName.equals(unlocalizedName))
				return null;
		}
		return localizedName;
	}

}
