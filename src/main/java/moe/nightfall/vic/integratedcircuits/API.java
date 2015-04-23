package moe.nightfall.vic.integratedcircuits;

import java.util.List;

import moe.nightfall.vic.integratedcircuits.api.IAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.GateIOProvider;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketProvider;
import moe.nightfall.vic.integratedcircuits.gate.GateRegistry;
import net.minecraft.world.World;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Rotation;

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

	@Override
	public int updateRedstoneInput(ISocket socket, int side)
	{
		int r =  socket.getRotationAbs(side);
		int face = socket.getSide();
		int abs = Rotation.rotateSide(face, r);
		BlockCoord pos = socket.getPos().offset(abs);
		
		int input = 0;
		List<GateIOProvider> providerList = gateRegistry.getIOProviderList(socket.getWrapper().getClass());
		for(GateIOProvider provider : providerList) {
			provider.socket = socket;
			input = provider.calculateRedstoneInput(side, pos, abs);
			if(input != 0) return input;
		}
		return input;
	}

	@Override
	public byte[] updateBundledInput(ISocket socket, int side)
	{
		int r =  socket.getRotationAbs(side);
		int face = socket.getSide();
		int abs = Rotation.rotateSide(face, r);
		BlockCoord pos = socket.getPos().offset(abs);
		
		byte[] input = updateBundledInputNative(socket, side, pos);
		if(input != null) return input;
		
		List<GateIOProvider> providerList = gateRegistry.getIOProviderList(socket.getWrapper().getClass());
		for(GateIOProvider provider : providerList) {
			provider.socket = socket;
			input = provider.calculateBundledInput(side, pos, abs);
			if(input != null) return input;
		}
		return input == null ? new byte[16] : input;
	}
	
	public byte[] updateBundledInputNative(ISocket socket, int side, BlockCoord pos) {
		ISocket neighbour = getSocketAt(socket.getWorld(), pos, socket.getSide());
		if(neighbour != null) return neighbour.getOutput()[(side + 2) % 4];
		return null;
	}
}
