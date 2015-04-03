package moe.nightfall.vic.integratedcircuits.api;

import net.minecraft.world.World;
import codechicken.lib.vec.BlockCoord;

/**
 * Integrated Circuits API.
 * 
 * @author Vic Nightfall
 */
public class IntegratedCircuitsAPI
{
	private static IAPI instance;
	
	public static IAPI getInstance()
	{
		if(instance == null) throw new RuntimeException("Integrated Circuits not installed, aborting!");
		return instance;
	}
	
	public static ISocket getSocketAt(World world, BlockCoord pos, int side)
	{
		return getInstance().getSocketAt(world, pos, side);
	}
	
	public static void registerSocketProvider(ISocketProvider provider)
	{
		getInstance().registerSocketProvider(provider);
	}
	
	public static IGateRegistry getGateRegistry()
	{
		return getInstance().getGateRegistry();
	}
}
