package vic.mod.integratedcircuits.ic;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.parts.PartIOBit;
import vic.mod.integratedcircuits.ic.parts.PartMultiplexer;
import vic.mod.integratedcircuits.ic.parts.PartNull;
import vic.mod.integratedcircuits.ic.parts.PartSynchronizer;
import vic.mod.integratedcircuits.ic.parts.PartTorch;
import vic.mod.integratedcircuits.ic.parts.PartWire;
import vic.mod.integratedcircuits.ic.parts.cell.PartANDCell;
import vic.mod.integratedcircuits.ic.parts.cell.PartBufferCell;
import vic.mod.integratedcircuits.ic.parts.cell.PartInvertCell;
import vic.mod.integratedcircuits.ic.parts.cell.PartNullCell;
import vic.mod.integratedcircuits.ic.parts.latch.PartRSLatch;
import vic.mod.integratedcircuits.ic.parts.latch.PartToggleLatch;
import vic.mod.integratedcircuits.ic.parts.latch.PartTranspartentLatch;
import vic.mod.integratedcircuits.ic.parts.logic.PartANDGate;
import vic.mod.integratedcircuits.ic.parts.logic.PartBufferGate;
import vic.mod.integratedcircuits.ic.parts.logic.PartNANDGate;
import vic.mod.integratedcircuits.ic.parts.logic.PartNORGate;
import vic.mod.integratedcircuits.ic.parts.logic.PartNOTGate;
import vic.mod.integratedcircuits.ic.parts.logic.PartORGate;
import vic.mod.integratedcircuits.ic.parts.logic.PartXNORGate;
import vic.mod.integratedcircuits.ic.parts.logic.PartXORGate;
import vic.mod.integratedcircuits.ic.parts.timed.PartPulseFormer;
import vic.mod.integratedcircuits.ic.parts.timed.PartRandomizer;
import vic.mod.integratedcircuits.ic.parts.timed.PartRepeater;
import vic.mod.integratedcircuits.ic.parts.timed.PartSequencer;
import vic.mod.integratedcircuits.ic.parts.timed.PartStateCell;
import vic.mod.integratedcircuits.ic.parts.timed.PartTimer;
import vic.mod.integratedcircuits.util.CraftingAmount;

public abstract class CircuitPart implements Cloneable
{
	private static HashMap<Integer, CircuitPart> partRegistry = new HashMap<Integer, CircuitPart>();
	private static HashMap<Class<? extends CircuitPart>, Integer> idRegistry = new HashMap<Class<? extends CircuitPart>, Integer>();
	
	static 
	{
		registerPart(0, new PartNull());
		registerPart(1, new PartWire());
		registerPart(2, new PartTorch());
		registerPart(3, new PartANDGate());
		registerPart(4, new PartORGate());
		registerPart(5, new PartNANDGate());
		registerPart(6, new PartNORGate());
		registerPart(7, new PartBufferGate());
		registerPart(8, new PartNOTGate());
		registerPart(9, new PartMultiplexer());
		registerPart(10, new PartRepeater());
		registerPart(11, new PartTimer());
		registerPart(12, new PartSequencer());
		registerPart(13, new PartStateCell());
		registerPart(14, new PartRandomizer());
		registerPart(15, new PartPulseFormer());
		registerPart(16, new PartRSLatch());
		registerPart(17, new PartToggleLatch());
		registerPart(18, new PartTranspartentLatch());
		registerPart(19, new PartXORGate());
		registerPart(20, new PartXNORGate());
		registerPart(21, new PartSynchronizer());
		registerPart(22, new PartNullCell());
		registerPart(23, new PartIOBit());
		registerPart(24, new PartInvertCell());
		registerPart(25, new PartBufferCell());
		registerPart(26, new PartANDCell());
	}
	
	public static void registerPart(int id, CircuitPart part)
	{
		part.id = id;
		partRegistry.put(id, part);
		idRegistry.put(part.getClass(), id);
	}
	
	public static Integer getId(CircuitPart part)
	{
		return part.id;
	}
	
	public static Integer getIdFromClass(Class<? extends CircuitPart> clazz)
	{
		return idRegistry.get(clazz);
	}
	
	/** Returns a CircuitPart from the registry. **/
	@Deprecated
	public static CircuitPart getPart(int id)
	{
		return partRegistry.get(id).clone();
	}
	
