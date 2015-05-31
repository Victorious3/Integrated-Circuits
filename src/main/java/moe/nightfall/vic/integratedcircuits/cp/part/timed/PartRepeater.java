package moe.nightfall.vic.integratedcircuits.cp.part.timed;

import java.util.ArrayList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import net.minecraftforge.common.util.ForgeDirection;

public class PartRepeater extends PartDelayedAction {
	public final IntProperty PROP_DELAY = new IntProperty("DELAY", stitcher, 255);
	public final BooleanProperty PROP_OUT = new BooleanProperty("OUT", stitcher);

	@Override
	protected int getDelay(Vec2 pos, ICircuit parent) {
		return getProperty(pos, parent, PROP_DELAY);
	}

	@Override
	public void onPlaced(Vec2 pos, ICircuit parent) {
		setProperty(pos, parent, PROP_DELAY, 2);
		setProperty(pos, parent, PROP_OUT, false);
	}

	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) {
		super.onClick(pos, parent, button, ctrl);
		if (button == 0 && ctrl) {
			int delay = getDelay(pos, parent);
			int newDelay = 0;
			switch (delay) {
				case 255:
					delay = 2;
					break;
				case 128:
					delay = 255;
					break;
				default:
					delay <<= 1;
					break;
			}
			setProperty(pos, parent, PROP_DELAY, delay);
		}
	}

	@Override
	public boolean canConnectToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		ForgeDirection s2 = toInternal(pos, parent, side);
		return s2 == ForgeDirection.NORTH || s2 == ForgeDirection.SOUTH;
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		ForgeDirection s2 = toInternal(pos, parent, side);
		if (s2 != ForgeDirection.NORTH)
			return false;
		return getProperty(pos, parent, PROP_OUT);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(1, 1);
	}

	@Override
	public void onDelay(Vec2 pos, ICircuit parent) {
		boolean b = invertProperty(pos, parent, PROP_OUT);
		if (b != getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH)))
			setDelay(pos, parent, true);
		super.onDelay(pos, parent);
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		updateInput(pos, parent);
		if (toInternal(pos, parent, side) != ForgeDirection.SOUTH)
			return;
		if (getCurrentDelay(pos, parent) == 0) {
			boolean in = getInputFromSide(pos, parent, side);
			if (getProperty(pos, parent, PROP_OUT) != in)
				setDelay(pos, parent, true);
		}
	}

	@Override
	public ArrayList<String> getInformation(Vec2 pos, ICircuit parent, boolean edit, boolean ctrlDown) {
		ArrayList<String> list = super.getInformation(pos, parent, edit, ctrlDown);
		list.add("Delay: " + getDelay(pos, parent));
		return list;
	}
}