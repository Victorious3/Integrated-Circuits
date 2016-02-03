package moe.nightfall.vic.integratedcircuits.cp.part.latch;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.cp.part.PartCPGate;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import net.minecraftforge.common.util.ForgeDirection;

public class PartTransparentLatch extends PartCPGate {
	public final BooleanProperty PROP_OUT = new BooleanProperty("OUT", stitcher);
	public final IntProperty PROP_CONFIG = new IntProperty("CONFIG", stitcher, 5);

	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) {
		if (button == 0 && ctrl) {
			cycleProperty(pos, parent, PROP_CONFIG);
			scheduleInputChange(pos, parent);
			notifyNeighbours(pos, parent);
		}
		super.onClick(pos, parent, button, ctrl);
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent) {
		scheduleTick(pos, parent);
	}

	@Override
	public void onScheduledTick(Vec2 pos, ICircuit parent) {
		int cfg = getProperty(pos, parent, PROP_CONFIG);
		ForgeDirection writeSide = (cfg & 1) == 0 ? ForgeDirection.SOUTH : ForgeDirection.NORTH;
		if (getInputFromSide(pos, parent, toExternal(pos, parent, writeSide))) {
			setProperty(pos, parent, PROP_OUT,
					getInputFromSide(pos, parent,
						toExternal(pos, parent, ForgeDirection.WEST)));
			notifyNeighbours(pos, parent);
		}
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		ForgeDirection s2 = toInternal(pos, parent, side);
		int cfg = getProperty(pos, parent, PROP_CONFIG);
		if ((s2 == ((cfg & 1) == 0 ? ForgeDirection.NORTH : ForgeDirection.SOUTH) && (cfg & 2) == 0)
				|| (s2 == ForgeDirection.EAST && (cfg & 4) == 0))
			return getProperty(pos, parent, PROP_OUT);
		return false;
	}

	@Override
	public boolean canConnectToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		ForgeDirection s2 = toInternal(pos, parent, side);
		if (s2 == ForgeDirection.WEST)
			return true;
		int cfg = getProperty(pos, parent, PROP_CONFIG);
		if (s2 == ForgeDirection.EAST)
			return (cfg & 4) == 0;
		return ((cfg & 2) == 0) || (((cfg & 1) == 0) == (s2 == ForgeDirection.SOUTH));
	}

	@Override
	public void renderPart(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		CircuitPartRenderer.renderPartGate(pos, parent, this, x, y, type);
		int cfg = getProperty(pos, parent, PROP_CONFIG);
		CircuitPartRenderer.addQuad(x, y, 9 * 16, (1 + (cfg / 2)) * 16, 16, 16, this.getRotation(pos, parent), false, (cfg & 1) != 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(9, 1);
	}

	@Override
	public Category getCategory() {
		return Category.LATCH;
	}
}
