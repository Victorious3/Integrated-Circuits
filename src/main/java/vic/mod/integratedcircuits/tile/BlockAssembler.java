package vic.mod.integratedcircuits.tile;

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
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.Resources;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockAssembler extends BlockContainer
{
	public BlockAssembler() 
	{
		super(Material.iron);
		setBlockName(IntegratedCircuits.modID + ".assembler");
		setCreativeTab(IntegratedCircuits.creativeTab);
		setHardness(2F);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) 
	{
		if(!world.isRemote) player.openGui(IntegratedCircuits.instance, 1, world, x, y, z);
		return true;
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int meta) 
	{
		return new TileEntityAssembler();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) 
	{
		return getIcon(null, side);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int s) 
	{
		return getIcon((TileEntityAssembler)world.getTileEntity(x, y, z), s);
	}
	
	@SideOnly(Side.CLIENT)
	private IIcon getIcon(TileEntityAssembler te, int s)
	{
		int rotation = te != null ? te.rotation : 0;
		
		if(s == 0) return Resources.ICON_ASSEMBLER_BOTTOM;
		else if(s == 1) return Resources.ICON_ASSEMBLER_TOP;
		else if(s == 2 && rotation == 0 || s == 5 && rotation == 1 
			|| s == 3 && rotation == 2 || s == 4 && rotation == 3) 
			return te != null && te.laserHelper.isRunning ? Resources.ICON_ASSEMBLER_FRONT_ON : Resources.ICON_ASSEMBLER_FRONT_OFF;
		else if(s == 3 && rotation == 0 || s == 4 && rotation == 1 
			|| s == 2 && rotation == 2 || s == 5 && rotation == 3) return Resources.ICON_ASSEMBLER_BACK;
		else return Resources.ICON_ASSEMBLER_SIDE;
	}
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) 
	{
		int rotation = MathHelper.floor_double((double)(entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		TileEntityAssembler te = (TileEntityAssembler)world.getTileEntity(x, y, z);
		if(te != null) te.rotation = rotation;
	}

	@Override
	public void onBlockPreDestroy(World world, int x, int y, int z, int meta) 
	{
		TileEntityAssembler te = (TileEntityAssembler)world.getTileEntity(x, y, z);
		te.dropContents();
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) 
	{
		TileEntityAssembler te = (TileEntityAssembler)world.getTileEntity(x, y, z);
		te.onNeighborBlockChange();
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int meta) 
	{
		return isProvidingStrongPower(world, x, y, z, meta);
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int meta) 
	{
		TileEntityAssembler te = (TileEntityAssembler)world.getTileEntity(x, y, z);
		return te.isProvidingPower();
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) 
	{
		return true;
	}

	@Override
	public boolean canProvidePower() 
	{
		return true;
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) 
	{
		return side != -1;
	}

	@Override
	public boolean isOpaqueCube() 
	{
		return false;
	}

	@Override
	public void registerBlockIcons(IIconRegister p_149651_1_) {}
}
