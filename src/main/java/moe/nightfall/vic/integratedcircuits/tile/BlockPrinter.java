package moe.nightfall.vic.integratedcircuits.tile;

import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockPrinter extends Block {

	public BlockPrinter() {
		super(Material.iron);
		setBlockName(Constants.MOD_ID + ".pcbprinter");
		setCreativeTab(IntegratedCircuits.creativeTab);
		setHardness(2F);
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		return new TileEntityPrinter();
	}

	@Override
	public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis) {
		return ((TileEntityContainer) world.getTileEntity(x, y, z)).rotate();
	}
}
