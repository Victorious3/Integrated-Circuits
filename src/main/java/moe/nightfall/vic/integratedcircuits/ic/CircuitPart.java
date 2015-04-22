package moe.nightfall.vic.integratedcircuits.ic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import moe.nightfall.vic.integratedcircuits.Config;
import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.ic.part.PartIOBit;
import moe.nightfall.vic.integratedcircuits.ic.part.PartMultiplexer;
import moe.nightfall.vic.integratedcircuits.ic.part.PartNull;
import moe.nightfall.vic.integratedcircuits.ic.part.PartSynchronizer;
import moe.nightfall.vic.integratedcircuits.ic.part.PartTorch;
import moe.nightfall.vic.integratedcircuits.ic.part.PartWire;
import moe.nightfall.vic.integratedcircuits.ic.part.cell.PartANDCell;
import moe.nightfall.vic.integratedcircuits.ic.part.cell.PartBufferCell;
import moe.nightfall.vic.integratedcircuits.ic.part.cell.PartInvertCell;
import moe.nightfall.vic.integratedcircuits.ic.part.cell.PartNullCell;
import moe.nightfall.vic.integratedcircuits.ic.part.latch.PartRSLatch;
import moe.nightfall.vic.integratedcircuits.ic.part.latch.PartToggleLatch;
import moe.nightfall.vic.integratedcircuits.ic.part.latch.PartTransparentLatch;
import moe.nightfall.vic.integratedcircuits.ic.part.logic.PartANDGate;
import moe.nightfall.vic.integratedcircuits.ic.part.logic.PartBufferGate;
import moe.nightfall.vic.integratedcircuits.ic.part.logic.PartNANDGate;
import moe.nightfall.vic.integratedcircuits.ic.part.logic.PartNORGate;
import moe.nightfall.vic.integratedcircuits.ic.part.logic.PartNOTGate;
import moe.nightfall.vic.integratedcircuits.ic.part.logic.PartORGate;
import moe.nightfall.vic.integratedcircuits.ic.part.logic.PartXNORGate;
import moe.nightfall.vic.integratedcircuits.ic.part.logic.PartXORGate;
import moe.nightfall.vic.integratedcircuits.ic.part.timed.PartPulseFormer;
import moe.nightfall.vic.integratedcircuits.ic.part.timed.PartRandomizer;
import moe.nightfall.vic.integratedcircuits.ic.part.timed.PartRepeater;
import moe.nightfall.vic.integratedcircuits.ic.part.timed.PartSequencer;
import moe.nightfall.vic.integratedcircuits.ic.part.timed.PartStateCell;
import moe.nightfall.vic.integratedcircuits.ic.part.timed.PartTimer;
import moe.nightfall.vic.integratedcircuits.misc.CraftingAmount;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IProperty;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.ValueProperty;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.collect.Lists;

