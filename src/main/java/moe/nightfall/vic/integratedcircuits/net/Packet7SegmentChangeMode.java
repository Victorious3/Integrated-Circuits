package moe.nightfall.vic.integratedcircuits.net;

import java.io.IOException;

import moe.nightfall.vic.integratedcircuits.api.gate.ISocketBridge.ISocketBase;
import moe.nightfall.vic.integratedcircuits.client.gui.Gui7Segment;
import moe.nightfall.vic.integratedcircuits.gate.Gate7Segment;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;

public class Packet7SegmentChangeMode extends PacketGate<Packet7SegmentChangeMode>
{
	private int mode;
	private boolean isSlave;
	
	public Packet7SegmentChangeMode() {}
	
	public Packet7SegmentChangeMode(ISocketBase part, int mode, boolean isSlave)
	{
		super(part);
		this.mode = mode;
		this.isSlave = isSlave;
	}
	
	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
		isSlave = buffer.readBoolean();
		if(!isSlave) mode = buffer.readInt();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		buffer.writeBoolean(isSlave);
		if(!isSlave) buffer.writeInt(mode);
	}
	
	@Override
	public void process(EntityPlayer player, Side side) 
	{
		Gate7Segment part = (Gate7Segment)getPart(player.worldObj);
		if(part == null) return;
		if(side == Side.SERVER)
		{
			if(isSlave != part.isSlave)
			{
				if(isSlave) part.onAdded();
				else
				{
					part.updateConnections();
					part.claimSlaves();
				}
			}
			if(!isSlave) part.mode = mode;
			
			isSlave = part.isSlave;
			part.getProvider().notifyBlocksAndChanges();
			part.getProvider().resetInput();
			part.getProvider().updateInput();
			
			CommonProxy.networkWrapper.sendToAllAround(this, 
				new TargetPoint(part.getProvider().getWorld().provider.dimensionId, xCoord, yCoord, zCoord, 8));
		}
		else
		{
			part.isSlave = isSlave;
			if(!isSlave && mode != part.mode)
			{
				part.mode = mode;
				part.getProvider().markRender();
			}
			if(Minecraft.getMinecraft().currentScreen instanceof Gui7Segment)
				((Gui7Segment)Minecraft.getMinecraft().currentScreen).refreshUI();
		}
	}
}
