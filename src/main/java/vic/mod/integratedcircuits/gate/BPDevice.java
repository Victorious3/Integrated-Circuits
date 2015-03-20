package vic.mod.integratedcircuits.gate;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.bluepowermod.api.BPApi;
import com.bluepowermod.api.misc.MinecraftColor;
import com.bluepowermod.api.wire.ConnectionType;
import com.bluepowermod.api.wire.IConnectionCache;
import com.bluepowermod.api.wire.redstone.IBundledDevice;
import com.bluepowermod.api.wire.redstone.IRedstoneDevice;

public class BPDevice implements IBundledDevice, IRedstoneDevice
{
	private final PartGate gate;
	
	private final IConnectionCache<IBundledDevice> cacheBundled;
	private final IConnectionCache<IRedstoneDevice> cacheSimple;
	
	public BPDevice(PartGate gate) 
	{
		this.gate = gate;
		this.cacheBundled = BPApi.getInstance().getRedstoneApi().createBundledConnectionCache(this);
		this.cacheSimple = BPApi.getInstance().getRedstoneApi().createRedstoneConnectionCache(this);
	}
	
	@Override
	public int getX()
	{
		return gate.getProvider().getPos().x;
	}

	@Override
	public int getY()
	{
		return gate.getProvider().getPos().y;
	}

	@Override
	public int getZ()
	{
		return gate.getProvider().getPos().z;
	}
	
	@Override
	public World getWorld()
	{
		return gate.getProvider().getWorld();
	}

	@Override
	public boolean canConnect(ForgeDirection side, IBundledDevice dev, ConnectionType type)
	{
		return gate.canConnectBundledImpl(gate.getSideRel(side.ordinal()));
	}

	@Override
	public IConnectionCache<? extends IBundledDevice> getBundledConnectionCache()
	{
		return cacheBundled;
	}

	@Override
	public byte[] getBundledOutput(ForgeDirection side)
	{
		return gate.getBundledOutput(gate.getSideRel(side.ordinal()));
	}

	@Override
	public void setBundledPower(ForgeDirection side, byte[] power)
	{
		gate.updateInputPre();
		gate.setInput(gate.getSideRel(side.ordinal()), power);
	}

	@Override
	public byte[] getBundledPower(ForgeDirection side)
	{
		return gate.getBundledInput(gate.getSideRel(side.ordinal()));
	}

	@Override
	public void onBundledUpdate()
	{
		gate.updateInputPost();
	}

	@Override
	public MinecraftColor getBundledColor(ForgeDirection side)
	{
		return MinecraftColor.ANY;
	}

	@Override
	public boolean isNormalFace(ForgeDirection side)
	{
		return true;
	}

	@Override
	public boolean canConnect(ForgeDirection side, IRedstoneDevice dev, ConnectionType type)
	{
		return gate.canConnectRedstoneImpl(gate.getSideRel(side.ordinal()));
	}

	@Override
	public IConnectionCache<? extends IRedstoneDevice> getRedstoneConnectionCache()
	{
		return cacheSimple;
	}

	@Override
	public byte getRedstonePower(ForgeDirection side)
	{
		return (byte)(gate.getRedstoneOutput(gate.getSideRel(side.ordinal())) != 0 ? -1 : 0);
	}

	@Override
	public void setRedstonePower(ForgeDirection side, byte power)
	{
		gate.updateInputPre();
		gate.setInput(gate.getSideRel(side.ordinal()), 0, power);
	}

	@Override
	public void onRedstoneUpdate()
	{
		gate.updateInputPost();
	}
}
