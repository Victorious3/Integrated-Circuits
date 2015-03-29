package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import vic.mod.integratedcircuits.gate.Socket;
import vic.mod.integratedcircuits.gate.ISocket;
import vic.mod.integratedcircuits.gate.PartGate;
import codechicken.lib.vec.BlockCoord;

public abstract class PacketGate<T extends AbstractPacket<T>> extends PacketTileEntity<T>
{
	protected int facing;
	
	public PacketGate() {}
	
	public PacketGate(ISocket part)
	{
		BlockCoord pos = part.getPos();
		this.xCoord = pos.x;
		this.yCoord = pos.y;
		this.zCoord = pos.z;
		this.facing = part.getGate().getSide();
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
	
	protected PartGate getPart(World world)
	{
		return Socket.getGateAt(world, new BlockCoord(xCoord, yCoord, zCoord), facing);
	}
}
