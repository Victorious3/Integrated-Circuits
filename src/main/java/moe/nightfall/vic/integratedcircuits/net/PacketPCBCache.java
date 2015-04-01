package moe.nightfall.vic.integratedcircuits.net;

import java.io.IOException;

import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityPCBLayout;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ChatComponentText;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;

public class PacketPCBCache extends PacketTileEntity<PacketPCBCache>
{
	public static final byte SNAPSHOT = 0;
	public static final byte UNDO = 1;
	public static final byte REDO = 2;
	
	private byte mode;
	
	public PacketPCBCache(){}
	
	public PacketPCBCache(byte mode, int x, int y, int z)
	{
		super(x, y, z);
		this.mode = mode;
	}
	
	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
		mode = buffer.readByte();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		buffer.writeByte(mode);
	}

	@Override
	public void process(EntityPlayer player, Side side) 
	{
		TileEntityPCBLayout te = (TileEntityPCBLayout)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(te != null)
		{
			switch (mode) {
			case SNAPSHOT :
				te.cache.capture(player.getGameProfile().getId());
				break;
			case UNDO :
				try {
					te.cache.undo(player.getGameProfile().getId());
					CommonProxy.networkWrapper.sendToAllAround(new PacketPCBLoad(te.getCircuitData(), xCoord, yCoord, zCoord), 
						new TargetPoint(te.getWorldObj().provider.dimensionId, xCoord, yCoord, zCoord, 8));
				} catch (ArrayIndexOutOfBoundsException e) {
					player.addChatMessage(new ChatComponentText("[CAD]: No more undos"));
				}
				break;
			case REDO :
				try {
					te.cache.redo(player.getGameProfile().getId());
					CommonProxy.networkWrapper.sendToAllAround(new PacketPCBLoad(te.getCircuitData(), xCoord, yCoord, zCoord), 
						new TargetPoint(te.getWorldObj().provider.dimensionId, xCoord, yCoord, zCoord, 8));
				} catch (ArrayIndexOutOfBoundsException e) {
					player.addChatMessage(new ChatComponentText("[CAD]: No more redos"));
				}
				break;
			}
		}
	}
}
