package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import vic.mod.integratedcircuits.part.PartGate;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public abstract class PacketGate<T extends AbstractPacket<T>> extends PacketTileEntity<T>
{
	protected int facing;
	
	public PacketGate() {}
	
	public PacketGate(PartGate part)
	{
		super(part.x(), part.y(), part.z());
		this.facing = part.getFace();
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
		TileMultipart tm = (TileMultipart)world.getTileEntity(xCoord, yCoord, zCoord);
		if(tm == null) return null;
		TMultiPart part = tm.partMap(this.facing);
		if(part == null || !(part instanceof PartGate)) return null;
		return (PartGate)part;
	}
}
