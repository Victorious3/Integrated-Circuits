package moe.nightfall.vic.integratedcircuits.cp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import moe.nightfall.vic.integratedcircuits.Config;
import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.cp.part.PartIOBit;
import moe.nightfall.vic.integratedcircuits.cp.part.PartMultiplexer;
import moe.nightfall.vic.integratedcircuits.cp.part.PartNull;
import moe.nightfall.vic.integratedcircuits.cp.part.PartTorch;
import moe.nightfall.vic.integratedcircuits.cp.part.PartTunnel;
import moe.nightfall.vic.integratedcircuits.cp.part.PartWire;
import moe.nightfall.vic.integratedcircuits.cp.part.cell.PartANDCell;
import moe.nightfall.vic.integratedcircuits.cp.part.cell.PartBufferCell;
import moe.nightfall.vic.integratedcircuits.cp.part.cell.PartInvertCell;
import moe.nightfall.vic.integratedcircuits.cp.part.cell.PartNullCell;
import moe.nightfall.vic.integratedcircuits.cp.part.latch.PartRSLatch;
import moe.nightfall.vic.integratedcircuits.cp.part.latch.PartToggleLatch;
import moe.nightfall.vic.integratedcircuits.cp.part.latch.PartTransparentLatch;
import moe.nightfall.vic.integratedcircuits.cp.part.logic.PartANDGate;
import moe.nightfall.vic.integratedcircuits.cp.part.logic.PartBufferGate;
import moe.nightfall.vic.integratedcircuits.cp.part.logic.PartNANDGate;
import moe.nightfall.vic.integratedcircuits.cp.part.logic.PartNORGate;
import moe.nightfall.vic.integratedcircuits.cp.part.logic.PartNOTGate;
import moe.nightfall.vic.integratedcircuits.cp.part.logic.PartORGate;
import moe.nightfall.vic.integratedcircuits.cp.part.logic.PartXNORGate;
import moe.nightfall.vic.integratedcircuits.cp.part.logic.PartXORGate;
import moe.nightfall.vic.integratedcircuits.cp.part.timed.PartPulseFormer;
import moe.nightfall.vic.integratedcircuits.cp.part.timed.PartRandomizer;
import moe.nightfall.vic.integratedcircuits.cp.part.timed.PartRepeater;
import moe.nightfall.vic.integratedcircuits.cp.part.timed.PartSequencer;
import moe.nightfall.vic.integratedcircuits.cp.part.timed.PartStateCell;
import moe.nightfall.vic.integratedcircuits.cp.part.timed.PartSynchronizer;
import moe.nightfall.vic.integratedcircuits.cp.part.timed.PartTimer;
import moe.nightfall.vic.integratedcircuits.misc.CraftingAmount;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IProperty;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.ValueProperty;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class CircuitPart {
	private static HashMap<Integer, CircuitPart> partRegistry = new HashMap<Integer, CircuitPart>();
	private static HashMap<Class<? extends CircuitPart>, Integer> idRegistry = new HashMap<Class<? extends CircuitPart>, Integer>();

	private int id;
	public final PropertyStitcher stitcher = new PropertyStitcher();
	public final IntProperty PROP_INPUT = new IntProperty("INPUT", stitcher, 15);

	public enum Category {
		NONE, MISC, LATCH, GATE, NGATE, CELL, WIRE, TORCH
	}

	public Category getCategory() {
		return Category.NONE;
	}

	public static void registerParts() {
		registerPart(0, new PartNull());
		registerPartConfig(1, new PartWire());
		registerPartConfig(2, new PartTorch());
		registerPartConfig(3, new PartANDGate());
		registerPartConfig(4, new PartORGate());
		registerPartConfig(5, new PartNANDGate());
		registerPartConfig(6, new PartNORGate());
		registerPartConfig(7, new PartBufferGate());
		registerPartConfig(8, new PartNOTGate());
		registerPartConfig(9, new PartMultiplexer());
		registerPartConfig(10, new PartRepeater());
		registerPartConfig(11, new PartTimer());
		registerPartConfig(12, new PartSequencer());
		registerPartConfig(13, new PartStateCell());
		registerPartConfig(14, new PartRandomizer());
		registerPartConfig(15, new PartPulseFormer());
		registerPartConfig(16, new PartRSLatch());
		registerPartConfig(17, new PartToggleLatch());
		registerPartConfig(18, new PartTransparentLatch());
		registerPartConfig(19, new PartXORGate());
		registerPartConfig(20, new PartXNORGate());
		registerPartConfig(21, new PartSynchronizer());
		registerPartConfig(22, new PartNullCell());
		registerPart(23, new PartIOBit());
		registerPartConfig(24, new PartInvertCell());
		registerPartConfig(25, new PartBufferCell());
		registerPartConfig(26, new PartANDCell());
		registerPartConfig(27, new PartTunnel());
	}

	public static void registerPartConfig(int id, CircuitPart part) {
		if (Config.config.getBoolean(part.getClass().getSimpleName().replaceFirst("Part", ""), "PARTS", true, ""))
			registerPart(id, part);
	}

	public static void registerPart(int id, CircuitPart part) {
		part.id = id;
		partRegistry.put(id, part);
		idRegistry.put(part.getClass(), id);
		partRegistry.values();
	}

	public static Integer getId(CircuitPart part) {
		return part.id;
	}

	public static Integer getId(Class<? extends CircuitPart> clazz) {
		return idRegistry.get(clazz);
	}

	public static <T extends CircuitPart> T getPart(Class<T> clazz) {
		return (T) partRegistry.get(getId(clazz));
	}

	/** Returns a CircuitPart from the registry. **/
	public static CircuitPart getPart(int id) {
		return partRegistry.get(id);
	}

	/** Returns all the circuit parts that are registered **/
	public static Collection<CircuitPart> getParts() {
		return Collections.unmodifiableCollection(partRegistry.values());
	}

	/**
	 * Returns all the circuit parts that are registered and are in a certain
	 * category
	 **/
	public static List<CircuitPart> getParts(Category category) {
		ArrayList<CircuitPart> parts = new ArrayList<CircuitPart>();
		for (CircuitPart part : CircuitPart.getParts())
			if (part.getCategory() == category)
				parts.add(part);
		return parts;
	}

	public final <T extends Comparable> void setProperty(Vec2 pos, ICircuit parent, IProperty<T> property, T value) {
		setState(pos, parent, property.set(value, getState(pos, parent)));
	}

	public final <T extends Comparable> T getProperty(Vec2 pos, ICircuit parent, IProperty<T> property) {
		return property.get(getState(pos, parent));
	}

	public final <T extends Comparable> T invertProperty(Vec2 pos, ICircuit parent, IProperty<T> property) {
		int state = getState(pos, parent);
		state = property.invert(state);
		setState(pos, parent, state);
		return property.get(state);
	}

	public final void cycleProperty(Vec2 pos, ICircuit parent, ValueProperty property, int offset) {
		int value = (Integer) property.get(getState(pos, parent));
		value = (value + offset) % (property.getLimit() + 1);
		setProperty(pos, parent, property, value);
	}

	public final void cycleProperty(Vec2 pos, ICircuit parent, ValueProperty property) {
		cycleProperty(pos, parent, property, 1);
	}

	public void onPlaced(Vec2 pos, ICircuit parent) {
		updateInput(pos, parent);
		notifyNeighbours(pos, parent);
	}

	public void onScheduledTick(Vec2 pos, ICircuit parent) {
	}
	
	public void onPostponedInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
	}

	public final void scheduleTick(Vec2 pos, ICircuit parent) {
		parent.getCircuitData().scheduleTick(pos);
	}

	public final void markForUpdate(Vec2 pos, ICircuit parent) {
		parent.getCircuitData().markForUpdate(pos);
	}

	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) {
	}

	public String getName(Vec2 pos, ICircuit parent) {
		return getClass().getSimpleName().substring(4).toLowerCase();
	}

	public String getLocalizedName(Vec2 pos, ICircuit parent) {
		return I18n.format("part." + Constants.MOD_ID + "." + getName(pos, parent) + ".name");
	}

	public ArrayList<String> getInformation(Vec2 pos, ICircuit parent, boolean edit, boolean ctrlDown) {
		return Lists.newArrayList();
	}

	public void getCraftingCost(CraftingAmount amount, CircuitData parent, Vec2 pos) {
	}

	public final int getState(Vec2 pos, ICircuit parent) {
		return parent.getCircuitData().getMeta(pos);
	}

	public final void setState(Vec2 pos, ICircuit parent, int state) {
		parent.getCircuitData().setMeta(pos, state);
	}

	public boolean canConnectToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		return true;
	}

	public final boolean hasConnectionOnSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		if (side == ForgeDirection.UNKNOWN)
			return false;
		CircuitPart neighbour = getNeighbourOnSide(pos, parent, side);
		if (neighbour == null)
			return false;
		return canConnectToSide(pos, parent, side)
			&& neighbour.canConnectToSide(pos.offset(side), parent, side.getOpposite());
	}

	public final boolean getCachedInputFromSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		if (side == ForgeDirection.UNKNOWN)
			return false;
		boolean in = (getProperty(pos, parent, PROP_INPUT) << (side.ordinal() - 2) & 8) != 0;
		return in;
	}

	public final boolean getInputFromSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		return hasConnectionOnSide(pos, parent, side) && getCachedInputFromSide(pos, parent, side);
	}

	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		updateInput(pos, parent);
	}

	// To be used ONLY inside onInputChange handler.
	// Pass updates you want to process synchronously there.
	// Make sure you do so either ALWAYS or NEVER for specific side.
	public void togglePostponedInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		parent.getCircuitData().togglePostponedInputChange(pos, side);
	}

	public void scheduleInputChange(Vec2 pos, ICircuit parent, ForgeDirection side, boolean differs) {
		parent.getCircuitData().scheduleInputChange(pos, side, differs);
	}

	// Check every side to update the internal buffer
	// Must ONLY be called from onInputChange handler.
	public final void updateInput(Vec2 pos, ICircuit parent) {
		int input = 0;
		for (int i = 2; i < 6; i++) {
			ForgeDirection fd = ForgeDirection.getOrientation(i);
			if (hasConnectionOnSide(pos, parent, fd) && getNeighbourOnSide(pos, parent, fd)
					.getOutputToSide(pos.offset(fd), parent, fd.getOpposite()))
				input |= 8 >> (i - 2);
		}
		setProperty(pos, parent, PROP_INPUT, input);
	}

	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		return false;
	}

	public final void notifyNeighbours(Vec2 pos, ICircuit parent) {
		for (int i = 2; i < 6; i++) {
			ForgeDirection fd = ForgeDirection.getOrientation(i);
			CircuitPart part = getNeighbourOnSide(pos, parent, fd);

			if (part != null) {
				ForgeDirection fd2 = fd.getOpposite();
				Vec2 pos2 = pos.offset(fd);
				boolean conn = hasConnectionOnSide(pos, parent, fd);
				boolean out = getOutputToSide(pos, parent, fd);
				boolean in = part.getCachedInputFromSide(pos2, parent, fd2);
				if ((conn && out) != in) {
					part.scheduleInputChange(pos2, parent, fd2, true);
					part.markForUpdate(pos2, parent);
				} else if (conn && (out == in)) {
					part.scheduleInputChange(pos2, parent, fd2, false);
					part.markForUpdate(pos2, parent);
				}
				markForUpdate(pos, parent);
			}
		}
	}

	public final CircuitPart getNeighbourOnSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		return parent.getCircuitData().getPart(pos.offset(side));
	}

	public final boolean getInput(Vec2 pos, ICircuit parent) {
		return getInputFromSide(pos, parent, ForgeDirection.NORTH)
				|| getInputFromSide(pos, parent, ForgeDirection.EAST)
				|| getInputFromSide(pos, parent, ForgeDirection.SOUTH)
				|| getInputFromSide(pos, parent, ForgeDirection.WEST);
	}

	@SideOnly(Side.CLIENT)
	public abstract void renderPart(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type);

	/** Gets called on a client update */
	public void onChanged(Vec2 pos, ICircuit parent, int oldMeta) {
		updateInput(pos, parent);
		notifyNeighbours(pos, parent);
	}

	/** Gets called when the client removes this */
	public void onRemoved(Vec2 pos, ICircuit parent) {
	}

	/**
	 * Gets the subtypes of this part
	 * @return an empty collection, or a collection with all the subtypes
	 */
	public Collection<Integer> getSubtypes() {
		return Collections.emptyList();
	}
}
