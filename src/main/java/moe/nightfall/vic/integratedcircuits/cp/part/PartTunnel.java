package moe.nightfall.vic.integratedcircuits.cp.part;

import moe.nightfall.vic.integratedcircuits.Content;
import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPart;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer.CircuitRenderWrapper;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer.EnumRenderType;
import moe.nightfall.vic.integratedcircuits.misc.CraftingAmount;
import moe.nightfall.vic.integratedcircuits.misc.ItemAmount;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Items;
import net.minecraftforge.common.util.ForgeDirection;

public class PartTunnel extends CircuitPart {

	public final IntProperty PROP_POS_X = new IntProperty("PROP_POS_X", stitcher, 255);
	public final IntProperty PROP_POS_Y = new IntProperty("PROP_POS_Y", stitcher, 255);
	public final BooleanProperty PROP_IN = new BooleanProperty("PROP_IN", stitcher);
	public final BooleanProperty PROP_OUT = new BooleanProperty("PROP_OUT", stitcher);

	public Vec2 getConnectedPos(Vec2 pos, ICircuit parent) {
		return new Vec2(getProperty(pos, parent, PROP_POS_X), getProperty(pos, parent, PROP_POS_Y));
	}

	public int setConnectedPos(int data, Vec2 pos) {
		data = PROP_POS_X.set(pos.x, data);
		data = PROP_POS_Y.set(pos.y, data);
		return data;
	}

	public boolean isConnected(Vec2 pos) {
		return pos.x != 255 && pos.y != 255;
	}

	public PartTunnel getConnectedPart(Vec2 pos, ICircuit parent) {
		if (isConnected(pos)) {
			CircuitPart cp = parent.getCircuitData().getPart(pos);
			if (cp instanceof PartTunnel) {
				return (PartTunnel) cp;
			} else {
				// Reset back to default, unlinked state
				setProperty(pos, parent, PROP_POS_X, 255);
				setProperty(pos, parent, PROP_POS_Y, 255);
			}
		}
		return null;
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		super.onInputChange(pos, parent, side);
		Vec2 pos2 = getConnectedPos(pos, parent);

		PartTunnel part = null;
		if (isConnected(pos2)) {
			part = getConnectedPart(pos2, parent);
			if (part != null) {
				setProperty(pos, parent, PROP_IN, getOutputToSide(pos2, parent, ForgeDirection.UNKNOWN));
			}
		}

		notifyNeighbours(pos, parent);
		if (part != null && getOutputToSide(pos, parent, ForgeDirection.UNKNOWN) != part.getProperty(pos2, parent, PROP_IN)) {
			// Lazy refresh
			part.onInputChange(pos2, parent, ForgeDirection.UNKNOWN);
			part.markForUpdate(pos2, parent);
		}
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		boolean in = getProperty(pos, parent, PROP_IN);
		if (side == ForgeDirection.UNKNOWN)
			return getInput(pos, parent) && !in;
		return (getInput(pos, parent) || in) && !getInputFromSide(pos, parent, side);
	}

	@Override
	public void onPlaced(Vec2 pos, ICircuit parent) {
		setProperty(pos, parent, PROP_POS_X, 255);
		setProperty(pos, parent, PROP_POS_Y, 255);
		super.onPlaced(pos, parent);
	}

	@Override
	public void onChanged(Vec2 pos, ICircuit parent, int oldMeta) {
		Vec2 pos2 = getConnectedPos(pos, parent);
		boolean oldI = getProperty(pos, parent, PROP_IN);
		boolean newI = oldI;

		PartTunnel part = null;
		if (isConnected(pos2)) {
			part = getConnectedPart(pos2, parent);
			if (part != null) {
				newI = getOutputToSide(pos2, parent, ForgeDirection.UNKNOWN);
				setProperty(pos, parent, PROP_IN, newI);
			}
		}

		if (oldI != newI) {
			markForUpdate(pos, parent);
			notifyNeighbours(pos, parent);
		}
	}

	@Override
	public void onRemoved(Vec2 pos, ICircuit parent) {
		Vec2 pos2 = getConnectedPos(pos, parent);
		if (isConnected(pos2)) {
			PartTunnel part = getConnectedPart(pos2, parent);
			if (part != null) {
				part.setState(pos2, parent, setConnectedPos(part.getState(pos2, parent), new Vec2(255, 255)));
				markForUpdate(pos2, parent);
			}
		}
	}

	@Override
	public void renderPart(Vec2 pos, ICircuit parent, double x, double y, EnumRenderType type) {
		Tessellator tes = Tessellator.instance;

		tes.setColorRGBA_F(0F, 1F, 0F, 1F);
		CircuitPartRenderer.addQuad(x, y, 16, 4 * 16, 16, 16);
		if (getInput(pos, parent) || getProperty(pos, parent, PROP_IN)) {
			tes.setColorRGBA_F(0F, 1F, 0F, 1F);
		} else {
			tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
		}
		CircuitPartRenderer.addQuad(x, y, 0, 4 * 16, 16, 16);
	}

	@Override
	public Category getCategory() {
		return Category.WIRE;
	}

	@Override
	public String getLocalizedName(Vec2 pos, ICircuit parent) {
		String name = super.getLocalizedName(pos, parent);
		if (!(parent instanceof CircuitRenderWrapper) && isConnected(getConnectedPos(pos, parent))) {
			name += " (Linked)";
		}
		return name;
	}

	@Override
	public void getCraftingCost(CraftingAmount amount, CircuitData parent, Vec2 pos) {
		amount.add(new ItemAmount(Items.redstone, 0.1));
		amount.add(new ItemAmount(Content.itemSiliconDrop, 0.1));

		int data = parent.getMeta(pos);
		Vec2 end = new Vec2(PROP_POS_X.get(data), PROP_POS_Y.get(data));
		if (isConnected(end)) {
			amount.add(new ItemAmount(Items.redstone, 0.1 * pos.distanceTo(end)));
		}
	}
}
