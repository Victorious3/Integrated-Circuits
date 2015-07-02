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
		setProperty(pos, parent, PROP_DELAY, 1);
		super.onPlaced(pos, parent);
	}

	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) {
		super.onClick(pos, parent, button, ctrl);
		if (button == 0 && ctrl) {
			int delay = getProperty(pos, parent, PROP_DELAY);
			delay = delay * 2 + 1;
			if (delay > 255)
				delay = 1;
			setProperty(pos, parent, PROP_DELAY, delay);
			markForUpdate(pos, parent);
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
		invertProperty(pos, parent, PROP_OUT);
		notifyNeighbours(pos, parent);
		scheduleTick(pos, parent);
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent) {
		scheduleTick(pos, parent);
	}

	@Override
	public void onScheduledTick(Vec2 pos, ICircuit parent) {
		// Check if onDelay is still to be triggered (even on *current* tick),
		//  to avoid sampling input before output takes any effect.
		boolean checkInput = !isDelayActive(pos, parent);
		
		super.onScheduledTick(pos, parent);
		if (checkInput && getProperty(pos, parent, PROP_OUT) !=
				getInputFromSide(pos, parent,
					toExternal(pos, parent, ForgeDirection.SOUTH)))
			setDelay(pos, parent, true);
	}

	@Override
	public ArrayList<String> getInformation(Vec2 pos, ICircuit parent, boolean edit, boolean ctrlDown) {
		ArrayList<String> list = super.getInformation(pos, parent, edit, ctrlDown);
		list.add("Delay: " + (getProperty(pos, parent, PROP_DELAY) + 1));
		return list;
	}
}