public abstract class CircuitPart
{
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
	}

	public static void registerPartConfig(int id, CircuitPart part) {
		if (Config.config.getBoolean(part.getClass().getSimpleName().replaceFirst("Part", ""), "PARTS", true, ""))
			registerPart(id, part);
	}

	public static void registerPart(int id, CircuitPart part)
	{
		part.id = id;
		partRegistry.put(id, part);
		idRegistry.put(part.getClass(), id);
		partRegistry.values();
	}
	
	public static Integer getId(CircuitPart part)
	{
		return part.id;
	}
	
	public static Integer getId(Class<? extends CircuitPart> clazz)
	{
		return idRegistry.get(clazz);
	}
	
	public static CircuitPart getPart(Class<? extends CircuitPart> clazz)
	{
		return partRegistry.get(getId(clazz));
	}
	
	/** Returns a CircuitPart from the registry. **/
	public static CircuitPart getPart(int id)
	{
		return partRegistry.get(id);
	}

	/** Returns all the circuit parts that are registered **/
	public static Collection<CircuitPart> getParts() {
		return Collections.unmodifiableCollection(partRegistry.values());
	}

	/** Returns all the circuit parts that are registered and are in a certain category **/
	public static List<CircuitPart> getParts(Category category)
	{
		ArrayList<CircuitPart> parts = new ArrayList<CircuitPart>();
		for (CircuitPart part : CircuitPart.getParts())
			if (part.getCategory() == category)
				parts.add(part);
		return parts;
	}

	public final <T extends Comparable> void setProperty(Vec2 pos, ICircuit parent, IProperty<T> property, T value)
	{
		setState(pos, parent, property.set(value, getState(pos, parent)));
	}
	
	public final <T extends Comparable> T getProperty(Vec2 pos, ICircuit parent, IProperty<T> property)
	{
		return property.get(getState(pos, parent));
	}
	
	public final <T extends Comparable> T invertProperty(Vec2 pos, ICircuit parent, IProperty<T> property)
	{
		int state = getState(pos, parent);
		state = property.invert(state);
		setState(pos, parent, state);
		return property.get(state);
	}
	
	public final void cycleProperty(Vec2 pos, ICircuit parent, ValueProperty property, int offset)
	{
		int value = (Integer)property.get(getState(pos, parent));
		value = (value + offset) % (property.getLimit() + 1);
		setProperty(pos, parent, property, value);
	}
	
	public final void cycleProperty(Vec2 pos, ICircuit parent, ValueProperty property)
	{
		cycleProperty(pos, parent, property, 1);
	}

	public void onPlaced(Vec2 pos, ICircuit parent)
	{
		updateInput(pos, parent);
		notifyNeighbours(pos, parent);
	}

	//TODO Isn't really used anywhere. Leaving the iteration in doesn't make much sense, but maybe the repeater could make use of it.
	@Deprecated
	public void onTick(Vec2 pos, ICircuit parent){}
	
	public void onScheduledTick(Vec2 pos, ICircuit parent){}
	
	public final void scheduleTick(Vec2 pos, ICircuit parent)
	{
		parent.getCircuitData().scheduleTick(pos);
	}
	
	public final void markForUpdate(Vec2 pos, ICircuit parent)
	{
		parent.getCircuitData().markForUpdate(pos);
	}
	
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) {}
	
	public String getName(Vec2 pos, ICircuit parent)
	{
		return getClass().getSimpleName().substring(4).toLowerCase();
	}
	
	public String getLocalizedName(Vec2 pos, ICircuit parent)
	{
		return I18n.format("part." + Constants.MOD_ID + "." + getName(pos, parent) + ".name");
	}
	
	public ArrayList<String> getInformation(Vec2 pos, ICircuit parent, boolean edit, boolean ctrlDown) 
	{
		return Lists.newArrayList();
	}
	
	public void getCraftingCost(CraftingAmount amount) {}
	
	public final int getState(Vec2 pos, ICircuit parent)
	{
		return parent.getCircuitData().getMeta(pos);
	}
	
	public final void setState(Vec2 pos, ICircuit parent, int state)
	{
		parent.getCircuitData().setMeta(pos, state);
	}
	
	public boolean canConnectToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		return true;
	}
	
	public final boolean getInputFromSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		boolean cc = true;
		CircuitPart neighbour = getNeighbourOnSide(pos, parent, side);
		if(neighbour != null) cc = neighbour.canConnectToSide(pos.offset(side), parent, side.getOpposite());
		if(!(canConnectToSide(pos, parent, side) && cc)) return false;
		boolean in = (getProperty(pos, parent, PROP_INPUT) << (side.ordinal() - 2) & 8) != 0;
		return in;
	}
	
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		updateInput(pos, parent);
	}
	
	/** Check every side to update the internal buffer **/
	public final void updateInput(Vec2 pos, ICircuit parent)
	{
		int input = 0;
		input |= (getNeighbourOnSide(pos, parent, ForgeDirection.NORTH) != null ? 
			getNeighbourOnSide(pos, parent, ForgeDirection.NORTH).getOutputToSide(pos.offset(ForgeDirection.NORTH), parent, ForgeDirection.SOUTH) ? 1 : 0 : 0) << 3;
		input |= (getNeighbourOnSide(pos, parent, ForgeDirection.SOUTH) != null ? 
			getNeighbourOnSide(pos, parent, ForgeDirection.SOUTH).getOutputToSide(pos.offset(ForgeDirection.SOUTH), parent, ForgeDirection.NORTH) ? 1 : 0 : 0) << 2; 
		input |= (getNeighbourOnSide(pos, parent, ForgeDirection.WEST) != null ? 
			getNeighbourOnSide(pos, parent, ForgeDirection.WEST).getOutputToSide(pos.offset(ForgeDirection.WEST), parent, ForgeDirection.EAST) ? 1 : 0 : 0) << 1;
		input |= (getNeighbourOnSide(pos, parent, ForgeDirection.EAST) != null ? 
			getNeighbourOnSide(pos, parent, ForgeDirection.EAST).getOutputToSide(pos.offset(ForgeDirection.EAST), parent, ForgeDirection.WEST) ? 1 : 0 : 0);
		setProperty(pos, parent, PROP_INPUT, input);
	}
	
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		return false;
	}
	
	public final void notifyNeighbours(Vec2 pos, ICircuit parent)
	{
		for(int i = 2; i < 6; i++)
		{
			ForgeDirection fd = ForgeDirection.getOrientation(i);
			Vec2 pos2 = pos.offset(fd);
			CircuitPart part = getNeighbourOnSide(pos, parent, fd);
			
			boolean b = canConnectToSide(pos, parent, fd) 
				&& part.canConnectToSide(pos2, parent, fd.getOpposite()) 
				&& getOutputToSide(pos, parent, fd) != part.getInputFromSide(pos2, parent, fd.getOpposite());
			
			if(part != null && b)
			{
				part.onInputChange(pos2, parent, fd.getOpposite());
				part.markForUpdate(pos2, parent);
			}
			
			markForUpdate(pos, parent);
		}
	}
	
	public final CircuitPart getNeighbourOnSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{	
		return parent.getCircuitData().getPart(pos.offset(side));
	}
	
	public final boolean getInput(Vec2 pos, ICircuit parent)
	{
		return getInputFromSide(pos, parent, ForgeDirection.NORTH)
			|| getInputFromSide(pos, parent, ForgeDirection.EAST)
			|| getInputFromSide(pos, parent, ForgeDirection.SOUTH)
			|| getInputFromSide(pos, parent, ForgeDirection.WEST);
	}
}
