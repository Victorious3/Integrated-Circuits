package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import vic.mod.integratedcircuits.gate.GateIO;
import vic.mod.integratedcircuits.gate.IGate;
import vic.mod.integratedcircuits.gate.ISocketBridge.ISocketBase;
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
		return GateIO.getGateAt(world, new BlockCoord(xCoord, yCoord, zCoord), facing);
	}
}
