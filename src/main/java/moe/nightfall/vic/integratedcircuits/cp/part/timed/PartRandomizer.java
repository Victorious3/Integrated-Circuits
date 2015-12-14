package moe.nightfall.vic.integratedcircuits.cp.part.timed;

import java.util.Random;

import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.CraftingAmount;
import moe.nightfall.vic.integratedcircuits.misc.ItemAmount;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.init.Items;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class PartRandomizer extends PartDelayedAction {
	public final IntProperty PROP_RANDOM = new IntProperty("RANDOM", stitcher, 7);
	private Random random = new Random();

	@Override
	protected int getDelay(Vec2 pos, ICircuit parent) {
		return 2;
	}

	@Override
	public void onDelay(Vec2 pos, ICircuit parent) {
		setProperty(pos, parent, PROP_RANDOM, random.nextInt(8)); // 0..7
		setDelay(pos, parent, true);
		notifyNeighbours(pos, parent);
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, EnumFacing side) {
		EnumFacing s2 = toInternal(pos, parent, side);
		if (s2 == EnumFacing.SOUTH)
			return false;
		int rand = getProperty(pos, parent, PROP_RANDOM);
		if (s2 == EnumFacing.EAST && (rand >> 2 & 1) != 0)
			return true;
		if (s2 == EnumFacing.WEST && (rand >> 1 & 1) != 0)
			return true;
		if (s2 == EnumFacing.NORTH && (rand & 1) != 0)
			return true;
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(5, 1);
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent) {
		scheduleTick(pos, parent);
	}

	@Override
	public void onScheduledTick(Vec2 pos, ICircuit parent) {
		if (!getInputFromSide(pos, parent, toExternal(pos, parent, EnumFacing.SOUTH))) {
			// Do not call super() if input is low,
			//  to prevent output change on the same tick when it goes low.
			setDelay(pos, parent, false);
		} else {
			super.onScheduledTick(pos, parent); // Handle delay countdown
			if (!isDelayActive(pos, parent))
				// Input went high on this tick.
				// Change random output right now to respect 1-tick input pulses.
				// Output still changes at most once in 2 ticks.
				// This also schedules the next delay.
				onDelay(pos, parent);
		}
	}

	@Override
	public void getCraftingCost(CraftingAmount cost, CircuitData parent, Vec2 pos) {
		cost.add(new ItemAmount(Items.redstone, 0.15));
		cost.add(new ItemAmount(Items.glowstone_dust, 0.1));
	}
}
