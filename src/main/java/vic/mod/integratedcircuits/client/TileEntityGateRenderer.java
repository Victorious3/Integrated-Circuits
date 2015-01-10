package vic.mod.integratedcircuits.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import vic.mod.integratedcircuits.proxy.ClientProxy;
import vic.mod.integratedcircuits.tile.TileEntityGate;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Vector3;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class TileEntityGateRenderer extends TileEntitySpecialRenderer implements ISimpleBlockRenderingHandler
{
	@Override 
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		
		CCRenderState.reset();
		CCRenderState.lightMatrix.locate(world, x, y, z);
		te.getGate().renderStatic(new Vector3(x, y, z), 0);
		
		return true;
	} 

	@Override
	public boolean shouldRender3DInInventory(int modelId) 
	{
		return false;
	}

	@Override
	public int getRenderId() 
	{
		return ClientProxy.GATE_RENDER_ID;
	}

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float par5) 
	{
		CCRenderState.reset();
		CCRenderState.pullLightmap();
		CCRenderState.useNormals = true;
		
		((TileEntityGate)te).getGate().renderDynamic(new Vector3(x, y, z), par5, 0);
	}
}
