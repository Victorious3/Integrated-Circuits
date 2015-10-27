package moe.nightfall.vic.integratedcircuits.compat;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaFMPAccessor;
import mcp.mobius.waila.api.IWailaFMPProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket.EnumConnectionType;
import moe.nightfall.vic.integratedcircuits.cp.CircuitProperties;
import moe.nightfall.vic.integratedcircuits.gate.GateCircuit;
import moe.nightfall.vic.integratedcircuits.tile.BlockSocket;
import moe.nightfall.vic.integratedcircuits.tile.TileEntitySocket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class WailaAddon implements IWailaDataProvider, IWailaFMPProvider {
	public static void registerAddon(IWailaRegistrar register) {
		WailaAddon instance = new WailaAddon();

		register.registerStackProvider(instance, BlockSocket.class);
		register.registerBodyProvider(instance, BlockSocket.class);
		register.registerBodyProvider(instance, Constants.MOD_ID + ".socket_fmp");
	}

	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
		MovingObjectPosition pos = accessor.getPosition();
		ItemStack stack = accessor.getBlock().getPickBlock(pos, accessor.getWorld(), pos.blockX, pos.blockY,
				pos.blockZ, accessor.getPlayer());
		return stack != null ? stack : accessor.getStack();
	}

	private List<String> getCircuitInformation(ItemStack circuit, List<String> currenttip) {
		NBTTagCompound circuitTag = circuit.getTagCompound().getCompoundTag("circuit");
		NBTTagCompound properties = circuitTag.getCompoundTag("properties");
		currenttip.add(EnumChatFormatting.GOLD + "Name: " + EnumChatFormatting.RESET + properties.getString("name"));
		currenttip
			.add(EnumChatFormatting.GOLD + "Author: " + EnumChatFormatting.RESET + properties.getString("author"));
		int size = circuitTag.getInteger("size");
		currenttip.add(EnumChatFormatting.GOLD + "Size: " + EnumChatFormatting.RESET + size + "x" + size);
		int con = properties.getInteger("con");
		String io = "";
		for (int side = 0; side < 4; side++) {
			EnumConnectionType mode = CircuitProperties.getModeAtSide(con, side);
			io += mode.singleCharID() + (side < 3 ? "-" : "");
		}
		currenttip.add(EnumChatFormatting.GOLD + "IO: " + EnumChatFormatting.RESET + io);
		return currenttip;
	}

	@Override
	public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		return currenttip;
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
		TileEntitySocket te = (TileEntitySocket) accessor.getTileEntity();
		if (te.getSocket().getGate() instanceof GateCircuit)
			return getCircuitInformation(itemStack, currenttip);
		else {
			return currenttip;
		}
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		return currenttip;
	}

	@Override
	public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x,
			int y, int z) {
		return tag;
	}

	@Override
	public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaFMPAccessor accessor,
			IWailaConfigHandler config) {
		return currenttip;
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaFMPAccessor accessor,
			IWailaConfigHandler config) {

		if (itemStack.getTagCompound() != null) {
			return getCircuitInformation(itemStack, currenttip);
		} else {
			return currenttip;
		}
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaFMPAccessor accessor,
			IWailaConfigHandler config) {
		return currenttip;
	}
}
