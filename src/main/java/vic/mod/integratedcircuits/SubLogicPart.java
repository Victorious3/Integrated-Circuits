package vic.mod.integratedcircuits;

import java.util.HashMap;
import java.util.Random;

import net.minecraftforge.common.util.ForgeDirection;

public abstract class SubLogicPart
{
	private static HashMap<Integer, Class<? extends SubLogicPart>> partRegistry = new HashMap<Integer, Class<? extends SubLogicPart>>();
	
	static 
	{
		partRegistry.put(1, PartWire.class);
		partRegistry.put(2, PartTorch.class);
		partRegistry.put(3, PartANDGate.class);
		partRegistry.put(4, PartORGate.class);
		partRegistry.put(5, PartNANDGate.class);
		partRegistry.put(6, PartNORGate.class);
		partRegistry.put(7, PartBufferGate.class);
		partRegistry.put(8, PartNOTGate.class);
		partRegistry.put(9, PartMultiplexer.class);
		partRegistry.put(10, PartRepeater.class);
		partRegistry.put(11, PartTimer.class);
		partRegistry.put(12, PartSequencer.class);
		partRegistry.put(13, PartStateCell.class);
		partRegistry.put(14, PartRandomizer.class);
		partRegistry.put(15, PartPulseFormer.class);
		partRegistry.put(16, PartRSLatch.class);
		partRegistry.put(17, PartToggleLatch.class);
		partRegistry.put(18, PartTranspartentLatch.class);
	}

