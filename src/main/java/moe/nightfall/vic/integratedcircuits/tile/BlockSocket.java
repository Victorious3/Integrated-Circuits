package moe.nightfall.vic.integratedcircuits.tile;

import java.util.List;
import java.util.Random;

import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.Content;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.compat.gateio.GateIO;
import moe.nightfall.vic.integratedcircuits.gate.Socket;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import codechicken.lib.vec.Cuboid6;

import com.google.common.collect.Lists;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockSocket extends BlockContainer {
	public BlockSocket() {
		super(Material.circuits);
		setBlockName(Constants.MOD_ID + ".gate");
		setHardness(1);

		Socket.box.setBlockBounds(this);
	}

	@Override
	public Item getItemDropped(int par1, Random rand, int par3) {
		return Content.itemSocket;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(x, y, z);
		if (te == null)
			return null;
		ItemStack stack = te.getSocket().pickItem(target);
		return stack != null ? stack : new ItemStack(Content.itemSocket);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer) {
		return true;
	}

	@Override
	public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis) {
		return !world.isRemote &&
			((TileEntitySocket) world.getTileEntity(x, y, z)).getSocket().rotate();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer) {
		return true;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(x, y, z);
		if (te == null)
			return;
		Cuboid6 bounds = Socket.box.copy().apply(Socket.getRotationTransformation(te.getSocket()));
		bounds.setBlockBounds(this);
	}

	public AxisAlignedBB getStatelessBoundingBox(World world, int x, int y, int z) {
		return null;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		if (world.getBlock(x, y, z) != this) { // BUGFIX: mojang randomly calls this method for blocks not in the world!
			return getStatelessBoundingBox(world, x, y, z); // see: net.minecraft.item.ItemBlock.func_150936_a (1.7.10 srg)
		}

		setBlockBoundsBasedOnState(world, x, y, z);
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random random) {
		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(x, y, z);
		te.getSocket().scheduledTick();
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(x, y, z);
		if (!te.isDestroyed)
			te.getSocket().onNeighborChanged();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
			float hitY, float hitZ) {
		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(x, y, z);

		ItemStack heldStack = player.getHeldItem();
		if (heldStack != null) {
			Item item = heldStack.getItem();
			if (Socket.checkItemIsTool(item) && !player.isSneaking()) {
				// return before activate is called if the item right clicked causes a rotation anyway
				if ((IntegratedCircuits.isBPAPIThere && item instanceof com.bluepowermod.api.misc.IScrewdriver)
				        || (IntegratedCircuits.isBCToolsAPIThere && item instanceof buildcraft.api.tools.IToolWrench)) {
					return false;
				}
			}
		}

		return te.getSocket().activate(player,
				new MovingObjectPosition(x, y, z, side, Vec3.createVectorHelper(hitX, hitY, hitZ)),
				heldStack);
	}

	@Override
	public void onBlockPreDestroy(World world, int x, int y, int z, int meta) {
		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(x, y, z);
		List<ItemStack> drops = Lists.newArrayList();
		te.getSocket().addDrops(drops);
		for (ItemStack stack : drops)
			MiscUtils.dropItem(world, stack, x, y, z);
		te.getSocket().onRemoved();
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public int getRenderType() {
		return Constants.GATE_RENDER_ID;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir) {
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntitySocket();
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
		side = GateIO.vanillaToSide(side);

		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(x, y, z);
		ISocket socket = te.getSocket();

		if ((side & 6) == (socket.getSide() & 6))
			return false;
		int rel = socket.getSideRel(side);

		return socket.getConnectionTypeAtSide(rel).isRedstone();
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
		return isProvidingStrongPower(world, x, y, z, side);
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) {
		side ^= 1;

		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(x, y, z);
		ISocket socket = te.getSocket();

		if ((side & 6) == (socket.getSide() & 6))
			return 0;
		int rot = socket.getSideRel(side);
		if (!socket.getConnectionTypeAtSide(side).isRedstone())
			return 0;

		return socket.getRedstoneOutput(rot);
	}
}
