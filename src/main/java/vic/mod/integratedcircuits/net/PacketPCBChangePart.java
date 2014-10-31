package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.TileEntityPCBLayout;
import vic.mod.integratedcircuits.ic.CircuitData;
import cpw.mods.fml.relauncher.Side;

public class PacketPCBChangePart extends PacketTileEntity<PacketPCBChangePart>
{
	private int size;
	private int[] data;
	private int button;
	private boolean ctrl, placed;
	
	public PacketPCBChangePart(){}
	
	//TODO I do absolutely not like this.
	public PacketPCBChangePart(int data[], int button, boolean ctrl, boolean placed, int tx, int ty, int tz)
	{
		this(data, button, ctrl, tx, ty, tz);
		this.placed = placed;
	}
	
	public PacketPCBChangePart(int data[], int button, boolean ctrl, int tx, int ty, int tz)
	{
		super(tx, ty, tz);
		this.button = button;
		this.ctrl = ctrl;
		this.size = data.length;
		this.data = data;
	}
	
	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
		button = buffer.readInt();
		ctrl = buffer.readBoolean();
		placed = buffer.readBoolean();
		size = buffer.readInt();
		data = new int[size];
		for(int i = 0; i < size; i++)
			data[i] = buffer.readInt();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		buffer.writeInt(button);
		buffer.writeBoolean(ctrl);
		buffer.writeBoolean(placed);
		buffer.writeInt(size);
		for(int i : data)
			buffer.writeInt(i);
	}

	@Override
	public void process(EntityPlayer player, Side side) 
	{
		TileEntityPCBLayout te = (TileEntityPCBLayout)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(te != null)
		{
			CircuitData cdata = te.getCircuitData();
			for(int i = 0; i < size; i += 4)
			{	
				int x = data[i];
				int y = data[i + 1];
				if(button != -1) cdata.getPart(x, y).onClick(button, ctrl);
				else
				{
					cdata.setID(x, y, data[i + 2]);
					cdata.setMeta(x, y, data[i + 3]);
					if(placed) cdata.getPart(x, y).onPlaced();
					cdata.markForUpdate(x, y);
				}
			}
		}
	}
}
