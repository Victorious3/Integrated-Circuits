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

	// pos is for CURRENT part
	public Vec2 getConnectedPos(Vec2 pos, ICircuit parent) {
		return new Vec2(getProperty(pos, parent, PROP_POS_X), getProperty(pos, parent, PROP_POS_Y));
	}

	// pos is for CONNECTED part
	public int setConnectedPos(int data, Vec2 pos) {
		data = PROP_POS_X.set(pos.x, data);
		data = PROP_POS_Y.set(pos.y, data);
		return data;
	}

	// pos is for CONNECTED part
	public boolean isConnected(Vec2 pos) {
		return pos.x != 255 && pos.y != 255;
	}

	// pos is for CURRENT part, not connected one, like with getNeighbourOnSide
	public PartTunnel getConnectedPart(Vec2 pos, ICircuit parent) {
		Vec2 pos2 = getConnectedPos(pos, parent);
		if (isConnected(pos2)) {
			CircuitPart cp = parent.getCircuitData().getPart(pos2);
			if (cp instanceof PartTunnel) {
				return (PartTunnel) cp;
			} else {
				// Reset back to default, unlinked state
				setProperty(pos, parent, PROP_POS_X, 255);
				setProperty(pos, parent, PROP_POS_Y, 255);
				setProperty(pos, parent, PROP_IN, false);
			}
		}
		return null;
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		updateInput(pos, parent);
		// updateInput analog for paired tunnel part
		Vec2 pos2 = getConnectedPos(pos, parent);
		PartTunnel part = getConnectedPart(pos, parent);
		setProperty(pos, parent, PROP_IN, part == null ? false : part.getOutputToSide(pos2, parent, ForgeDirection.UNKNOWN));

		notifyNeighbours(pos, parent);
		// notifyNeighbors analog for paired tunnel
		if (part != null && getOutputToSide(pos, parent, ForgeDirection.UNKNOWN) != part.getProperty(pos2, parent, PROP_IN)) {
			// Unlike notifyNeighbors, nothing can be done here after disconnect from paired tunnel.
			part.scheduleInputChange(pos2, parent, ForgeDirection.UNKNOWN, true);
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
		setProperty(pos, parent, PROP_IN, false);
		super.onPlaced(pos, parent);
	}

	@Override
	public void onChanged(Vec2 pos, ICircuit parent, int oldMeta) {
		// updateInput analog for paired tunnel part
		Vec2 pos2 = getConnectedPos(pos, parent);
		PartTunnel part = getConnectedPart(pos, parent);
		boolean oldI = getProperty(pos, parent, PROP_IN);
		boolean newI = part == null ? false : part.getOutputToSide(pos2, parent, ForgeDirection.UNKNOWN);
		setProperty(pos, parent, PROP_IN, newI);
		
		// Update previously connected tunnel, if required
		Vec2 oldPos2 = new Vec2(PROP_POS_X.get(oldMeta), PROP_POS_Y.get(oldMeta));
		if (!pos2.equals(oldPos2) && isConnected(oldPos2)) {
			CircuitPart cp = parent.getCircuitData().getPart(oldPos2);
			if (cp instanceof PartTunnel) {
				PartTunnel oldPart = (PartTunnel) cp;
				if (pos.equals(oldPart.getConnectedPos(oldPos2, parent))) {
					// Like notifyNeighbours after disconnect from neighbour
					oldPart.setProperty(oldPos2, parent, PROP_POS_X, 255);
					oldPart.setProperty(oldPos2, parent, PROP_POS_Y, 255);
					if (oldPart.getProperty(oldPos2, parent, PROP_IN))
						oldPart.scheduleInputChange(oldPos2, parent, ForgeDirection.UNKNOWN, true);
					oldPart.markForUpdate(oldPos2, parent);
				}
			}
		}
		
		if (oldI != newI) {
			markForUpdate(pos, parent);
			notifyNeighbours(pos, parent);
			// notifyNeighbors analog for paired tunnel
			if (part != null && getOutputToSide(pos, parent, ForgeDirection.UNKNOWN) != part.getProperty(pos2, parent, PROP_IN)) {
				part.scheduleInputChange(pos2, parent, ForgeDirection.UNKNOWN, true);
				part.markForUpdate(pos2, parent);
			}
		}
	}

	@Override
	public void onRemoved(Vec2 pos, ICircuit parent) {
		// Update connected tunnel, if required
		PartTunnel part = getConnectedPart(pos, parent);
		if (part != null) {
			Vec2 pos2 = getConnectedPos(pos, parent);
			// Like notifyNeighbours after disconnect from neighbour
			part.setProperty(pos2, parent, PROP_POS_X, 255);
			part.setProperty(pos2, parent, PROP_POS_Y, 255);
			if (part.getProperty(pos2, parent, PROP_IN))
				part.scheduleInputChange(pos2, parent, ForgeDirection.UNKNOWN, true);
			part.markForUpdate(pos2, parent);
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
