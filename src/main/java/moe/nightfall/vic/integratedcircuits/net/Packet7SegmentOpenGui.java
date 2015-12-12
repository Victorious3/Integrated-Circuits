package moe.nightfall.vic.integratedcircuits.net;

import moe.nightfall.vic.integratedcircuits.api.gate.ISocketBridge.ISocketBase;
import moe.nightfall.vic.integratedcircuits.gate.Gate7Segment;
import moe.nightfall.vic.integratedcircuits.proxy.ClientProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

public class Packet7SegmentOpenGui extends PacketGate<Packet7SegmentOpenGui> {
	public Packet7SegmentOpenGui() {
	}

	public Packet7SegmentOpenGui(ISocketBase part) {
		super(part);
	}

	@Override
	public void process(EntityPlayer player, Side side) {
		Gate7Segment part = (Gate7Segment) getPart(player.worldObj);
		if (part == null)
			return;
		ClientProxy.open7SegmentGUI(part);
	}
}
