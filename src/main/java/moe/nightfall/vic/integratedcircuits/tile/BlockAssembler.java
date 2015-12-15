package moe.nightfall.vic.integratedcircuits.tile;

import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockAssembler extends BlockContainer {
	public BlockAssembler() {
		super(Material.iron);
		setUnlocalizedName(Constants.MOD_ID + ".assembler");
		setCreativeTab(IntegratedCircuits.creativeTab);
		setHardness(2F);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote)
			player.openGui(IntegratedCircuits.instance, 1, world, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityAssembler();
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
		int rotation = MathHelper.floor_double((double) (entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		TileEntityAssembler te = (TileEntityAssembler) world.getTileEntity(pos);
		if (te != null)
			te.rotation = rotation;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntityAssembler te = (TileEntityAssembler) world.getTileEntity(pos);
		te.dropContents();
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block block) {
		TileEntityAssembler te = (TileEntityAssembler) world.getTileEntity(pos);
		te.onNeighborBlockChange();
	}
	
	@Override
	public int getWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
		return getStrongPower(world, pos, state, side);
	}

	@Override
	public int getStrongPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
		TileEntityAssembler te = (TileEntityAssembler) world.getTileEntity(pos);
		return te.isProvidingPower();
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side) {
		return side != EnumFacing.DOWN;
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
		return ((TileEntityAssembler) world.getTileEntity(pos)).rotate();
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}
}
