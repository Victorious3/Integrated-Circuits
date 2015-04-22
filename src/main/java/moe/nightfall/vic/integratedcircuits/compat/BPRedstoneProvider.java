package moe.nightfall.vic.integratedcircuits.compat;

import moe.nightfall.vic.integratedcircuits.api.IGate;
import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import codechicken.lib.vec.BlockCoord;

import com.bluepowermod.api.BPApi;
import com.bluepowermod.api.wire.redstone.IBundledDevice;
import com.bluepowermod.api.wire.redstone.IBundledDeviceWrapper;
import com.bluepowermod.api.wire.redstone.IRedstoneDevice;
import com.bluepowermod.api.wire.redstone.IRedstoneProvider;

public class BPRedstoneProvider implements IRedstoneProvider
{
	public BPRedstoneProvider()
	{
		BPApi.getInstance().getRedstoneApi().registerRedstoneProvider(this);
	}
	
	@Override
	public IRedstoneDevice getRedstoneDeviceAt(World world, int x, int y, int z, ForgeDirection side, ForgeDirection face)
	{
		return (IRedstoneDevice)getBundledDeviceAt(world, x, y, z, side, face);
	}
	
	@Override
	public IBundledDevice getBundledDeviceAt(World world, int x, int y, int z, ForgeDirection side, ForgeDirection face)
	{
		IGate gate = IntegratedCircuitsAPI.getSocketAt(world, new BlockCoord(x, y, z), side.ordinal()).getGate();
		if(gate != null && gate.getProvider() instanceof IBundledDeviceWrapper) 
			return ((IBundledDeviceWrapper)gate.getProvider()).getBundledDeviceOnSide(face);
		return null;
	}
}
