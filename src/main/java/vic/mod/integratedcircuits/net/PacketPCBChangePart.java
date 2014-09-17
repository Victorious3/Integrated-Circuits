package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.TileEntityPCBLayout;
import vic.mod.integratedcircuits.ic.CircuitData;
import cpw.mods.fml.relauncher.Side;

public class PacketPCBChangePart extends PacketPCB<PacketPCBChangePart>
{
	private int x, y, id, meta, button;
	boolean ctrl;
	
	public PacketPCBChangePart(){}
	
	public PacketPCBChangePart(int x, int y, int id, int meta, int button, boolean ctrl, int tx, int ty, int tz)
	{
		super(tx, ty, tz);
		this.x = x; this.y = y;
		this.id = id;
		this.meta = meta;
		this.button = button;
		this.ctrl = ctrl;
	}
	
	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
		x = buffer.readInt();
		y = buffer.readInt();
		id = buffer.readInt();
		meta = buffer.readInt();
		button = buffer.readInt();
		ctrl = buffer.readBoolean();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		buffer.writeInt(x);
		buffer.writeInt(y);
		buffer.writeInt(id);
		buffer.writeInt(meta);
		buffer.writeInt(button);
		buffer.writeBoolean(ctrl);
	}

	@Override
	public void process(EntityPlayer player, Side side) 
	{
		TileEntityPCBLayout te = (TileEntityPCBLayout)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(te != null)
		{
			CircuitData cdata = te.getCircuitData();
			if(button != -1) cdata.getPart(x, y).onClick(button, ctrl);
			else
			{
				cdata.setID(x, y, id);
				cdata.setMeta(x, y, meta);
				cdata.getPart(x, y).onPlaced();
				cdata.markForUpdate(x, y);
			}
		}
	}
}
