package moe.nightfall.vic.integratedcircuits.item;

import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketWrapper;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TItemMultiPart;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.Method;

@Interface(iface = "codechicken.multipart.TItemMultiPart", modid = "ForgeMultipart")
public class ItemSocketFMP extends ItemBase implements TItemMultiPart {

	public ItemSocketFMP() {
		super("socket_fmp");
		setHasIcon(false);
	}

	public double getHitDepth(Vector3 vhit, int side) {
		return vhit.copy().scalarProject(Rotation.axes[side]) + (side % 2 ^ 1);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		BlockCoord pos = new BlockCoord(x, y, z);
		Vector3 vhit = new Vector3(hitX, hitY, hitZ);
		double d = getHitDepth(vhit, side);

		if (d < 1 && place(stack, player, world, pos, side, vhit))
			return true;

		pos.offset(side);
		return place(stack, player, world, pos, side, vhit);
	}

	private boolean place(ItemStack stack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 vhit)
	{
		BlockCoord pos2 = pos.copy().offset(side ^ 1);
		if (!MiscUtils.canPlaceGateOnSide(world, pos2.x, pos2.y, pos2.z, side))
			return false;

		if (!placeFMP(stack, player, world, pos, side, vhit))
			return false;

		if (!player.capabilities.isCreativeMode)
			stack.stackSize--;

		return true;
	}

	@Method(modid = "ForgeMultipart")
	private boolean placeFMP(ItemStack stack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 vhit)
	{
		TMultiPart part = newPart(stack, player, world, pos, side, vhit);
		if (!TileMultipart.canPlacePart(world, pos, part))
			return false;

		if (!world.isRemote)
			TileMultipart.addPart(world, pos, part);
		return true;
	}

	public TMultiPart newPart(ItemStack stack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 arg5) {
		ISocketWrapper part = (ISocketWrapper) MultiPartRegistry.createPart(Constants.MOD_ID + ".socket_fmp", false);
		part.getSocket().preparePlacement(player, pos, side, stack);
		return (TMultiPart) part;
	}
}
