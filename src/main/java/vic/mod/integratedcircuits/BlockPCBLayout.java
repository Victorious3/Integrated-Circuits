package vic.mod.integratedcircuits;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPCBLayout extends BlockContainer
{
	private IIcon front_off, front_on, back_off, back_on, side;
	
	protected BlockPCBLayout() 
	{
		super(Material.iron);
		setBlockName(IntegratedCircuits.modID + ".pcblayoutcad");
		setCreativeTab(CreativeTabs.tabMisc);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) 
	{
		player.openGui(IntegratedCircuits.instance, 0, world, x, y, z);
		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) 
	{
		int rotation = MathHelper.floor_double((double)(entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		TileEntityPCBLayout te = (TileEntityPCBLayout)world.getTileEntity(x, y, z);
		if(te != null) te.rotation = rotation;
	}

	@Override
	public IIcon getIcon(int side, int meta) 
	{
		return getIcon(null, side);
	}

	@Override
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int s) 
	{
		return getIcon((TileEntityPCBLayout)world.getTileEntity(x, y, z), s);
	}
	
	private IIcon getIcon(TileEntityPCBLayout te, int s)
	{
		boolean on = false;
		int rotation = 2;
		if(te != null && te.playersUsing > 0) on = true;
		if(te != null) rotation = te.rotation;
		
		if(rotation == 0 && s == 2 || rotation == 1 && s == 5 
			|| rotation == 2 && s == 3 || rotation == 3 && s == 4) return on ? front_on : front_off;
		if(rotation == 0 && s == 3 || rotation == 1 && s == 4 
			|| rotation == 2 && s == 2 || rotation == 3 && s == 5) return on ? back_on : back_off;
		
		return side;
	}

	@Override
	public void registerBlockIcons(IIconRegister ir) 
	{
		front_off = ir.registerIcon(IntegratedCircuits.modID + ":cad_front_off");
		front_on = ir.registerIcon(IntegratedCircuits.modID + ":cad_front_on");
		back_off = ir.registerIcon(IntegratedCircuits.modID + ":cad_back_off");
		back_on = ir.registerIcon(IntegratedCircuits.modID + ":cad_back_on");
		side = ir.registerIcon(IntegratedCircuits.modID + ":cad_side");
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) 
	{
		TileEntityPCBLayout te = new TileEntityPCBLayout();
		te.setup(32, 32);
		return te;
	}

	@Override
	public boolean isOpaqueCube() 
	{
		return false;
	}
}