	public static SubLogicPart getPart(int x, int y, ICircuit parent)
	{
		if(parent.getMatrix()[0][x][y] == 0) return new PartNull(x, y, parent);
		try {
			return partRegistry.get(parent.getMatrix()[0][x][y]).getConstructor(int.class, int.class, ICircuit.class).newInstance(x, y, parent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public SubLogicPart(int x, int y, ICircuit parent)
	{
		this.x = x;
		this.y = y;
		this.parent = parent;
	}
	
	private final int x;
	private final int y;
	private final ICircuit parent;
	
	public void onPlaced()
	{
		notifyNeighbours();
	}
	
	public void onUpdateTick(){}
	
	public void onClick(int button, boolean shift){}
	
	public final int getX()
	{
		return x;
	}
	
	public final int getY()
	{
		return y;
	}
	
	public final ICircuit getParent()
	{
		return parent;
	}
	
	public final int getState()
	{
		return parent.getMatrix()[1][getX()][getY()];
	}
	
	public final void setState(int state)
	{
		parent.getMatrix()[1][getX()][getY()] = state;
	}
	
	public boolean canConnectToSide(ForgeDirection side)
	{
		return true;
	}
	
	public final boolean getInputFromSide(ForgeDirection side)
	{
		if(!(canConnectToSide(side) && getNeighbourOnSide(side).canConnectToSide(side.getOpposite()))) return false;
		boolean in = ((getState() & 15) << (side.ordinal() - 2) & 8) > 0;
		return in;
	}
	
	public void onInputChange(ForgeDirection side)
	{
		int newState = 0;
		//Check every side to update the internal buffer.
		newState |= (getNeighbourOnSide(ForgeDirection.NORTH).getOutputToSide(ForgeDirection.NORTH.getOpposite()) ? 1 : 0) << 3;
		newState |= (getNeighbourOnSide(ForgeDirection.SOUTH).getOutputToSide(ForgeDirection.SOUTH.getOpposite()) ? 1 : 0) << 2;
		newState |= (getNeighbourOnSide(ForgeDirection.WEST).getOutputToSide(ForgeDirection.WEST.getOpposite()) ? 1 : 0) << 1;
		newState |= (getNeighbourOnSide(ForgeDirection.EAST).getOutputToSide(ForgeDirection.EAST.getOpposite()) ? 1 : 0);
		setState(getState() & ~15 | newState);
	}
	
	public boolean getOutputToSide(ForgeDirection side)
	{
		return false;
	}
	
	public void notifyNeighbours()
	{
		for(int i = 2; i < 6; i++)
		{
			ForgeDirection fd = ForgeDirection.getOrientation(i);
			SubLogicPart part = getNeighbourOnSide(fd);
			if(canConnectToSide(fd) 
				&& part.canConnectToSide(fd.getOpposite()) 
				&& getOutputToSide(fd) != part.getInputFromSide(fd.getOpposite())) 
				part.onInputChange(fd.getOpposite());
		}
	}
	
	public final SubLogicPart getNeighbourOnSide(ForgeDirection side)
	{	
		//TODO
		return getPart(x + side.offsetX, y + side.offsetZ, parent);
	}
	
	public final boolean getInput()
	{
		return getInputFromSide(ForgeDirection.NORTH)
			|| getInputFromSide(ForgeDirection.EAST)
			|| getInputFromSide(ForgeDirection.SOUTH)
			|| getInputFromSide(ForgeDirection.WEST);
	}
	
	public static class PartNull extends SubLogicPart
	{
		public PartNull(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}

		@Override
		public void onInputChange(ForgeDirection side) {}

		@Override
		public boolean canConnectToSide(ForgeDirection side) 
		{
			return false;
		}
	}
	
	public static class PartWire extends SubLogicPart
	{
		public PartWire(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}

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
	}
	
	public static class PartTorch extends SubLogicPart
	{
		public PartTorch(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			return true;
		}
	}
	
	public static abstract class PartGate extends SubLogicPart
	{
		private boolean updateLater = false;
		
		public PartGate(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}
		
		public final int getRotation()
		{
			return (getState() & 48) >> 4;
		}
		
		public final void setRotation(int rotation)
		{
			setState(getState() & ~48 | rotation << 4);
			notifyNeighbours();
		}
		
		protected final boolean getUpdateFlag()
		{
			return (getState() & 64) > 0; 
		}
		
		protected void setUpdate(boolean update)
		{
			if(update) setState(getState() | 64);
			else setState(getState() & ~64);
		}
		
		@Override
		public void onClick(int button, boolean shift) 
		{
			if(button == 1)
			{
				int rot = getRotation() + 1;
				setRotation(rot > 3 ? 0 : rot);
			}
		}

		@Override
		public void onUpdateTick() 
		{
			if(getUpdateFlag())
			{
				setUpdate(false);
				notifyNeighbours();
			}
		}
		
		@Override
		public void onInputChange(ForgeDirection side) 
		{
			super.onInputChange(side);
			setUpdate(true);
		}
	}
	
	public static class Part3I1O extends PartGate
	{
		public Part3I1O(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}
		
		@Override
		public void onClick(int button, boolean shift) 
		{
			super.onClick(button, shift);
			if(button == 1 && shift)
			{
				int i1 = (getState() & 384) >> 7;
				i1 = i1 + 1 > 3 ? 0 : i1 + 1;
				setState(getState() & ~384 | i1 << 7);
			}
		}
		
		@Override
		public boolean canConnectToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = Misc.rotn(side, getRotation());
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
			ForgeDirection s2 = Misc.rotn(side, getRotation());		
			super.onInputChange(side);
			if(s2 == ForgeDirection.NORTH) setUpdate(false);
		}
	}
	
	public static class Part1I3O extends PartGate
	{
		public Part1I3O(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}
		
		@Override
		public void onClick(int button, boolean shift) 
		{
			super.onClick(button, shift);
			if(button == 1 && shift)
			{
				int i1 = (getState() & 896) >> 7;
				i1 = i1 + 1 > 5 ? 0 : i1 + 1;
				setState(getState() & ~896 | i1 << 7);
			}
		}

		@Override
		public void onInputChange(ForgeDirection side) 
		{
			ForgeDirection s2 = Misc.rotn(side, getRotation());
			super.onInputChange(side);
			if(s2 != ForgeDirection.NORTH) setUpdate(false);
		}
		
		@Override
		public boolean canConnectToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = Misc.rotn(side, getRotation());
			if(s2 == ForgeDirection.NORTH) return true;
			int i = (getState() & 896) >> 7;
			//TODO I bet there would be a better solution for this.
			if(s2 == ForgeDirection.EAST && (i == 3 || i == 4 || i == 5)) return false;
			if(s2 == ForgeDirection.SOUTH && (i == 2 || i == 4 || i == 6)) return false;
			if(s2 == ForgeDirection.WEST && (i == 1 || i == 5 || i == 6)) return false;
			return true;
		}
	}
	
	public static class PartANDGate extends Part3I1O
	{
		public PartANDGate(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = Misc.rotn(side, getRotation());
			ForgeDirection s3 = Misc.rotn(ForgeDirection.SOUTH, -getRotation());
			ForgeDirection s4 = Misc.rotn(ForgeDirection.EAST, -getRotation());
			ForgeDirection s5 = Misc.rotn(ForgeDirection.WEST, -getRotation());
			return s2 == ForgeDirection.NORTH 
				&& (!canConnectToSide(s3) || getInputFromSide(s3))
				&& (!canConnectToSide(s4) || getInputFromSide(s4))
				&& (!canConnectToSide(s5) || getInputFromSide(s5));
		}
	}
	
	public static class PartORGate extends Part3I1O
	{
		public PartORGate(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = Misc.rotn(side, getRotation());
			ForgeDirection s3 = Misc.rotn(ForgeDirection.SOUTH, -getRotation());
			ForgeDirection s4 = Misc.rotn(ForgeDirection.EAST, -getRotation());
			ForgeDirection s5 = Misc.rotn(ForgeDirection.WEST, -getRotation());
			return s2 == ForgeDirection.NORTH &&
				((!canConnectToSide(s3) || getInputFromSide(s3))
				|| (!canConnectToSide(s4) || getInputFromSide(s4))
				|| (!canConnectToSide(s5) || getInputFromSide(s5)));
		}
	}
	
	public static class PartNORGate extends PartORGate
	{
		public PartNORGate(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			return !super.getOutputToSide(side);
		}
	}
	
	public static class PartNANDGate extends PartANDGate
	{
		public PartNANDGate(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			return !super.getOutputToSide(side);
		}
	}
	
	public static class PartBufferGate extends Part1I3O
	{
		public PartBufferGate(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = Misc.rotn(side, getRotation());
			if(s2 != ForgeDirection.NORTH) return getInputFromSide(Misc.rotn(ForgeDirection.NORTH, -getRotation()));
			return false;
		}
	}
	
	public static class PartNOTGate extends Part1I3O
	{
		public PartNOTGate(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}
		
		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = Misc.rotn(side, getRotation());
			if(s2 != ForgeDirection.NORTH) return !getInputFromSide(Misc.rotn(ForgeDirection.NORTH, -getRotation()));
			return false;
		}
	}

