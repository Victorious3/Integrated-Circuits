package moe.nightfall.vic.integratedcircuits.cp.part.timed;

import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.cp.part.PartCPGate;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import net.minecraftforge.common.util.ForgeDirection; // Remove me!

/** Uses 8 bits for the delay. 255 ticks = 12.75 seconds */
public abstract class PartDelayedAction extends PartCPGate {
	public final BooleanProperty PROP_ACTIVE = new BooleanProperty("ACTIVE", stitcher);
	public final IntProperty PROP_CURRENT_DELAY = new IntProperty("CURRENT_DELAY", stitcher, 255);

	@Deprecated // Just to let things build. Remove as soon as possible
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
	}

	protected abstract int getDelay(Vec2 pos, ICircuit parent);

	public boolean isDelayActive(Vec2 pos, ICircuit parent) {
		return getProperty(pos, parent, PROP_ACTIVE);
	}

	/** Don't forget to call super BEFORE your own code */
	@Override
	public void onScheduledTick(Vec2 pos, ICircuit parent) {
		if (getProperty(pos, parent, PROP_ACTIVE)) {
			int counter = getProperty(pos, parent, PROP_CURRENT_DELAY);
			counter++;
			if (counter >= getDelay(pos, parent)) {
				setProperty(pos, parent, PROP_ACTIVE, false);
				setProperty(pos, parent, PROP_CURRENT_DELAY, 0);
				onDelay(pos, parent);
			} else {
				setProperty(pos, parent, PROP_CURRENT_DELAY, counter);
				scheduleTick(pos, parent);
			}
		}
	}

	public void onDelay(Vec2 pos, ICircuit parent) {
	}

	protected void setDelay(Vec2 pos, ICircuit parent, boolean delay) {
		setProperty(pos, parent, PROP_CURRENT_DELAY, 0);
		setProperty(pos, parent, PROP_ACTIVE, delay);
		if (delay)
			scheduleTick(pos, parent);
	}
}
