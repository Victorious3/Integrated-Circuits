package moe.nightfall.vic.integratedcircuits.cp.part.latch;

import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.cp.part.PartCPGate;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import net.minecraftforge.common.util.ForgeDirection; // Remove me!

public abstract class PartLatch extends PartCPGate {
	public final BooleanProperty PROP_OUT = new BooleanProperty("OUT", stitcher);
	protected final BooleanProperty PROP_TMP = new BooleanProperty("TMP", stitcher);

	@Deprecated // Just to let things build. Remove as soon as possible
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
	}

	@Override
	public Category getCategory() {
		return Category.LATCH;
	}

	@Override
	public void onScheduledTick(Vec2 pos, ICircuit parent) {
		setProperty(pos, parent, PROP_TMP, getProperty(pos, parent, PROP_OUT));
		notifyNeighbours(pos, parent);
	}
}
