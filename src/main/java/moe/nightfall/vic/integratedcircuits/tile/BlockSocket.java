package moe.nightfall.vic.integratedcircuits.tile;

import java.util.List;
import java.util.Random;

import moe.nightfall.vic.integratedcircuits.Constants;
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

public class BlockSocket extends BlockContainer {
	public BlockSocket() {
		super(Material.circuits);
		setBlockName(Constants.MOD_ID + ".gate");
		setHardness(1);

		Socket.box.setBlockBounds(this);
	}

	@Override
	public Item getItemDropped(int par1, Random rand, int par3) {
		return IntegratedCircuits.itemSocket;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(x, y, z);
		if (te == null)
			return null;
		ItemStack stack = te.getSocket().pickItem(target);
		return stack != null ? stack : new ItemStack(IntegratedCircuits.itemSocket);
	}

	@Override
	public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer) {
		return true;
	}

	@Override
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

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
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
		return te.getSocket().activate(player,
				new MovingObjectPosition(x, y, z, side, Vec3.createVectorHelper(hitX, hitY, hitZ)),
				player.getHeldItem());
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
	public void registerBlockIcons(IIconRegister ir) {
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		try {
			return IntegratedCircuits.socketClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
