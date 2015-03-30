package vic.mod.integratedcircuits.net;

import net.minecraft.entity.player.EntityPlayer;
import vic.mod.integratedcircuits.gate.Gate7Segment;
import vic.mod.integratedcircuits.gate.ISocketBridge.ISocketBase;
import vic.mod.integratedcircuits.proxy.ClientProxy;
import cpw.mods.fml.relauncher.Side;

public class Packet7SegmentOpenGui extends PacketGate<Packet7SegmentOpenGui>
{
	public Packet7SegmentOpenGui() {}
	
	public Packet7SegmentOpenGui(ISocketBase part) 
	{
		super(part);
	}
	
	@Override
	public void process(EntityPlayer player, Side side) 
	{
		Gate7Segment part = (Gate7Segment)getPart(player.worldObj);
		if(part == null) return;
		ClientProxy.open7SegmentGUI(part);
	}
}
