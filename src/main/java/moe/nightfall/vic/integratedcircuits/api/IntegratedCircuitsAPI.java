package moe.nightfall.vic.integratedcircuits.api;

import net.minecraft.world.World;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;

/**
 * Integrated Circuits API.
 * 
 * @author Vic Nightfall
 */
public class IntegratedCircuitsAPI
{
	private static IAPI instance;
	
	public static final Class<?> TILE = findClass("moe.nightfall.vic.integratedcircuits.tile.TileEntitySocket");
	public static final Class<?> TILE_FMP = findClass("moe.nightfall.vic.integratedcircuits.tile.FMPartGate");
	public static final Class<?> BLOCK = findClass("moe.nightfall.vic.integratedcircuits.tile.BlockSocket");
	
	private static Class<?> findClass(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
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
	
	public static MCDataOutput getWriteStream(World world, BlockCoord pos, int side)
	{
		return getInstance().getWriteStream(world, pos, side);
	}
	
	public static IGateRegistry getGateRegistry()
	{
		return getInstance().getGateRegistry();
	}
	
	public static int updateRedstoneInput(ISocket socket, int side) 
	{
		return getInstance().updateRedstoneInput(socket, side);
	}
	
	public static byte[] updateBundledInput(ISocket socket, int side)
	{
		return getInstance().updateBundledInput(socket, side);
	}
}
