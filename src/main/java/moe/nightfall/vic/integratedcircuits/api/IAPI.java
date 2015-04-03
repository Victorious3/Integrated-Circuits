package moe.nightfall.vic.integratedcircuits.api;

import net.minecraft.world.World;
import codechicken.lib.vec.BlockCoord;

public interface IAPI
{
	public ISocket getSocketAt(World world, BlockCoord pos, int side);
	
	public void registerSocketProvider(ISocketProvider provider);
	
	public IGateRegistry getGateRegistry();
}
