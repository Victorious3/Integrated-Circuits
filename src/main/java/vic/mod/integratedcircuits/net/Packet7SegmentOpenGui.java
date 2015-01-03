package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.client.gui.Gui7Segment;
import vic.mod.integratedcircuits.part.Part7Segment;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.relauncher.Side;

public class Packet7SegmentOpenGui extends PacketTileEntity<Packet7SegmentOpenGui>
{
	private int side;
	
	public Packet7SegmentOpenGui() {}
	
	public Packet7SegmentOpenGui(Part7Segment part)
	{
		super(part.x(), part.y(), part.z());
		this.side = part.getSide();
	}
	
	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
		side = buffer.readInt();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		buffer.writeInt(side);
	}

	@Override
	public void process(EntityPlayer player, Side side) 
	{
		TileMultipart tm = (TileMultipart)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(tm == null) return;
		Part7Segment part = (Part7Segment)tm.partMap(this.side);
		if(part == null) return;
		
		Minecraft.getMinecraft().displayGuiScreen(new Gui7Segment(part));
	}
}
