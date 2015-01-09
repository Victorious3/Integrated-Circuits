package vic.mod.integratedcircuits.tile;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import vic.mod.integratedcircuits.gate.PartGate;
import vic.mod.integratedcircuits.proxy.ClientProxy;

public class BlockGate extends BlockContainer
{
	private PartGate gate;
	
	public BlockGate(PartGate gate) 
	{
		super(Material.circuits);
		setBlockName(gate.getType());
		this.gate = gate;
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random random) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		te.getGate().scheduledTick();
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		te.getGate().scheduledTick();
	}

	@Override
	public void onPostBlockPlaced(World world, int x, int y, int z, int meta) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		te.getGate().onAdded();
	}

	@Override
	public boolean renderAsNormalBlock() 
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube() 
	{
		return false;
	}

	@Override
	public int getRenderType() 
	{
		return ClientProxy.GATE_RENDER_ID;
	}

	@Override
	public void registerBlockIcons(IIconRegister ir) {}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) 
	{
		return new TileEntityGate(gate);
	}
}
