package moe.nightfall.vic.integratedcircuits.cp.part.latch;

import java.util.ArrayList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.cp.part.PartCPGate;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.util.ForgeDirection;

public class PartToggleLatch extends PartCPGate {
	public final BooleanProperty PROP_OUT = new BooleanProperty("OUT", stitcher);
	
	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) {
		super.onClick(pos, parent, button, ctrl);
		if (button == 0 && ctrl) {
			invertProperty(pos, parent, PROP_OUT);
			notifyNeighbours(pos, parent);
		}
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		updateInput(pos, parent);
		ForgeDirection s2 = toInternal(pos, parent, side);
		if ((s2 == ForgeDirection.NORTH || s2 == ForgeDirection.SOUTH))
			togglePostponedInputChange(pos, parent, side);
	}

	@Override
	public void onPostponedInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		if (getInputFromSide(pos, parent, side)) {
			invertProperty(pos, parent, PROP_OUT);
			notifyNeighbours(pos, parent);
		}
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		ForgeDirection s2 = toInternal(pos, parent, side);
		if (s2 == ForgeDirection.EAST)
			return getProperty(pos, parent, PROP_OUT);
		if (s2 == ForgeDirection.WEST)
			return !getProperty(pos, parent, PROP_OUT);
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(8, 1);
	}

	@Override
	public ArrayList<String> getInformation(Vec2 pos, ICircuit parent, boolean edit, boolean ctrlDown) {
		ArrayList<String> text = super.getInformation(pos, parent, edit, ctrlDown);
		if (edit && ctrlDown)
			text.add(I18n.format("gui.integratedcircuits.cad.toggle"));
		return text;
	}

	@Override
	public Category getCategory() {
		return Category.LATCH;
	}
}
