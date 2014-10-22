package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.TileEntityAssembler;
import cpw.mods.fml.relauncher.Side;

public class PacketAssemblerChangeLaser extends PacketTileEntity<PacketAssemblerChangeLaser>
{
	private int id;
	
	public PacketAssemblerChangeLaser() {}
	
	public PacketAssemblerChangeLaser(int xCoord, int yCoord, int zCoord, int id)
	{
		super(xCoord, yCoord, zCoord);
		this.id = id;
	}
	
	@Override
	public void read(PacketBuffer buffer) throws IOException
	{
		super.read(buffer);
		id = buffer.readInt();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		buffer.writeInt(id);
	}

	@Override
	public void process(EntityPlayer player, Side side) 
	{
		System.out.println("Creating laser: " + id);
		TileEntityAssembler te = (TileEntityAssembler)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(te == null) return;
		te.laserHelper.createLaser(id, te.contents[te.laserHelper.offset + id]);
		System.out.println(te.laserHelper.getLaser(id));
	}
}
