package moe.nightfall.vic.integratedcircuits.cp.part.latch;

import java.util.ArrayList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;

public class PartRSLatch extends PartLatch {
	public final IntProperty PROP_MODE = new IntProperty("MODE", stitcher, 3);

	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) {
		super.onClick(pos, parent, button, ctrl);
		if (button == 0 && ctrl) {
			cycleProperty(pos, parent, PROP_MODE);
			notifyNeighbours(pos, parent);
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
		if (s2 == ForgeDirection.EAST || s2 == ForgeDirection.WEST)
			return;
		setProperty(pos, parent, PROP_OUT, s2 == ForgeDirection.NORTH);
		scheduleTick(pos, parent);
		markForUpdate(pos, parent);
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		ForgeDirection s2 = toInternal(pos, parent, side);
		ForgeDirection s3 = toExternal(pos, parent, ForgeDirection.NORTH);
		boolean b1 = !(getInputFromSide(pos, parent, s3) && getInputFromSide(pos, parent, s3.getOpposite()));
		if (((s2 == ForgeDirection.EAST && !isMirrored(pos, parent) || s2 == ForgeDirection.WEST
				&& isMirrored(pos, parent)) || (s2 == ForgeDirection.NORTH && isSpecial(pos, parent) && !getInputFromSide(
				pos, parent, s3.getOpposite()))) && b1 && getProperty(pos, parent, PROP_TMP))
			return true;
		if (((s2 == ForgeDirection.WEST && !isMirrored(pos, parent) || s2 == ForgeDirection.EAST
				&& isMirrored(pos, parent)) || (s2 == ForgeDirection.SOUTH && isSpecial(pos, parent) && !getInputFromSide(
				pos, parent, s3))) && b1 && !getProperty(pos, parent, PROP_TMP))
			return true;
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
}