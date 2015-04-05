package moe.nightfall.vic.integratedcircuits.api;

import net.minecraft.world.World;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;

public interface IAPI
{
	public ISocket getSocketAt(World world, BlockCoord pos, int side);
	
	public void registerSocketProvider(ISocketProvider provider);
	
	public MCDataOutput getWriteStream(World world, BlockCoord pos, int side);
	
	public IGateRegistry getGateRegistry();
}
