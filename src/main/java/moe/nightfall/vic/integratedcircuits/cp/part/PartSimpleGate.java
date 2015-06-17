package moe.nightfall.vic.integratedcircuits.cp.part;

import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import net.minecraftforge.common.util.ForgeDirection;

/** Has only one type of output **/
public abstract class PartSimpleGate extends PartCPGate {
	public final BooleanProperty PROP_OUT = new BooleanProperty("OUT", stitcher);

	protected final boolean getOutput(Vec2 pos, ICircuit parent) {
		return getProperty(pos, parent, PROP_OUT);
	}

	protected final void setOutput(Vec2 pos, ICircuit parent, boolean output) {
		setProperty(pos, parent, PROP_OUT, output);
	}

	protected abstract void calcOutput(Vec2 pos, ICircuit parent);

	/** already rotated **/
	protected abstract boolean hasOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection fd);

	@Override
	public Category getCategory() {
		return Category.GATE;
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		return hasOutputToSide(pos, parent, toInternal(pos, parent, side)) && getProperty(pos, parent, PROP_OUT);
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		updateInput(pos, parent);
		ForgeDirection s2 = toInternal(pos, parent, side);
		if (canConnectToSide(pos, parent, side) && !hasOutputToSide(pos, parent, s2))
			togglePostponedInputChange(pos, parent, side);
	}

	@Override
	public void onPostponedInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		calcOutput(pos, parent);
		notifyNeighbours(pos, parent);
	}

	@Override
	public void onPlaced(Vec2 pos, ICircuit parent) {
		updateInput(pos, parent);
		setProperty(pos, parent, PROP_OUT, false);
		calcOutput(pos, parent);
		notifyNeighbours(pos, parent);
	}

	@Override
	public void onAfterRotation(Vec2 pos, ICircuit parent) {
		calcOutput(pos, parent);
		notifyNeighbours(pos, parent);
	}

	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) {
		super.onClick(pos, parent, button, ctrl);
		onPlaced(pos, parent);
	}
}
