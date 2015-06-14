package moe.nightfall.vic.integratedcircuits.cp.part.timed;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.CraftingAmount;
import moe.nightfall.vic.integratedcircuits.misc.ItemAmount;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import net.minecraft.init.Items;
import net.minecraftforge.common.util.ForgeDirection;

public class PartRandomizer extends PartDelayedAction {
	public final IntProperty PROP_RANDOM = new IntProperty("RANDOM", stitcher, 7);

	static final int actionDelay = 2;

	@Override
	public void onPlaced(Vec2 pos, ICircuit parent) {
		setDelay(pos, parent, actionDelay);
	}

	@Override
	public void onDelay(Vec2 pos, ICircuit parent) {
		setProperty(pos, parent, PROP_RANDOM, new Random().nextInt(7));
		notifyNeighbours(pos, parent);
		setDelay(pos, parent, actionDelay);
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		if (!getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH)))
			return false;
		ForgeDirection s2 = toInternal(pos, parent, side);
		if (s2 == ForgeDirection.SOUTH)
			return false;
		int rand = getProperty(pos, parent, PROP_RANDOM);
		if (s2 == ForgeDirection.EAST && (rand >> 2 & 1) != 0)
			return true;
		if (s2 == ForgeDirection.WEST && (rand >> 1 & 1) != 0)
			return true;
		if (s2 == ForgeDirection.NORTH && (rand & 1) != 0)
			return true;
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(5, 1);
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		super.onInputChange(pos, parent, side);
		ForgeDirection s2 = toInternal(pos, parent, side);
		if (s2 != ForgeDirection.SOUTH)
			return;
		if (getInputFromSide(pos, parent, side))
			setDelay(pos, parent, actionDelay);
		else
			setDelay(pos, parent, 0);
	}

	@Override
	public void getCraftingCost(CraftingAmount cost, CircuitData parent, Vec2 pos) {
		cost.add(new ItemAmount(Items.redstone, 0.15));
		cost.add(new ItemAmount(Items.glowstone_dust, 0.1));
	}
}
