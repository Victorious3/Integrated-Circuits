package moe.nightfall.vic.integratedcircuits.cp.part;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.Config;
import moe.nightfall.vic.integratedcircuits.Content;
import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPart;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer.CircuitRenderWrapper;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer.EnumRenderType;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.CraftingAmount;
import moe.nightfall.vic.integratedcircuits.misc.ItemAmount;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import moe.nightfall.vic.integratedcircuits.misc.RenderUtils;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Items;
import net.minecraftforge.common.util.ForgeDirection;

public class PartTunnel extends PartWire {

	public final IntProperty PROP_POS_X = new IntProperty("PROP_POS_X", stitcher, 255);
	public final IntProperty PROP_POS_Y = new IntProperty("PROP_POS_Y", stitcher, 255);
	public final BooleanProperty PROP_IN = new BooleanProperty("PROP_IN", stitcher);

	@Override
	public boolean getInput(Vec2 pos, ICircuit parent) {
		return super.getInput(pos, parent) || getProperty(pos, parent, PROP_IN);
	}

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
				markForUpdate(pos, parent);
			}
		}
		return null;
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent) {
		// updateInput analog for paired tunnel part
		Vec2 pos2 = getConnectedPos(pos, parent);
		PartTunnel part = getConnectedPart(pos, parent);
		setProperty(pos, parent, PROP_IN, part == null ? false : part.getOutputToSide(pos2, parent, ForgeDirection.UNKNOWN));

		notifyNeighbours(pos, parent);
		// notifyNeighbors analog for paired tunnel
		if (part != null && getOutputToSide(pos, parent, ForgeDirection.UNKNOWN) != part.getProperty(pos2, parent, PROP_IN)) {
			// Unlike notifyNeighbors, nothing can be done here after disconnect from paired tunnel.
			part.scheduleInputChange(pos2, parent);
			part.markForUpdate(pos2, parent);
		}
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		boolean in = getProperty(pos, parent, PROP_IN);
		if (side == ForgeDirection.UNKNOWN)
			return getInput(pos, parent) && !in;
		return (getInput(pos, parent)) && !getInputFromSide(pos, parent, side);
	}

	@Override
	public void onPlaced(Vec2 pos, ICircuit parent) {
		setProperty(pos, parent, PROP_POS_X, 255);
		setProperty(pos, parent, PROP_POS_Y, 255);
		setProperty(pos, parent, PROP_IN, false);
		scheduleInputChange(pos, parent);
	}

	// Used to update previously connected tunnel when required
	private void dropConnected(Vec2 pos, ICircuit parent, Vec2 oldPos2) {
		if (isConnected(oldPos2)) {
			CircuitPart cp = parent.getCircuitData().getPart(oldPos2);
			if (cp instanceof PartTunnel) {
				PartTunnel oldPart = (PartTunnel) cp;
				if (pos.equals(oldPart.getConnectedPos(oldPos2, parent))) {
					oldPart.setProperty(oldPos2, parent, PROP_POS_X, 255);
					oldPart.setProperty(oldPos2, parent, PROP_POS_Y, 255);
					// Like notifyNeighbours after disconnect from neighbour
					if (oldPart.getProperty(oldPos2, parent, PROP_IN))
						oldPart.scheduleInputChange(oldPos2, parent);
					oldPart.markForUpdate(oldPos2, parent);
				}
			}
		}
	}

	@Override
	public void onChanged(Vec2 pos, ICircuit parent, int oldMeta) {
		// Update previously connected tunnel, if required
		Vec2 pos2 = getConnectedPos(pos, parent);
		Vec2 oldPos2 = new Vec2(PROP_POS_X.get(oldMeta), PROP_POS_Y.get(oldMeta));
		if (!pos2.equals(oldPos2))
			dropConnected(pos, parent, oldPos2);
		
		scheduleInputChange(pos, parent);
	}

	@Override
	public void onRemoved(Vec2 pos, ICircuit parent) {
		// Update connected tunnel, if required
		dropConnected(pos, parent, getConnectedPos(pos, parent));
	}

	@Override
	public Category getCategory() {
		return Category.WIRE;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderPart(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		Tessellator tes = Tessellator.instance;

		RenderUtils.applyColorIRGBA(tes, Config.colorGreen);
		CircuitPartRenderer.addQuad(x, y, 16, 4*16, 16, 16);

		renderViaWire(pos, parent, x, y, type);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean allowsDragPlacement() {
		return false;
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
		// Tunnels are twice as expensive as wires and also cost a bit of silicon.
		amount.add(new ItemAmount(Items.redstone, 0.1));
		amount.add(new ItemAmount(Content.itemSiliconDrop, 0.1));

		int data = parent.getMeta(pos);
		Vec2 end = new Vec2(PROP_POS_X.get(data), PROP_POS_Y.get(data));
		if (isConnected(end)) {
			// Half the amount, because it will be added twice.
			amount.add(new ItemAmount(Items.redstone, 0.05 * pos.distanceTo(end)));
		}
	}
}
