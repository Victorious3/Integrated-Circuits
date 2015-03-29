package vic.mod.integratedcircuits.net;

import net.minecraft.entity.player.EntityPlayer;
import vic.mod.integratedcircuits.gate.ISocket;
import vic.mod.integratedcircuits.gate.Part7Segment;
import vic.mod.integratedcircuits.proxy.ClientProxy;
import cpw.mods.fml.relauncher.Side;

public class Packet7SegmentOpenGui extends PacketGate<Packet7SegmentOpenGui>
{
	public Packet7SegmentOpenGui() {}
	
	public Packet7SegmentOpenGui(ISocket part) 
	{
		super(part);
	}
	
	@Override
	public void process(EntityPlayer player, Side side) 
	{
		Part7Segment part = (Part7Segment)getPart(player.worldObj);
		if(part == null) return;
		ClientProxy.open7SegmentGUI(part);
	}
}
