package moe.nightfall.vic.integratedcircuits.cp.part;

import java.util.Arrays;
import java.util.Collection;

import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPart;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.CraftingAmount;
import moe.nightfall.vic.integratedcircuits.misc.ItemAmount;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Items;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartWire extends CircuitPart {
	public final IntProperty PROP_COLOR = new IntProperty("COLOR", stitcher, 2);

	// TODO Hardcoded for now, do change!
	/*@Override
	public Category getCategory() {
		return Category.WIRE;
	}*/

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		return getInput(pos, parent) && !getInputFromSide(pos, parent, side);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderPart(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		int color = this.getColor(pos, parent);
		Tessellator tes = Tessellator.instance;

		if (type == CircuitPartRenderer.EnumRenderType.GUI) {
			switch (color) {
				case 1:
					if (this.getInput(pos, parent))
						tes.setColorRGBA_F(1F, 0F, 0F, 1F);
					else
						tes.setColorRGBA_F(0.4F, 0F, 0F, 1F);
					break;
				case 2:
					if (this.getInput(pos, parent))
						tes.setColorRGBA_F(1F, 0.4F, 0F, 1F);
					else
						tes.setColorRGBA_F(0.4F, 0.2F, 0F, 1F);
					break;
				default:
					if (this.getInput(pos, parent))
						tes.setColorRGBA_F(0F, 1F, 0F, 1F);
					else
						tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);
					break;
			}
		} else
			tes.setColorRGBA_F(0F, 0.4F, 0F, 1F);

		int ty = type == CircuitPartRenderer.EnumRenderType.WORLD_16x ? 3 * 16 : 0;

		int con = CircuitPartRenderer.checkConnections(pos, parent, this);
		if ((con & 12) == 12 && (con & ~12) == 0)
			CircuitPartRenderer.addQuad(x, y, 6 * 16, ty, 16, 16);
		else if ((con & 3) == 3 && (con & ~3) == 0)
			CircuitPartRenderer.addQuad(x, y, 5 * 16, ty, 16, 16);
		else {
			if ((con & 8) > 0)
				CircuitPartRenderer.addQuad(x, y, 2 * 16, ty, 16, 16);
			if ((con & 4) > 0)
				CircuitPartRenderer.addQuad(x, y, 4 * 16, ty, 16, 16);
			if ((con & 2) > 0)
				CircuitPartRenderer.addQuad(x, y, 1 * 16, ty, 16, 16);
			if ((con & 1) > 0)
				CircuitPartRenderer.addQuad(x, y, 3 * 16, ty, 16, 16);
			CircuitPartRenderer.addQuad(x, y, 0, ty, 16, 16);
		}
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		updateInput(pos, parent);
		notifyNeighbours(pos, parent);
	}

	public int getColor(Vec2 pos, ICircuit parent) {
		return getProperty(pos, parent, PROP_COLOR);
	}

	@Override
	public boolean canConnectToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		CircuitPart part = getNeighbourOnSide(pos, parent, side);
		if (part instanceof PartWire) {
			int pcolor = ((PartWire) part).getColor(pos.offset(side), parent);
			int color = getColor(pos, parent);
			if (pcolor == 0 || color == 0)
				return true;
			return color == pcolor;
		}
		return true;
	}

	@Override
	public void getCraftingCost(CraftingAmount cost, CircuitData parent, Vec2 pos) {
		cost.add(new ItemAmount(Items.redstone, 0.05));
	}

	@Override
	public String getName(Vec2 pos, ICircuit parent) {
		return super.getName(pos, parent) + "." + getColor(pos, parent);
	}

	@Override
	public Category getCategory() {
		return Category.WIRE;
	}

	@Override
	public Collection<Integer> getSubtypes() {
		return Arrays.asList(0 << 4, 1 << 4, 2 << 4);
	}
}
