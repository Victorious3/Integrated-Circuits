package moe.nightfall.vic.integratedcircuits.tile;

import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockPrinter extends BlockContainer {

	public BlockPrinter() {
		super(Material.iron);
		setBlockName(Constants.MOD_ID + ".pcbprinter");
		setCreativeTab(IntegratedCircuits.creativeTab);
		setHardness(2F);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntityPrinter();
	}
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		int rotation = MathHelper.floor_double((double) (entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		TileEntityPrinter te = (TileEntityPrinter) world.getTileEntity(x, y, z);
		if (te != null)
			te.rotation = rotation;
	}

	@Override
	public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis) {
		return ((TileEntityContainer) world.getTileEntity(x, y, z)).rotate();
	}

	@Override
	public int getRenderType() {
		return Constants.PRINTER_RENDER_ID;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}
}
