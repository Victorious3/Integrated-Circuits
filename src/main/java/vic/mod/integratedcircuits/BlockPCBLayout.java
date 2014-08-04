package vic.mod.integratedcircuits;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockPCBLayout extends BlockContainer
{
	protected BlockPCBLayout() 
	{
		super(Material.iron);
		
		setCreativeTab(CreativeTabs.tabMisc);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) 
	{
		player.openGui(IntegratedCircuits.instance, 0, world, x, y, z);
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) 
	{
		TileEntityPCBLayout te = new TileEntityPCBLayout();
		te.setup(32, 32);
		return te;
	}
}
