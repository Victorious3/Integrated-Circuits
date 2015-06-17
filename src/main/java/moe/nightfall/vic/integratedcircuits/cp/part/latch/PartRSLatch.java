package moe.nightfall.vic.integratedcircuits.cp.part.latch;

import java.util.ArrayList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.cp.part.PartCPGate;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;

public class PartRSLatch extends PartCPGate {
	public final IntProperty PROP_STATE = new IntProperty("STATE", stitcher, 2);
	public final IntProperty PROP_MODE = new IntProperty("MODE", stitcher, 3);

	@Override
	public void onPlaced(Vec2 pos, ICircuit parent) {
		setProperty(pos, parent, PROP_STATE, 0);
		setProperty(pos, parent, PROP_MODE, 0);
		updateInput(pos, parent);
		onPostponedInputChange(pos, parent, ForgeDirection.UNKNOWN);
	}

	@Override
	public void onAfterRotation(Vec2 pos, ICircuit parent) {
		onPostponedInputChange(pos, parent, ForgeDirection.UNKNOWN);
	}

	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) {
		super.onClick(pos, parent, button, ctrl);
		if (button == 0 && ctrl) {
			cycleProperty(pos, parent, PROP_MODE);
			onPostponedInputChange(pos, parent, ForgeDirection.UNKNOWN);
		}
	}

	private boolean isMirrored(Vec2 pos, ICircuit parent) {
		return (getProperty(pos, parent, PROP_MODE) & 2) != 0;
	}

	private boolean isSpecial(Vec2 pos, ICircuit parent) {
		return (getProperty(pos, parent, PROP_MODE) & 1) != 0;
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		updateInput(pos, parent);
		ForgeDirection s2 = toInternal(pos, parent, side);
		if (s2 == ForgeDirection.NORTH || s2 == ForgeDirection.SOUTH) {
			togglePostponedInputChange(pos, parent, side);
			// A bit of wire-like behavior, for the second mode
			if (getInputFromSide(pos, parent, side)
					&& !getInputFromSide(pos, parent, side.getOpposite())
					&& getOutputToSide(pos, parent, side.getOpposite())) {
				// Break up temporarily, to check the other input.
				setProperty(pos, parent, PROP_STATE, 0);
			}
			notifyNeighbours(pos, parent);
		}
	}

	@Override
	public void onPostponedInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		ForgeDirection s2 = toExternal(pos, parent, ForgeDirection.NORTH);
		boolean in1 = getInputFromSide(pos, parent, s2);
		boolean in2 = getInputFromSide(pos, parent, s2.getOpposite());
		System.out.println("Here");
		if (in1 != in2)
			setProperty(pos, parent, PROP_STATE, in1 ? 1 : 2);
		else if (in1 && in2)
			setProperty(pos, parent, PROP_STATE, 0);
		else if (getProperty(pos, parent, PROP_STATE) == 0)
			// Both inputs off, still in broken state
			// Vanilla and P:R RS latches work this way
			setProperty(pos, parent, PROP_STATE, 1); 
		notifyNeighbours(pos, parent);
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		int state = getProperty(pos, parent, PROP_STATE);
		if (state == 0)
			return false;
		ForgeDirection s2 = toInternal(pos, parent, side);
		boolean special = isSpecial(pos, parent);
		boolean mirrored = isMirrored(pos, parent);
		if (s2 == ForgeDirection.NORTH) {
			if (special && state == 1)
				// A bit of wire-like behavior:
				// Only output if we are not powered from the same side
				return !getInputFromSide(pos, parent, side);
			return false;
		}
		if (s2 == ForgeDirection.SOUTH) {
			if (special && state == 2)
				// A bit of wire-like behavior:
				// Only output if we are not powered from the same side
				return !getInputFromSide(pos, parent, side);
			return false;
		}
		if (s2 == ForgeDirection.EAST) {
			return (state == 1) != mirrored;
		}
		if (s2 == ForgeDirection.WEST) {
			return (state == 1) == mirrored;
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(7, 1);
	}

	@Override
	public ArrayList<String> getInformation(Vec2 pos, ICircuit parent, boolean edit, boolean ctrlDown) {
		ArrayList<String> text = super.getInformation(pos, parent, edit, ctrlDown);
		text.add(I18n.format("part.integratedcircuits.rslatch.mode") + ": " + (isSpecial(pos, parent) ? 1 : 0));
		if (isMirrored(pos, parent))
			text.add(EnumChatFormatting.ITALIC + I18n.format("part.integratedcircuits.rslatch.mirrored"));
		if (edit && ctrlDown)
			text.add(I18n.format("gui.integratedcircuits.cad.mode"));
		return text;
	}

	@Override
	public Category getCategory() {
		return Category.LATCH;
	}
}