	public static class PartXORGate extends PartGate
	{
		public PartXORGate(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}

		@Override
		public boolean canConnectToSide(ForgeDirection side) 
		{
			return Misc.rotn(side, getRotation()) != ForgeDirection.SOUTH;
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection fd = Misc.rotn(side, getRotation());
			return fd == ForgeDirection.NORTH &&
				(getInputFromSide(Misc.rotn(ForgeDirection.EAST, -getRotation()))
				!= getInputFromSide(Misc.rotn(ForgeDirection.WEST, -getRotation())));
		}	
	}
	
	public static class PartXNORGate extends PartXORGate
	{
		public PartXNORGate(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			return !super.getOutputToSide(side);
		}
	}
	
	public static class PartMultiplexer extends PartGate
	{
		public PartMultiplexer(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}

		@Override
		public void onInputChange(ForgeDirection side) 
		{
			super.onInputChange(side);
			if(Misc.rotn(side, getRotation()) == ForgeDirection.NORTH) setUpdate(false);
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			if(Misc.rotn(side, getRotation()) != ForgeDirection.NORTH) return false;
			if(getInputFromSide(Misc.rotn(ForgeDirection.SOUTH, -getRotation())))
				return getInputFromSide(Misc.rotn(ForgeDirection.EAST, -getRotation()));
			else return getInputFromSide(Misc.rotn(ForgeDirection.WEST, -getRotation()));
		}	
	}

	/** Uses 8 bits for the delay. 255 ticks = 12.75 seconds*/
	public static abstract class PartDelayedAction extends PartGate
	{
		public PartDelayedAction(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}
		
