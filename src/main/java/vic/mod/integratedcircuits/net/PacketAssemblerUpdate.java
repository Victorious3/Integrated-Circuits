package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.TileEntityAssembler;
import vic.mod.integratedcircuits.client.LaserHelper.Laser;
import cpw.mods.fml.relauncher.Side;

public class PacketAssemblerUpdate extends PacketTileEntity<PacketAssemblerUpdate>
{
	private int x, y, id;
	
	public PacketAssemblerUpdate() {}
	
	public PacketAssemblerUpdate(int x, int y, int id, int xCoord, int yCoord, int zCoord)
	{
		super(xCoord, yCoord, zCoord);
		this.x = x;
		this.y = y;
		this.id = id;
	}

	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
		this.id = buffer.readInt();
		this.x = buffer.readInt();
		this.y = buffer.readInt();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		buffer.writeInt(id);
		buffer.writeInt(x);
		buffer.writeInt(y);
	}

	@Override
	public void process(EntityPlayer player, Side side) 
	{
		TileEntityAssembler te = (TileEntityAssembler)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(te == null) return;
		Laser laser = te.laserHelper.getLaser(id);
		te.loadGateAt(laser.x, laser.y);
		laser.setAim(x, y);
	}
}
