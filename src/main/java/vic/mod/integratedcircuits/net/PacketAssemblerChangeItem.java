package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.TileEntityAssembler;
import vic.mod.integratedcircuits.client.TileEntityAssemblerRenderer;
import cpw.mods.fml.relauncher.Side;

public class PacketAssemblerChangeItem extends PacketTileEntity<PacketAssemblerChangeItem>
{
	private boolean occupied;
	
	public PacketAssemblerChangeItem() {}
	
	public PacketAssemblerChangeItem(int xCoord, int yCoord, int zCoord, boolean occupied)
	{
		super(xCoord, yCoord, zCoord);
		this.occupied = occupied;
	}

	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
		occupied = buffer.readBoolean();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		buffer.writeBoolean(occupied);
	}

	@Override
	public void process(EntityPlayer player, Side side) 
	{
		TileEntityAssembler te = (TileEntityAssembler)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(te == null) return;
		TileEntityAssemblerRenderer.scheduleFramebuffer(te);
		te.excMatrix = null;
		te.isOccupied = occupied;
	}
}
