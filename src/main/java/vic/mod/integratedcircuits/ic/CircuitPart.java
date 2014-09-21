package vic.mod.integratedcircuits.ic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.util.MiscUtils;

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
	
	public static class PartNull extends CircuitPart
	{
		@Override
		public void onPlaced() 
		{
			for(int i = 2; i < 6; i++)
			{
				ForgeDirection fd = ForgeDirection.getOrientation(i);
				CircuitPart part = getNeighbourOnSide(fd);
				part.onInputChange(fd.getOpposite());
			}
		}
	}
	
	public static class PartIOBit extends CircuitPart
	{
		public final int getRotation()
		{
			return (getState() & 48) >> 4;
		}
		
		public final void setRotation(int rotation)
		{
			setState(getState() & ~48 | rotation << 4);
		}
		
		public final int getFrequency()
		{
			return (getState() & 960) >> 6;
		}
		
		public final void setFrequency(int frequency)
		{
			setState(getState() & ~960 | frequency << 6);
		}

		@Override
		public void onInputChange(ForgeDirection side) 
		{
			updateInput();
			ForgeDirection dir = MiscUtils.getDirection(getRotation());
			if(side == dir.getOpposite())
				getData().getCircuit().setOutputToSide(dir, getFrequency(), getInputFromSide(side));
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection dir = MiscUtils.getDirection(getRotation());
			if(side == dir.getOpposite())
				return getData().getCircuit().getInputFromSide(dir, getFrequency());
			else return false;
		}
		
		public boolean isPowered()
		{
			ForgeDirection dir = MiscUtils.getDirection(getRotation()).getOpposite();
			return getOutputToSide(dir) || getNeighbourOnSide(dir).getOutputToSide(dir.getOpposite());
		}
	}
	
	public static class PartWire extends CircuitPart
	{
		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			return getInput() && !getInputFromSide(side);
		}

		@Override
		public void onInputChange(ForgeDirection side) 
		{
			super.onInputChange(side);
			notifyNeighbours();
		}

		public int getColor()
		{
			return (getState() & ~16) >> 5;
		}

		@Override
		public boolean canConnectToSide(ForgeDirection side) 
		{
			CircuitPart part = getNeighbourOnSide(side);
			if(part instanceof PartWire)
			{
				int pcolor = ((PartWire)part).getColor();
				int color = getColor();
				if(pcolor == 0 || color == 0) return true;
				return color == pcolor;
			}
			return true;
		}
	}
	
	public static class PartTorch extends CircuitPart
	{
		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			return true;
		}
	}
	
	public static abstract class PartGate extends CircuitPart
	{
		public final int getRotation()
		{
			return (getState() & 48) >> 4;
		}
		
		public final void setRotation(int rotation)
		{
			setState(getState() & ~48 | rotation << 4);
			notifyNeighbours();
		}
		
		@Override
		public void onClick(int button, boolean ctrl) 
		{
			if(button == 0 && !ctrl)
			{
				int rot = getRotation() + 1;
				setRotation(rot > 3 ? 0 : rot);
			}
			markForUpdate();
		}

		@Override
		public void onScheduledTick() 
		{
			notifyNeighbours();
		}
	}
	
	public static class Part3I1O extends PartGate
	{
		@Override
		public void onClick(int button, boolean ctrl) 
		{
			super.onClick(button, ctrl);
			if(button == 0 && ctrl)
			{
				int i1 = (getState() & 384) >> 7;
				i1 = i1 + 1 > 3 ? 0 : i1 + 1;
				setState(getState() & ~384 | i1 << 7);
			}
		}
		
		@Override
		public boolean canConnectToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			if(s2 == ForgeDirection.NORTH) return true;
			int i = (getState() & 384) >> 7;
			if(s2 == ForgeDirection.EAST && i == 1) return false;
			if(s2 == ForgeDirection.SOUTH && i == 2) return false;
			if(s2 == ForgeDirection.WEST && i == 3) return false;
			return true;
		}

		@Override
		public void onInputChange(ForgeDirection side) 
		{
			updateInput();
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			if(s2 != ForgeDirection.NORTH) scheduleTick();
		}
	}
	
	public static class Part1I3O extends PartGate
	{
		@Override
		public void onClick(int button, boolean ctrl) 
		{
			super.onClick(button, ctrl);
			if(button == 1 && ctrl)
			{
				int i1 = (getState() & 896) >> 7;
				i1 = i1 + 1 > 5 ? 0 : i1 + 1;
				setState(getState() & ~896 | i1 << 7);
			}
		}

		@Override
		public void onInputChange(ForgeDirection side) 
		{
			updateInput();
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			if(s2 == ForgeDirection.SOUTH) scheduleTick();
		}
		
		@Override
		public boolean canConnectToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			if(s2 == ForgeDirection.NORTH) return true;
			int i = (getState() & 896) >> 7;
			if(s2 == ForgeDirection.EAST && (i == 3 || i == 4 || i == 5)) return false;
			if(s2 == ForgeDirection.SOUTH && (i == 2 || i == 4 || i == 6)) return false;
			if(s2 == ForgeDirection.WEST && (i == 1 || i == 5 || i == 6)) return false;
			return true;
		}
	}
	
	public static class PartANDGate extends Part3I1O
	{
		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			ForgeDirection s3 = MiscUtils.rotn(ForgeDirection.SOUTH, getRotation());
			ForgeDirection s4 = MiscUtils.rotn(ForgeDirection.EAST, getRotation());
			ForgeDirection s5 = MiscUtils.rotn(ForgeDirection.WEST, getRotation());
			return s2 == ForgeDirection.NORTH 
				&& (!canConnectToSide(s3) || getInputFromSide(s3))
				&& (!canConnectToSide(s4) || getInputFromSide(s4))
				&& (!canConnectToSide(s5) || getInputFromSide(s5));
		}
	}
	
	public static class PartORGate extends Part3I1O
	{
		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			ForgeDirection s3 = MiscUtils.rotn(ForgeDirection.SOUTH, getRotation());
			ForgeDirection s4 = MiscUtils.rotn(ForgeDirection.EAST, getRotation());
			ForgeDirection s5 = MiscUtils.rotn(ForgeDirection.WEST, getRotation());
			return s2 == ForgeDirection.NORTH && (getInputFromSide(s3)
				|| getInputFromSide(s4) || getInputFromSide(s5));
		}
	}
	
	public static class PartNORGate extends PartORGate
	{
		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			if(s2 == ForgeDirection.NORTH) return !super.getOutputToSide(side);
			return false;
		}
	}
	
	public static class PartNANDGate extends PartANDGate
	{
		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			if(s2 == ForgeDirection.NORTH) return !super.getOutputToSide(side);
			return false;
		}
	}
	
	public static class PartBufferGate extends Part1I3O
	{
		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			if(s2 != ForgeDirection.SOUTH) return getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, getRotation()));
			return false;
		}
	}
	
	public static class PartNOTGate extends Part1I3O
	{
		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			if(s2 != ForgeDirection.SOUTH) return !getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, getRotation()));
			return false;
		}
	}

	public static class PartXORGate extends PartGate
	{
		@Override
		public boolean canConnectToSide(ForgeDirection side) 
		{
			return MiscUtils.rotn(side, getRotation()) != ForgeDirection.SOUTH;
		}

		@Override
		public void onInputChange(ForgeDirection side) 
		{
			updateInput();
			ForgeDirection fd = MiscUtils.rotn(side, -getRotation());
			if(fd == ForgeDirection.EAST || fd == ForgeDirection.WEST) scheduleTick();
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection fd = MiscUtils.rotn(side, -getRotation());
			return fd == ForgeDirection.NORTH &&
				(getInputFromSide(MiscUtils.rotn(ForgeDirection.EAST, getRotation()))
				!= getInputFromSide(MiscUtils.rotn(ForgeDirection.WEST, getRotation())));
		}	
	}
	
	public static class PartXNORGate extends PartXORGate
	{
		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			if(s2 == ForgeDirection.NORTH) return !super.getOutputToSide(side);
			return false;
		}
	}
	
	public static class PartMultiplexer extends PartGate
	{
		@Override
		public void onInputChange(ForgeDirection side) 
		{
			if(MiscUtils.rotn(side, -getRotation()) != ForgeDirection.NORTH) super.onInputChange(side);
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			if(MiscUtils.rotn(side, -getRotation()) != ForgeDirection.NORTH) return false;
			if(getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, getRotation())))
				return getInputFromSide(MiscUtils.rotn(ForgeDirection.WEST, getRotation()));
			else return getInputFromSide(MiscUtils.rotn(ForgeDirection.EAST, getRotation()));
		}	
	}

	/** Uses 8 bits for the delay. 255 ticks = 12.75 seconds*/
	public static abstract class PartDelayedAction extends PartGate
	{
		protected abstract int getDelay();
		
		public int getCurrentDelay()
		{
			return ((getState() & 32640) >> 7);
		}
		
		@Override
		public void onScheduledTick() 
		{
			if((getState() & 64) > 0)
			{
				int counter = getCurrentDelay();
				counter--;
				if(counter == 0)
				{
					setState(getState() & ~32640);
					setState(getState() & ~64);
					onDelay();
				}
				else 
				{
					setState(getState() & ~32640 | counter << 7);
					scheduleTick();
				}
			}	
		}
		
		public void onDelay()
		{
			notifyNeighbours();
		}

		protected void setDelay(boolean b)
		{
			setState(getState() & ~32640 | getDelay() << 7);
			if(b) 
			{
				setState(getState() | 64);
				scheduleTick();
			}
			else setState(getState() & ~64);
		}
	}
	
	public static class PartRepeater extends PartDelayedAction
	{
		@Override
		protected int getDelay() 
		{
			return ((getState() & 16711680) >> 16);
		}
		
		@Override
		public void onPlaced()
		{
			setState(2 << 16);
			setState(getState() | 32768);
		}

		@Override
		public void onClick(int button, boolean ctrl) 
		{
			super.onClick(button, ctrl);
			if(button == 0 && ctrl)
			{
				int delay = getDelay();
				int newDelay = 0;
				switch (delay) {
				case 2 : delay = 4; break;
				case 4 : delay = 8; break;
				case 8 : delay = 16; break;
				case 16 : delay = 32; break;
				case 32 : delay = 64; break;
				case 64 : delay = 128; break;
				case 128 : delay = 255; break;
				default : delay = 2; break;
				}
				setState((getState() & ~16711680) | delay << 16);
			}
		}

		@Override
		public boolean canConnectToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			return s2 == ForgeDirection.NORTH || s2 == ForgeDirection.SOUTH;
		}
		
		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			if(s2 != ForgeDirection.NORTH) return false;
			return getCurrentDelay() > 0 ? (getState() & 32768) > 0 : (getState() & 32768) == 0;
		}

		@Override
		public void onDelay() 
		{	
			setState(getState() ^ 32768);
			super.onDelay();
		}

		@Override
		public void onInputChange(ForgeDirection side) 
		{
			updateInput();
			if(MiscUtils.rotn(side, -getRotation()) != ForgeDirection.SOUTH) return;
			if(getCurrentDelay() == 0)
			{
				if(((getState() & 32768) >> 15) != (getInputFromSide(side) ? 1 : 0))
				{
					setState(getState() & ~32768 | (getInputFromSide(side) ? 1 : 0) << 15);
				}
			}
			setDelay(true);
		}

		@Override
		public ArrayList<String> getInformation() 
		{
			ArrayList<String> list = super.getInformation();
			list.add("Delay: " + getDelay());
			return list;
		}	
	}
	
	//TODO The timer should really use the tick method instead of scheduled ticks.
	public static class PartTimer extends PartDelayedAction
	{
		@Override
		protected int getDelay() 
		{
			if((getState() & 32768) == 0) return ((getState() & 16711680) >> 16);
			else return 2;
		}

		@Override
		public void onPlaced()
		{
			setState(10 << 16);
			updateInput();
			if(!getInputFromSide(ForgeDirection.SOUTH)) setDelay(true);
		}

		@Override
		public void onClick(int button, boolean ctrl) 
		{
			//TODO Insert some gui here.
			super.onClick(button, ctrl);
		}
		
		@Override
		public void onInputChange(ForgeDirection side) 
		{
			updateInput();
			if(MiscUtils.rotn(side, -getRotation()) != ForgeDirection.SOUTH) return;
			setState(getState() & ~32768);
			if(getInputFromSide(side))
			{
				setDelay(false);
				notifyNeighbours();
			}
			else setDelay(true);
		}

		@Override
		public void onDelay() 
		{
			setState(getState() ^ 32768);
			setDelay(true);
			super.onDelay();
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			if(MiscUtils.rotn(side, -getRotation()) == ForgeDirection.SOUTH) return false;
			return (getState() & 32768) != 0;
		}		
	}
	
	public static class PartSequencer extends PartTimer
	{
		@Override
		public void onInputChange(ForgeDirection side)
		{
			updateInput();
		}

		@Override
		public void onDelay() 
		{
			if((getState() & 32768) == 0)
			{
				ForgeDirection fd = ForgeDirection.getOrientation(((getState() & 50331648) >> 24) + 2);
				fd = MiscUtils.rot(fd);
				setState(getState() & ~50331648 | (fd.ordinal() - 2) << 24); 
			}
			super.onDelay();
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			if(ForgeDirection.getOrientation(((getState() & 50331648) >> 24) + 2) == MiscUtils.rotn(side, -getRotation()))
				return (getState() & 32768) > 0;
			else return false;
		}
	}
	
	public static class PartStateCell extends PartDelayedAction
	{	
		private static int f1 = 1 << 23;
		private static int f2 = 1 << 24;
		
		@Override
		public void onClick(int button, boolean ctrl) 
		{
			//TODO Insert some gui here too.
			super.onClick(button, ctrl);
		}

		@Override
		protected int getDelay()
		{
			if((getState() & f2) > 0) return 2;
			return ((getState() & 8355840) >> 16);
		}
		
		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			if(s2 == ForgeDirection.WEST && (getState() & f1) > 0) return true;
			if(s2 == ForgeDirection.NORTH && (getState() & f2) > 0) return true;
			return false;
		}

		@Override
		public void onDelay() 
		{
			if((getState() & f2) > 0) setState(getState() & ~f2);
			else if((getState() & f1) > 0) 
			{
				setState(getState() & ~f1);
				setState(getState() | f2);
				setDelay(true);
			}
			super.onDelay();
		}

		@Override
		public void onPlaced()
		{
			setState(10 << 16);
		}

		@Override
		public void onInputChange(ForgeDirection side) 
		{
			updateInput();
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			if(s2 == ForgeDirection.SOUTH)
			{
				if(getInputFromSide(side))
				{
					setState(getState() | f1);
					setState(getState() & ~f2);
					notifyNeighbours();
				}
				else if(!getInputFromSide(MiscUtils.rotn(ForgeDirection.EAST, getRotation())))
					setDelay(true);
			}
			else if(s2 == ForgeDirection.EAST && (getState() & f1) > 0)
			{
				if(getInputFromSide(side)) setDelay(false);
				else if(!getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, getRotation())))
					setDelay(true);
				notifyNeighbours();
			}
		}	
	}

	public static class PartRandomizer extends PartDelayedAction
	{
		@Override
		protected int getDelay() 
		{
			return 2;
		}
		
		@Override
		public void onPlaced()
		{
			setDelay(true);
		}

		@Override
		public void onDelay() 
		{
			setState(getState() & ~229376);
			setState(getState() | new Random().nextInt(7) << 15);
			super.onDelay();
			setDelay(true);
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			if(!getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, getRotation()))) return false;
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			if(s2 == ForgeDirection.SOUTH) return false;
			int rand = (getState() & 229376) >> 15;
			if(s2 == ForgeDirection.EAST && (rand >> 2 & 1) == 1) return true;
			if(s2 == ForgeDirection.WEST && (rand >> 1 & 1) == 1) return true;
			if(s2 == ForgeDirection.NORTH && (rand & 1) == 1) return true;
			return false;
		}

		@Override
		public void onInputChange(ForgeDirection side) 
		{
			super.onInputChange(side);
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			if(s2 != ForgeDirection.SOUTH) return;
			if(!getInputFromSide(side)) setDelay(false);
		}
	}
	
	public static class PartPulseFormer extends PartDelayedAction
	{
		@Override
		public void onInputChange(ForgeDirection side) 
		{
			updateInput();
			if((MiscUtils.rotn(side, -getRotation()) != ForgeDirection.SOUTH)) return;
			if(getInputFromSide(side)) 
			{
				setState(getState() | 128);
				notifyNeighbours();
				setDelay(true);
			}
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection f2 = MiscUtils.rotn(side, -getRotation());
			if(f2 != ForgeDirection.NORTH) return false;
			return (getState() & 128) > 0;
		}

		@Override
		public boolean canConnectToSide(ForgeDirection side) 
		{
			ForgeDirection f2 = MiscUtils.rotn(side, -getRotation());
			return f2 == ForgeDirection.NORTH || f2 == ForgeDirection.SOUTH;
		}

		@Override
		public void onTick() 
		{
			super.onTick();
		}

		@Override
		protected int getDelay() 
		{
			return 2;
		}

		@Override
		public void onDelay() 
		{
			setState(getState() & ~128);
			super.onDelay();
		}
	}
	
	public static class PartToggleLatch extends PartGate
	{
		@Override
		public void onClick(int button, boolean ctrl) 
		{
			super.onClick(button, ctrl);
			if(button == 0 && ctrl) 
			{
				setState(getState() ^ 128);
				notifyNeighbours();
			}
		}

		@Override
		public void onInputChange(ForgeDirection side) 
		{
			super.onInputChange(side);
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			if((s2 == ForgeDirection.NORTH || s2 == ForgeDirection.SOUTH))
			{
				if(getInputFromSide(side)) setState(getState() ^ 128);
				scheduleTick();
			}
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			if(s2 == ForgeDirection.EAST) return (getState() & 128) > 0;
			if(s2 == ForgeDirection.WEST) return (getState() & 128) == 0;
			return false;
		}
	}
	
	//TODO Acts a little bit different then described on the P:R wiki. I'll come back to this.
	public static class PartRSLatch extends PartGate
	{	
		@Override
		public void onClick(int button, boolean ctrl) 
		{
			super.onClick(button, ctrl);
			if(button == 0 && ctrl)
			{
				int state = (getState() & 768) >> 8;
				state++;
				state = state > 3 ? 0 : state;
				setState(getState() & ~768);
				setState(getState() | state << 8);
				notifyNeighbours();
			}
		}
		
		private boolean isMirrored()
		{
			return (getState() & 512) > 0;
		}
		
		private boolean isSpecial()
		{
			return (getState() & 256) > 0;
		}

		@Override
		public void onInputChange(ForgeDirection side) 
		{
			updateInput();
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			if(s2 == ForgeDirection.EAST || s2 == ForgeDirection.WEST) return;
			if(s2 == ForgeDirection.NORTH) setState(getState() | 128);
			else setState(getState() & ~128);
			scheduleTick();
			markForUpdate();
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			ForgeDirection s3 = MiscUtils.rotn(ForgeDirection.NORTH, getRotation());
			boolean b1 = !(getInputFromSide(s3) && getInputFromSide(s3.getOpposite()));
			if(((s2 == ForgeDirection.EAST && !isMirrored() 
				|| s2 == ForgeDirection.WEST && isMirrored()) 
				|| (s2 == ForgeDirection.NORTH && isSpecial() 
				&& !getInputFromSide(s3.getOpposite()))) 
				&& b1 && (getState() & 128) != 0) return true;
			if(((s2 == ForgeDirection.WEST && !isMirrored() 
				|| s2 == ForgeDirection.EAST && isMirrored()) 
				|| (s2 == ForgeDirection.SOUTH && isSpecial() 
				&& !getInputFromSide(s3))) 
				&& b1 && (getState() & 128) == 0) return true;
			return false;
		}

		@Override
		public ArrayList<String> getInformation() 
		{
			ArrayList<String> text = super.getInformation();
			text.add("Mode: " + (isSpecial() ? 1 : 0));
			if(isMirrored()) text.add(EnumChatFormatting.ITALIC + "Mirrored");
			return text;
		}
	}
	
	public static class PartTranspartentLatch extends PartGate
	{
		@Override
		public void onInputChange(ForgeDirection side) 
		{
			super.onInputChange(side);
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			if(s2 == ForgeDirection.SOUTH || (s2 == ForgeDirection.WEST && getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, getRotation())))) 
			{
				if(getInputFromSide(MiscUtils.rotn(ForgeDirection.WEST, getRotation()))) setState(getState() | 128);
				else setState(getState() & ~128);
			}
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			if(s2 == ForgeDirection.NORTH || s2 == ForgeDirection.EAST) return (getState() & 128) > 0;
			return false;
		}
	}
	
	//TODO Insert the counter here, might get back to it at some point.
	
	//TODO Is currently giving a one tick pulse, might cause problems with other gates.
	public static class PartSynchronizer extends PartGate
	{
		@Override
		public void onInputChange(ForgeDirection side) 
		{
			updateInput();
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			boolean input = getInputFromSide(s2);
			
			if(s2 == ForgeDirection.SOUTH && getInputFromSide(s2)) setState(getState() & ~896);
			else if(s2 == ForgeDirection.EAST && !getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, getRotation())) && input) 
				setState(getState() | 128);
			else if(s2 == ForgeDirection.WEST && !getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, getRotation())) && input) 
				setState(getState() | 256);
			
			if((getState() & 384) >> 7 == 3) 
			{
				setState(getState() & ~384);
				setState(getState() | 512);
				scheduleTick();
			}
		}

		@Override
		public void onScheduledTick()
		{
			notifyNeighbours();
			if((getState() & 512) > 0)
			{
				setState(getState() & ~512);
				scheduleTick();
			}
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
			if(s2 == ForgeDirection.NORTH) return (getState() & 512) > 0;
			return false;
		}
	}
	
	public static class PartNullCell extends CircuitPart
	{
		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			return getInputFromSide(side.getOpposite()) && !getInputFromSide(side);
		}
		
		@Override
		public void onInputChange(ForgeDirection side) 
		{
			updateInput();
			notifyNeighbours();
		}
	}
	
	public static class PartInvertCell extends PartGate
	{
		@Override
		public void onInputChange(ForgeDirection side) 
		{
			updateInput();
			ForgeDirection fd = MiscUtils.rotn(side, -getRotation());
			if(fd == ForgeDirection.EAST || fd == ForgeDirection.WEST) scheduleTick();
			getNeighbourOnSide(side.getOpposite()).onInputChange(side);
			markForUpdate();
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection fd = MiscUtils.rotn(side, -getRotation());
			if(fd == ForgeDirection.EAST) return getInputFromSide(MiscUtils.rotn(ForgeDirection.WEST, getRotation()));
			else if(fd == ForgeDirection.WEST) return getInputFromSide(MiscUtils.rotn(ForgeDirection.EAST, getRotation()));
			if(fd == ForgeDirection.NORTH || fd == ForgeDirection.SOUTH) 
				if(!(getInputFromSide(MiscUtils.rotn(ForgeDirection.EAST, getRotation())) 
				|| getInputFromSide(MiscUtils.rotn(ForgeDirection.WEST, getRotation())))) return true;
				else if(fd == ForgeDirection.NORTH) return getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, getRotation()));
				else if(fd == ForgeDirection.SOUTH) return getInputFromSide(MiscUtils.rotn(ForgeDirection.NORTH, getRotation()));
			return false;
		}	
	}
	
	public static class PartBufferCell extends PartGate
	{
		@Override
		public void onInputChange(ForgeDirection side) 
		{
			updateInput();
			ForgeDirection fd = MiscUtils.rotn(side, -getRotation());
			if(fd == ForgeDirection.EAST || fd == ForgeDirection.WEST) scheduleTick();
			getNeighbourOnSide(side.getOpposite()).onInputChange(side);
			markForUpdate();
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection fd = MiscUtils.rotn(side, -getRotation());
			if(fd == ForgeDirection.EAST) return getInputFromSide(MiscUtils.rotn(ForgeDirection.WEST, getRotation()));
			else if(fd == ForgeDirection.WEST) return getInputFromSide(MiscUtils.rotn(ForgeDirection.EAST, getRotation()));
			if(fd == ForgeDirection.NORTH || fd == ForgeDirection.SOUTH) 
				if(getInputFromSide(MiscUtils.rotn(ForgeDirection.EAST, getRotation())) 
				|| getInputFromSide(MiscUtils.rotn(ForgeDirection.WEST, getRotation()))) return true;
				else if(fd == ForgeDirection.NORTH) return getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, getRotation()));
				else if(fd == ForgeDirection.SOUTH) return getInputFromSide(MiscUtils.rotn(ForgeDirection.NORTH, getRotation()));
			return false;
		}
	}
	
	public static class PartANDCell extends PartGate
	{
		@Override
		public void onInputChange(ForgeDirection side) 
		{
			updateInput();
			ForgeDirection fd = MiscUtils.rotn(side, -getRotation());
			if(fd == ForgeDirection.EAST) scheduleTick();
			else if(fd != ForgeDirection.WEST) 
			{
				getNeighbourOnSide(side.getOpposite()).onInputChange(side);
				ForgeDirection fd2 = MiscUtils.rotn(ForgeDirection.WEST, getRotation());
				getNeighbourOnSide(fd2).onInputChange(fd2.getOpposite());
			}		
			markForUpdate();
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection fd = MiscUtils.rotn(side, -getRotation());
			if(fd == ForgeDirection.NORTH || fd == ForgeDirection.SOUTH)
				return getInputFromSide(side.getOpposite());
			else if(fd == ForgeDirection.WEST) return getInputFromSide(side.getOpposite()) 
				&& (getInputFromSide(MiscUtils.rotn(ForgeDirection.NORTH, getRotation()))
				|| getInputFromSide(MiscUtils.rotn(ForgeDirection.SOUTH, getRotation())));
			else return false;
		}
	}
}