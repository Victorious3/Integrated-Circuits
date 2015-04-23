package moe.nightfall.vic.integratedcircuits.ic.part.latch;

import java.util.ArrayList;

import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.util.ForgeDirection;

public class PartToggleLatch extends PartLatch {
	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) {
		super.onClick(pos, parent, button, ctrl);
		if (button == 0 && ctrl) {
			invertProperty(pos, parent, PROP_TMP);
			notifyNeighbours(pos, parent);
		}
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		super.onInputChange(pos, parent, side);
		ForgeDirection s2 = toInternal(pos, parent, side);
		if ((s2 == ForgeDirection.NORTH || s2 == ForgeDirection.SOUTH)) {
			if (getInputFromSide(pos, parent, side))
				invertProperty(pos, parent, PROP_OUT);
			scheduleTick(pos, parent);
		}
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		ForgeDirection s2 = toInternal(pos, parent, side);
		if (s2 == ForgeDirection.EAST)
			return getProperty(pos, parent, PROP_TMP);
		if (s2 == ForgeDirection.WEST)
			return !getProperty(pos, parent, PROP_TMP);
		return false;
	}

	@Override
	public ArrayList<String> getInformation(Vec2 pos, ICircuit parent, boolean edit, boolean ctrlDown) {
		ArrayList<String> text = super.getInformation(pos, parent, edit, ctrlDown);
		if (edit && ctrlDown)
			text.add(I18n.format("gui.integratedcircuits.cad.toggle"));
		return text;
	}
}