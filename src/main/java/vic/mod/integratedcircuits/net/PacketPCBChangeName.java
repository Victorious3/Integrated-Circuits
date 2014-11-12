package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.TileEntityPCBLayout;
import vic.mod.integratedcircuits.client.gui.GuiPCBLayout;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;

public class PacketPCBChangeName extends PacketTileEntity<PacketPCBChangeName>
{
	private String name;
	
	public PacketPCBChangeName(){}
	
	public PacketPCBChangeName(String name, int xCoord, int yCoord, int zCoord)
	{
		super(xCoord, yCoord, zCoord);
		this.name = name;
	}
	
	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
		this.name = buffer.readStringFromBuffer(7);
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		buffer.writeStringToBuffer(this.name);
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
				IntegratedCircuits.networkWrapper.sendToAllAround(this, 
					new TargetPoint(te.getWorldObj().getWorldInfo().getVanillaDimension(), xCoord, yCoord, zCoord, 8));
			}
			else if(Minecraft.getMinecraft().currentScreen instanceof GuiPCBLayout)
				((GuiPCBLayout)Minecraft.getMinecraft().currentScreen).refreshUI();	
		}
	}
}
