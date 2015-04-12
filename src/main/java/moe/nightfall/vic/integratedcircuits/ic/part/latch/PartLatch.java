package moe.nightfall.vic.integratedcircuits.ic.part.latch;

import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.ic.part.PartCPGate;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.BooleanProperty;

public class PartLatch extends PartCPGate
{
	public final BooleanProperty PROP_OUT = new BooleanProperty("OUT", stitcher);
	protected final BooleanProperty PROP_TMP = new BooleanProperty("TMP", stitcher);

	@Override
	public Category getCategory() {
		return Category.LATCH;
	}

	@Override
	public void onScheduledTick(Vec2 pos, ICircuit parent) 
	{
		setProperty(pos, parent, PROP_TMP, getProperty(pos, parent, PROP_OUT));
		notifyNeighbours(pos, parent);
	}
}
