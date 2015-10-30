package moe.nightfall.vic.integratedcircuits.tile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.DiskDrive;
import moe.nightfall.vic.integratedcircuits.DiskDrive.IDiskDrive;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import moe.nightfall.vic.integratedcircuits.client.Resources;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockCAD extends BlockContainer {
	public BlockCAD() {
		super(Material.iron);
		setBlockName(Constants.MOD_ID + ".pcblayoutcad");
		setCreativeTab(IntegratedCircuits.creativeTab);
		setHardness(2F);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7,
			float par8, float par9) {
		if (!world.isRemote) {
			TileEntityCAD te = (TileEntityCAD) world.getTileEntity(x, y, z);
			int rotation = te.rotation;
			boolean canInteract = rotation == 3 && par6 == 4 || rotation == 0 && par6 == 2 || rotation == 1
					&& par6 == 5 || rotation == 2 && par6 == 3;
			if (canInteract)
				player.openGui(IntegratedCircuits.instance, 0, world, x, y, z);
			return canInteract;
		}
		return true;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		TileEntityCAD te = (TileEntityCAD) world.getTileEntity(x, y, z);
		if (te != null) {
			te.onNeighborBlockChange();
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		int rotation = MathHelper.floor_double((double) (entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		TileEntityCAD te = (TileEntityCAD) world.getTileEntity(x, y, z);
		if (te != null) {
			te.rotation = rotation;
		}
	}

	@Override
	public void onBlockPreDestroy(World world, int x, int y, int z, int meta) {
		DiskDrive.dropFloppy((IDiskDrive) world.getTileEntity(x, y, z), world, x, y, z);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return getIcon(null, side);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int s) {
		return getIcon((TileEntityCAD) world.getTileEntity(x, y, z), s);
	}

	@SideOnly(Side.CLIENT)
	private IIcon getIcon(TileEntityCAD te, int s) {
		boolean on = false;
		int rotation = 2;
		if (te != null && te.playersUsing > 0)
			on = true;
		if (te != null)
			rotation = te.rotation;

		if (rotation == 0 && s == 2 || rotation == 1 && s == 5 || rotation == 2 && s == 3 || rotation == 3 && s == 4)
			return on ? Resources.ICON_CAD_FRONT_ON : Resources.ICON_CAD_FRONT_OFF;
		if (rotation == 0 && s == 3 || rotation == 1 && s == 4 || rotation == 2 && s == 2 || rotation == 3 && s == 5)
			return on ? Resources.ICON_CAD_BACK_ON : Resources.ICON_CAD_BACK_OFF;

		return Resources.ICON_CAD_SIDE;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		TileEntityCAD te = new TileEntityCAD();
		te.setup(32);
		return te;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public void registerBlockIcons(IIconRegister ir) {
	}

	@Override
	public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis) {
		return ((TileEntityContainer) world.getTileEntity(x, y, z)).rotate();
	}
}
