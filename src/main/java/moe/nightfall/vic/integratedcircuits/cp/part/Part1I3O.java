package moe.nightfall.vic.integratedcircuits.cp.part;

import java.util.ArrayList;

import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;


public abstract class Part1I3O extends PartSimpleGate {
	public final IntProperty PROP_CONNECTORS = new IntProperty("CONNECTORS", stitcher, 6);

	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) {
		if (button == 0 && ctrl) {
			cycleProperty(pos, parent, PROP_CONNECTORS);
			notifyNeighbours(pos, parent);
		}
		super.onClick(pos, parent, button, ctrl);
	}

	@Override
	public boolean canConnectToSide(Vec2 pos, ICircuit parent, EnumFacing side) {
		EnumFacing s2 = toInternal(pos, parent, side);
		if (s2 == EnumFacing.SOUTH)
			return true;
		int i = getProperty(pos, parent, PROP_CONNECTORS);
		if (s2 == EnumFacing.EAST && (i == 3 || i == 4 || i == 5))
			return false;
		if (s2 == EnumFacing.NORTH && (i == 2 || i == 4 || i == 6))
			return false;
		if (s2 == EnumFacing.WEST && (i == 1 || i == 5 || i == 6))
			return false;
		return true;
	}

	@Override
	protected boolean hasOutputToSide(Vec2 pos, ICircuit parent, EnumFacing fd) {
		return fd != EnumFacing.SOUTH;
	}

	@Override
	public ArrayList<String> getInformation(Vec2 pos, ICircuit parent, boolean edit, boolean ctrlDown) {
		ArrayList<String> text = super.getInformation(pos, parent, edit, ctrlDown);
		if (edit && ctrlDown)
			text.add(I18n.format("gui.integratedcircuits.cad.mode"));
		return text;
	}
}
