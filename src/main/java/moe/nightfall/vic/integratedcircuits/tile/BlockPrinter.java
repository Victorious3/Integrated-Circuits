package moe.nightfall.vic.integratedcircuits.tile;

import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import moe.nightfall.vic.integratedcircuits.client.Resources;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;


public class BlockPrinter extends BlockContainer {

	public BlockPrinter() {
		super(Material.iron);
		setBlockName(Constants.MOD_ID + ".pcbprinter");
		setCreativeTab(IntegratedCircuits.creativeTab);
		setHardness(2F);
		setBlockBounds(0, 0, 0, 1F, 0.5F, 1F);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntityPrinter();
	}

	@Override
	public IIcon getIcon(int par1, int par2) {
		return Resources.ICON_ASSEMBLER_BOTTOM;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		int rotation = MathHelper.floor_double((double) (entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		TileEntityPrinter te = (TileEntityPrinter) world.getTileEntity(x, y, z);
		if (te != null)
			te.rotation = rotation;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float hitX,
			float hitY, float hitZ) {
		TileEntityPrinter te = (TileEntityPrinter) world.getTileEntity(x, y, z);
		if (!world.isRemote) {
			ItemStack stack = player.getCurrentEquippedItem();
			if (te != null) {
				if (stack != null) {
					boolean b = te.addInk(stack);
					if (!b)
						b = te.addPaper(stack);
					if (b) {
						world.markBlockForUpdate(x, y, z);
					}
					if (stack.stackSize < 1) {
						player.setCurrentItemOrArmor(0, null);
					}
				} else {
					if (te.paperCount() > 0 && player.isSneaking()) {
						player.setCurrentItemOrArmor(0, te.getStackInSlot(0));
						te.setInventorySlotContents(0, null);
						world.markBlockForUpdate(x, y, z);
					}
				}
			}
		}
		return false;
	}

	@Override
	public void onBlockPreDestroy(World world, int x, int y, int z, int meta) {
		TileEntityPrinter te = (TileEntityPrinter) world.getTileEntity(x, y, z);
		if (te != null)
			MiscUtils.dropItem(world, te.getStackInSlot(0), x, y, z);
	}

	@Override
	public boolean rotateBlock(World world, int x, int y, int z, EnumFacing axis) {
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
