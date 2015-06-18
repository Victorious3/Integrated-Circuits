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

	@Override
	protected int getDelay(Vec2 pos, ICircuit parent) {
		return 2;
	}

	@Override
	public void onPlaced(Vec2 pos, ICircuit parent) {
		updateInput(pos, parent);
		setDelay(pos, parent, getInputFromSide(pos, parent,
				toExternal(pos, parent, ForgeDirection.SOUTH)));
		notifyNeighbours(pos, parent);
	}

	public void onAfterRotation(Vec2 pos, ICircuit parent) {
		onPlaced(pos, parent);
	}

	@Override
	public void onDelay(Vec2 pos, ICircuit parent) {
		setProperty(pos, parent, PROP_RANDOM, new Random().nextInt(7));
		notifyNeighbours(pos, parent);
		setDelay(pos, parent, true);
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
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
		updateInput(pos, parent);
		if (toInternal(pos, parent, side) == ForgeDirection.SOUTH)
			togglePostponedInputChange(pos, parent, side);
	}

	@Override
	public void onPostponedInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		setDelay(pos, parent, getInputFromSide(pos, parent, side));
	}

	@Override
	public void getCraftingCost(CraftingAmount cost, CircuitData parent, Vec2 pos) {
		cost.add(new ItemAmount(Items.redstone, 0.15));
		cost.add(new ItemAmount(Items.glowstone_dust, 0.1));
	}
}
