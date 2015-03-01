package vic.mod.integratedcircuits.ic.part.latch;

import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.ic.part.PartCPGate;
import vic.mod.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartLatch extends PartCPGate
{
	public final BooleanProperty PROP_OUT = new BooleanProperty("OUT", stitcher);
	protected final BooleanProperty PROP_TMP = new BooleanProperty("TMP", stitcher);
	
	@Override
	public void onScheduledTick(Vec2 pos, ICircuit parent) 
	{
		setProperty(pos, parent, PROP_TMP, getProperty(pos, parent, PROP_OUT));
		notifyNeighbours(pos, parent);
	}
}