		protected abstract int getDelay();
		
		public int getCurrentDelay()
		{
			return ((getState() & 32640) >> 7);
		}
		
		@Override
		public void onUpdateTick() 
		{
			if(getUpdateFlag())
			{
				int counter = getCurrentDelay();
				counter--;
				if(counter == 0)
				{
					setUpdate(false);
					setState(getState() & ~32640);
					onDelay();
				}
				else setState(getState() & ~32640 | counter << 7);
			}
		}
		
		public void onDelay()
		{
			notifyNeighbours();
		}

		@Override
		protected void setUpdate(boolean update) 
		{
			setState(getState() & ~32640 | getDelay() << 7);
			super.setUpdate(update);
		}
	}
	
	public static class PartRepeater extends PartDelayedAction
	{
		public PartRepeater(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}

		@Override
		protected int getDelay() 
		{
			return ((getState() & 16711680) >> 16);
		}
		
		@Override
		public void onPlaced()
		{
			setState(2 << 16);
		}

		@Override
		public void onClick(int button, boolean shift) 
		{
			super.onClick(button, shift);
			if(button == 1 && shift)
			{
				int delay = getDelay();
				int newDelay = 0;
				switch(delay)
				{
				case 2 : delay = 4; break;
				case 4 : delay = 8; break;
				case 8 : delay = 16; break;
				case 16 : delay = 32; break;
				case 32 : delay = 64; break;
				case 64 : delay = 128; break;
				case 128 : delay = 255; break;
				case 255 : delay = 0; break;
				}
				setState((getState() & 16711680) | delay);
			}
		}

