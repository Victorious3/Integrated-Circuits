package vic.mod.integratedcircuits.tile;

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
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.TileEntityAssembler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockAssembler extends BlockContainer
{
	private IIcon front_off, front_on, back, side, bottom, top;
	
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
		
		if(s == 0) return bottom;
		else if(s == 1) return top;
		else if(s == 2 && rotation == 0 || s == 5 && rotation == 1 
			|| s == 3 && rotation == 2 || s == 4 && rotation == 3) 
			return te != null && te.laserHelper.isRunning ? front_on : front_off;
		else if(s == 3 && rotation == 0 || s == 4 && rotation == 1 
			|| s == 2 && rotation == 2 || s == 5 && rotation == 3) return back;
		else return side;
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
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir) 
	{
		front_off = ir.registerIcon(IntegratedCircuits.modID + ":assembler_front_off");	
		front_on = ir.registerIcon(IntegratedCircuits.modID + ":assembler_front_on");
		back = ir.registerIcon(IntegratedCircuits.modID + ":assembler_back");
		bottom = ir.registerIcon(IntegratedCircuits.modID + ":assembler_bottom");
		top = ir.registerIcon(IntegratedCircuits.modID + ":assembler_top");
		side = ir.registerIcon(IntegratedCircuits.modID + ":assembler_side");
	}

	@Override
	public boolean isOpaqueCube() 
	{
		return false;
	}
}
