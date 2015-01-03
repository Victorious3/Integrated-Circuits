package vic.mod.integratedcircuits.net;

import java.io.IOException;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.client.gui.GuiPCBLayout;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.proxy.CommonProxy;
import vic.mod.integratedcircuits.tile.TileEntityPCBLayout;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;

public class PacketPCBChangeName extends PacketTileEntity<PacketPCBChangeName>
{
	private String name;
	private UUID uuid;
	
	public PacketPCBChangeName(){}
	
	public PacketPCBChangeName(EntityPlayer sender, String name, int xCoord, int yCoord, int zCoord)
	{
		super(xCoord, yCoord, zCoord);
		this.name = name;
		uuid = sender.getPersistentID();
	}
	
	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
		this.name = buffer.readStringFromBuffer(7);
		long l1 = buffer.readLong();
		long l2 = buffer.readLong();
		this.uuid = new UUID(l1, l2);
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		buffer.writeStringToBuffer(this.name);
		buffer.writeLong(uuid.getMostSignificantBits());
		buffer.writeLong(uuid.getLeastSignificantBits());
	}

	@Override
	public void process(EntityPlayer player, Side side) 
	{
		TileEntityPCBLayout te = (TileEntityPCBLayout)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(te != null)
		{
			te.getCircuitData().getProperties().setName(this.name);
			if(side == Side.SERVER)
			{
				CommonProxy.networkWrapper.sendToAllAround(this, 
					new TargetPoint(te.getWorldObj().provider.dimensionId, xCoord, yCoord, zCoord, 8));
			}
			else if(Minecraft.getMinecraft().currentScreen instanceof GuiPCBLayout && !MiscUtils.thePlayer().getPersistentID().equals(uuid))
				((GuiPCBLayout)Minecraft.getMinecraft().currentScreen).refreshUI();	
		}
	}
}
