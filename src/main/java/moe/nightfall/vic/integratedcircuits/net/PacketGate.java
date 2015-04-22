package moe.nightfall.vic.integratedcircuits.net;

import java.io.IOException;

import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.IGate;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketBridge.ISocketBase;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import codechicken.lib.vec.BlockCoord;

public abstract class PacketGate<T extends AbstractPacket<T>> extends PacketTileEntity<T>
{
	protected int facing;
	
	public PacketGate() {}
	
	public PacketGate(ISocketBase part)
	{
		BlockCoord pos = part.getPos();
		this.xCoord = pos.x;
		this.yCoord = pos.y;
		this.zCoord = pos.z;
		this.facing = part.getSide();
	}

	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
		facing = buffer.readInt();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		buffer.writeInt(facing);
	}
	
	protected IGate getPart(World world)
	{
		return IntegratedCircuitsAPI.getSocketAt(world, new BlockCoord(xCoord, yCoord, zCoord), facing).getGate();
	}
}
