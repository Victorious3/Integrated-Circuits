package vic.mod.integratedcircuits.tile;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import vic.mod.integratedcircuits.Constants;
import vic.mod.integratedcircuits.IntegratedCircuits;

public class BlockCircuitStorage extends BlockContainer
{
	protected BlockCircuitStorage() 
	{
		super(Material.iron);
		setBlockName(Constants.MOD_ID + ".circuitstorage");
		setCreativeTab(IntegratedCircuits.creativeTab);
		setHardness(2F);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) 
	{
		return null;
	}
}