	/** Returns a CircuitPart from the registry, already prepared. **/
	public static CircuitPart getPart(int id, int x, int y, CircuitData parent)
	{
		return partRegistry.get(id).clone().prepare(x, y, parent);
	}

	private int id;
	private int x;
	private int y;
	private CircuitData parent;
	
	public void onPlaced()
	{
		updateInput();
		notifyNeighbours();
	}
	
	public final CircuitPart prepare(int x, int y, CircuitData parent)
	{
		this.x = x;
		this.y = y;
		this.parent = parent;
		return this;
	}
	
	@Override
	protected final CircuitPart clone()
	{
		try {
			return (CircuitPart) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void onTick(){}
	
	public void onScheduledTick(){}
	
	public final void scheduleTick()
	{
		getData().scheduleTick(getX(), getY());
	}
	
	public final void markForUpdate()
	{
		getData().markForUpdate(getX(), getY());
	}
	
	public void onClick(int button, boolean ctrl){}
	
	public final int getX()
	{
		return x;
	}
	
	public final int getY()
	{
		return y;
	}
	
	public String getName()
	{
		return getClass().getSimpleName().substring(4);
	}
	
	public ArrayList<String> getInformation() 
	{
		return new ArrayList<String>();
	}
	
	public void getCraftingCost(CraftingAmount amount)
	{
		
	}
	
	public final CircuitData getData()
	{
		return parent;
	}
	
	public final int getState()
	{
		return parent.getMeta(getX(), getY());
	}
	
	public final void setState(int state)
	{
		parent.setMeta(getX(), getY(), state);
	}
	
	public boolean canConnectToSide(ForgeDirection side)
	{
		return true;
	}
	
	public final boolean getInputFromSide(ForgeDirection side)
	{
		boolean cc = true;
		CircuitPart neighbour = getNeighbourOnSide(side);
		if(neighbour != null) cc = neighbour.canConnectToSide(side.getOpposite());
		if(!(canConnectToSide(side) && cc)) return false;
		boolean in = ((getState() & 15) << (side.ordinal() - 2) & 8) > 0;
		return in;
	}
	
	public void onInputChange(ForgeDirection side)
	{
		updateInput();
	}
	
	public final void updateInput()
	{
		int newState = 0;
		//Check every side to update the internal buffer.
		newState |= (getNeighbourOnSide(ForgeDirection.NORTH) != null ? 
			getNeighbourOnSide(ForgeDirection.NORTH).getOutputToSide(ForgeDirection.NORTH.getOpposite()) ? 1 : 0 : 0) << 3;
		newState |= (getNeighbourOnSide(ForgeDirection.SOUTH) != null ? 
			getNeighbourOnSide(ForgeDirection.SOUTH).getOutputToSide(ForgeDirection.SOUTH.getOpposite()) ? 1 : 0 : 0) << 2; 
		newState |= (getNeighbourOnSide(ForgeDirection.WEST) != null ? 
			getNeighbourOnSide(ForgeDirection.WEST).getOutputToSide(ForgeDirection.WEST.getOpposite()) ? 1 : 0 : 0) << 1;
		newState |= (getNeighbourOnSide(ForgeDirection.EAST) != null ? 
			getNeighbourOnSide(ForgeDirection.EAST).getOutputToSide(ForgeDirection.EAST.getOpposite()) ? 1 : 0 : 0);
		setState(getState() & ~15 | newState);
	}
	
	public boolean getOutputToSide(ForgeDirection side)
	{
		return false;
	}
	
	public final void notifyNeighbours()
	{
		for(int i = 2; i < 6; i++)
		{
			ForgeDirection fd = ForgeDirection.getOrientation(i);
			CircuitPart part = getNeighbourOnSide(fd);
			if(part != null && canConnectToSide(fd) && part.canConnectToSide(fd.getOpposite()) && getOutputToSide(fd) != part.getInputFromSide(fd.getOpposite()))
			{
				part.onInputChange(fd.getOpposite());
				part.markForUpdate();
			}
			markForUpdate();
		}
	}
	
	public final CircuitPart getNeighbourOnSide(ForgeDirection side)
	{	
		return parent.getPart(x + side.offsetX, y + side.offsetZ);
	}
	
	public final boolean getInput()
	{
		return getInputFromSide(ForgeDirection.NORTH)
			|| getInputFromSide(ForgeDirection.EAST)
			|| getInputFromSide(ForgeDirection.SOUTH)
			|| getInputFromSide(ForgeDirection.WEST);
	}
}