		@Override
		public boolean canConnectToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = Misc.rotn(side, getRotation());
			return s2 == ForgeDirection.NORTH || s2 == ForgeDirection.SOUTH;
		}
		
		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = Misc.rotn(side, getRotation());
			if(s2 != ForgeDirection.SOUTH) return false;
			return getCurrentDelay() > 0 ? (getState() & 32768) > 0 : (getState() & 32768) == 0;
		}

		@Override
		public void onDelay() 
		{	
			setState(getState() ^ 32768);
			super.onDelay();
		}
		
		@Override
		public void onUpdateTick() 
		{
			int state = getState();
			super.onUpdateTick();
		}

		@Override
		public void onInputChange(ForgeDirection side) 
		{
			if(Misc.rotn(side, getRotation()) != ForgeDirection.NORTH) return;
			if(getCurrentDelay() != 0) super.onInputChange(side);
			else
			{
				super.onInputChange(side);
				if(((getState() & 32768) >> 15) != (getInputFromSide(side) ? 1 : 0))
				{
					setState(getState() & ~32768 | (getInputFromSide(side) ? 1 : 0) << 15);
					setUpdate(true);
				}
			}
		}
	}
	
	public static class PartTimer extends PartDelayedAction
	{
		public PartTimer(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}
		
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
			setUpdate(true);
		}

		@Override
		public void onClick(int button, boolean shift) 
		{
			//TODO Insert some gui here.
			super.onClick(button, shift);
		}
		
		@Override
		public void onInputChange(ForgeDirection side) 
		{
			if(Misc.rotn(side, getRotation()) == ForgeDirection.SOUTH) return;
			super.onInputChange(side);
			if(getInputFromSide(side))
			{
				setState(getState() & ~32768);
				setUpdate(false);
				notifyNeighbours();
			}
			else setUpdate(true);
		}

		@Override
		public void onDelay() 
		{
			setState(getState() ^ 32768);
			setUpdate(true);
			super.onDelay();
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			if(Misc.rotn(side, getRotation()) == ForgeDirection.NORTH) return false;
			return (getState() & 32768) > 0;
		}		
	}
	
	public static class PartSequencer extends PartTimer
	{
		public PartSequencer(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}
		
		@Override
		public void onInputChange(ForgeDirection side)
		{
			//The Sequencer doesn't care about input.
		}

		@Override
		public void onDelay() 
		{
			if((getState() & 32768) == 0)
			{
				ForgeDirection fd = ForgeDirection.getOrientation(((getState() & 50331648) >> 24) + 2);
				fd = Misc.rot(fd);
				setState(getState() & ~50331648 | (fd.ordinal() - 2) << 24); 
			}
			super.onDelay();
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			if(ForgeDirection.getOrientation(((getState() & 50331648) >> 24) + 2) == Misc.rotn(side, getRotation()))
				return (getState() & 32768) > 0;
			else return false;
		}
	}
	
	public static class PartStateCell extends PartDelayedAction
	{
		public PartStateCell(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}
		
		private static int f1 = 1 << 23;
		private static int f2 = 1 << 24;
		
		@Override
		public void onClick(int button, boolean shift) 
		{
			//TODO Insert some gui here too.
			super.onClick(button, shift);
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
			ForgeDirection s2 = Misc.rotn(side, getRotation());
			if(s2 == ForgeDirection.NORTH && (getState() & f1) > 0) return true;
			if(s2 == ForgeDirection.EAST && (getState() & f2) > 0) return true;
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
				setUpdate(true);
			}
			super.onDelay();
		}

		@Override
		public void onPlaced()
		{
			setState(10 << 15);
		}

		@Override
		public void onInputChange(ForgeDirection side) 
		{
			ForgeDirection s2 = Misc.rotn(side, getRotation());
			if(s2 == ForgeDirection.WEST)
			{
				super.onInputChange(side);
				if(getInputFromSide(side))
				{
					setState(getState() | f1);
					setState(getState() & ~f2);
					setUpdate(false);
				}
				else setUpdate(true);
			}
			else if(s2 == ForgeDirection.SOUTH && (getState() & f1) > 0) setUpdate(true);
		}	
	}

	//TODO Untested
	public static class PartRandomizer extends PartDelayedAction
	{
		public PartRandomizer(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}

		@Override
		protected int getDelay() 
		{
			return 2;
		}
		
		@Override
		public void onPlaced()
		{
			setUpdate(true);
		}

		@Override
		public void onDelay() 
		{
			setState(getState() & ~229376);
			setState(getState() | new Random().nextInt(2));
			super.onDelay();
			setUpdate(true);
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			if(getInputFromSide(Misc.rotn(ForgeDirection.NORTH, -getRotation()))) return false;
			ForgeDirection s2 = Misc.rotn(side, getRotation());
			if(s2 == ForgeDirection.NORTH) return false;
			int rand = (getState() & 229376) >> 15;
			if(s2 == ForgeDirection.EAST && (rand >> 2 & 1) == 1) return true;
			if(s2 == ForgeDirection.WEST && (rand >> 1 & 1) == 1) return true;
			if(s2 == ForgeDirection.SOUTH && (rand & 1) == 1) return true;
			return false;
		}

		@Override
		public void onInputChange(ForgeDirection side) 
		{
			super.onInputChange(side);
			ForgeDirection s2 = Misc.rotn(side, getRotation());
			if(s2 != ForgeDirection.NORTH) return;
			if(!getInputFromSide(side)) setUpdate(false);
		}
	}
	
	public static class PartPulseFormer extends PartGate
	{
		public PartPulseFormer(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}

		@Override
		public void onInputChange(ForgeDirection side) 
		{
			super.onInputChange(side);
			if((Misc.rotn(side, getRotation()) != ForgeDirection.NORTH) || !getInputFromSide(side)) setUpdate(false);
			notifyNeighbours();
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection f2 = Misc.rotn(side, getRotation());
			if(f2 != ForgeDirection.SOUTH) return false;
			return getUpdateFlag();
		}	
	}
	
	public static class PartToggleLatch extends PartGate
	{
		public PartToggleLatch(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}

		@Override
		public void onClick(int button, boolean shift) 
		{
			super.onClick(button, shift);
			if(button == 1 && shift) setState(getState() ^ 128); 
		}

		@Override
		public void onInputChange(ForgeDirection side) 
		{
			super.onInputChange(side);
			ForgeDirection s2 = Misc.rotn(side, getRotation());
			if((s2 == ForgeDirection.NORTH || s2 == ForgeDirection.SOUTH))
			{
				if(getInputFromSide(side)) setState(getState() ^ 128);
			}
			else setUpdate(false);
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = Misc.rotn(side, getRotation());
			if(s2 == ForgeDirection.EAST) return (getState() & 128) > 0;
			if(s2 == ForgeDirection.WEST) return (getState() & 128) == 0;
			return false;
		}
	}
	
	//TODO Untested
	public static class PartRSLatch extends PartGate
	{
		public PartRSLatch(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}
		
		@Override
		public void onClick(int button, boolean shift) 
		{
			super.onClick(button, shift);
			if(button == 1 && shift)
			{
				int state = (getState() & 768) >> 8;
				state = state++ > 3 ? 0 : state++;
				setState(getState() | state << 8);
			}
		}

		@Override
		public void onInputChange(ForgeDirection side) 
		{
			super.onInputChange(side);
			ForgeDirection s2 = Misc.rotn(side, getRotation());
			if(!(s2 == ForgeDirection.NORTH || s2 == ForgeDirection.SOUTH)) 
			{
				setUpdate(false);
				return;
			}
			if(s2 == ForgeDirection.NORTH || (getState() & 512) > 0) setState(getState() | 128);
			else setState(getState() & ~128);
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = Misc.rotn(side, getRotation());
			if((getState() & 512) > 0) s2 = Misc.rotn(s2, 2);
			if((s2 == ForgeDirection.EAST || (s2 == ForgeDirection.NORTH && (getState() & 256) > 0)) && (getState() & 128) > 0) return true;
			if((s2 == ForgeDirection.WEST || (s2 == ForgeDirection.SOUTH && (getState() & 256) > 0)) && (getState() & 128) == 0) return true;
			return false;
		}
	}
	
	//TODO Untested
	public static class PartTranspartentLatch extends PartGate
	{
		public PartTranspartentLatch(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}

		@Override
		public void onInputChange(ForgeDirection side) 
		{
			super.onInputChange(side);
			ForgeDirection s2 = Misc.rotn(side, getRotation());
			if(s2 == ForgeDirection.SOUTH || (s2 == ForgeDirection.WEST && getInputFromSide(Misc.rotn(ForgeDirection.SOUTH, -getRotation())))) 
			{
				if(getInputFromSide(Misc.rotn(ForgeDirection.WEST, -getRotation()))) setState(getState() | 128);
				else setState(getState() & ~128);
			}
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = Misc.rotn(side, getRotation());
			if(s2 == ForgeDirection.NORTH || s2 == ForgeDirection.SOUTH) return (getState() & 128) > 0;
			return false;
		}
	}
	
	//TODO Insert the counter here, might get back to it at some point.
	
	//TODO Untested
	public static class PartSynchronizer extends PartGate
	{
		public PartSynchronizer(int x, int y, ICircuit parent) 
		{
			super(x, y, parent);
		}

		@Override
		public void onInputChange(ForgeDirection side) 
		{
			super.onInputChange(side);
			ForgeDirection s2 = Misc.rotn(side, getRotation());
			
			if(s2 == ForgeDirection.SOUTH && getInputFromSide(s2)) setState(getState() & ~896);
			else if(s2 == ForgeDirection.EAST && !getInputFromSide(Misc.rotn(ForgeDirection.SOUTH, -getRotation()))) setState(getState() | 128);
			else if(s2 == ForgeDirection.WEST && !getInputFromSide(Misc.rotn(ForgeDirection.SOUTH, -getRotation()))) setState(getState() | 256);
			
			if((getState() & 384) >> 7 == 3) 
			{
				setState(getState() & ~384);
				setState(getState() | 512);
			}
		}

		@Override
		public void onUpdateTick()
		{
			if(getUpdateFlag())
			{
				notifyNeighbours();
				if((getState() & 512) > 0)
				{
					setState(getState() & ~512);
					setUpdate(true);
				}
				else setUpdate(false);
			}
		}

		@Override
		public boolean getOutputToSide(ForgeDirection side) 
		{
			ForgeDirection s2 = Misc.rotn(side, getRotation());
			if(s2 == ForgeDirection.NORTH) return (getState() & 512) > 0;
			return false;
		}
	}
}
