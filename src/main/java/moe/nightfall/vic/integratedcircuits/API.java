package moe.nightfall.vic.integratedcircuits;

import java.util.List;

import moe.nightfall.vic.integratedcircuits.api.IAPI;
import moe.nightfall.vic.integratedcircuits.api.ISocket;
import moe.nightfall.vic.integratedcircuits.api.ISocketProvider;
import moe.nightfall.vic.integratedcircuits.gate.GateRegistry;
import net.minecraft.world.World;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;

import com.google.common.collect.Lists;

public class API implements IAPI
{
	private List<ISocketProvider> providerList = Lists.newArrayList();
	private GateRegistry gateRegistry = new GateRegistry();
	
	@Override
	public ISocket getSocketAt(World world, BlockCoord pos, int side)
	{
		ISocket socket;
		for(ISocketProvider provider : providerList)
		{
			socket = provider.getSocketAt(world, pos, side);
			if(socket != null) return socket;
		}
		return null;
	}

	@Override
	public void registerSocketProvider(ISocketProvider provider)
	{
		providerList.add(provider);
	}

	@Override
	public GateRegistry getGateRegistry()
	{
		return gateRegistry;
	}

	@Override
	public MCDataOutput getWriteStream(World world, BlockCoord pos, int side)
	{
		return IntegratedCircuits.proxy.addStream(world, pos, side);
	}
}
