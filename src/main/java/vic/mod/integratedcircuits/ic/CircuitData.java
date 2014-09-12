package vic.mod.integratedcircuits.ic;

import java.awt.Point;
import java.util.LinkedList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import vic.mod.integratedcircuits.ic.CircuitPart.PartIOBit;
import vic.mod.integratedcircuits.ic.CircuitPart.PartNull;

import com.google.common.primitives.Ints;

public class CircuitData implements Cloneable
{
	private int size;
	private int[][] id;
	private int[][] meta;
	private LinkedList<Point> tickSchedule;
	private ICircuit parent;
	
	private CircuitData(){};
	
	public CircuitData(int size, ICircuit parent)
	{
		this.parent = parent;
		this.size = size;
		clear(size);
	}
	
	private CircuitData(int size, ICircuit parent, int[][] id, int[][] meta, LinkedList<Point> tickSchedule)
	{
		this.parent = parent;
		this.size = size;
		this.id = id;
		this.meta = meta;
		this.tickSchedule = tickSchedule;
	}
	
	public void setup()
	{
		int o = size / 2 - 8;
		int cid = CircuitPart.getId(CircuitPart.PartIOBit.class);
		
		for(int i = 0; i < 16; i++)
		{
			setID(i + o, 0, cid);
			setID(size - 1, i + o, cid);
			setID(i + o, size - 1, cid);
			setID(0, i + o, cid);
			
			PartIOBit io1 = (PartIOBit)getPart(i + o, 0);
			PartIOBit io2 = (PartIOBit)getPart(size - 1, i + o);
			PartIOBit io3 = (PartIOBit)getPart(i + o, size - 1);
			PartIOBit io4 = (PartIOBit)getPart(0, i + o);
			
			io1.setFrequency(i);
			io2.setFrequency(i);
			io3.setFrequency(i);
			io4.setFrequency(i);
			
			io1.setRotation(0);
			io2.setRotation(1);
			io3.setRotation(2);
			io4.setRotation(3);
		}
	}
	
	/** Syncs the circuit's IO bits with the suspected input **/
	public void updateInput()
	{
		int o = size / 2 - 8;
		
		for(int i = 0; i < 16; i++)
		{		
			PartIOBit io1 = (PartIOBit)getPart(i + o, 0);
			PartIOBit io2 = (PartIOBit)getPart(size - 1, i + o);
			PartIOBit io3 = (PartIOBit)getPart(i + o, size - 1);
			PartIOBit io4 = (PartIOBit)getPart(0, i + o);
			
			io1.notifyNeighbours();
			io2.notifyNeighbours();
			io3.notifyNeighbours();
			io4.notifyNeighbours();
		}
	}
	
	public int getMeta(int x, int y)
	{
		return meta[x][y];
	}
	
	public void setMeta(int x, int y, int m)
	{
		meta[x][y] = m;
	}
	
	public int getID(int x, int y) 
	{
		return id[x][y];
	}
	
	public void setID(int x, int y, int i)
	{
		id[x][y] = i;
	}
	
	public ICircuit getCircuit()
	{
		return parent;
	}
	
	public void setParent(ICircuit parent)
	{
		this.parent = parent;
	}
	
	public int getSize()
	{
		return size;
	}
	
	public void clear(int size)
	{
		this.id = new int[size][size];
		this.meta = new int[size][size];
		tickSchedule = new LinkedList<Point>();
		this.size = size;
		setup();
	}
	
	public CircuitPart getPart(int x, int y)
	{
		if(x < 0 || y < 0 || x >= size || y >= size) return new PartNull(x, y, this);
		try {
			return CircuitPart.getPart(id[x][y]).getConstructor(int.class, int.class, CircuitData.class).newInstance(x, y, this);
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return null;
	}
	
	public void scheduleTick(int x, int y)
	{
		Point p = new Point(x, y);
		if(!tickSchedule.contains(p)) tickSchedule.add(p);
	}
	
	public void updateMatrix()
	{
		LinkedList<Point> tmp = (LinkedList<Point>)tickSchedule.clone();
		tickSchedule.clear();
		for(Point p : tmp)
		{
			getPart(p.x, p.y).onScheduledTick();
		}
		for(int x = 0; x < size; x++)
		{
			for(int y = 0; y < size; y++)
			{
				getPart(x, y).onTick();
			}
		}
	}
	
	public static CircuitData readFromNBT(NBTTagCompound compound)
	{
		return readFromNBT(compound, null);
	}
	
	public static CircuitData readFromNBT(NBTTagCompound compound, ICircuit parent)
	{
		NBTTagList idlist = compound.getTagList("id", NBT.TAG_INT_ARRAY);
		int[][] id = new int[idlist.tagCount()][];
		for(int i = 0; i < idlist.tagCount(); i++)
		{
			id[i] = idlist.func_150306_c(i);
		}
		
		NBTTagList metalist = compound.getTagList("meta", NBT.TAG_INT_ARRAY);
		int[][] meta = new int[metalist.tagCount()][];
		for(int i = 0; i < metalist.tagCount(); i++)
		{
			meta[i] = metalist.func_150306_c(i);
		}
		
		int size = compound.getInteger("size");
		LinkedList<Point> scheduledTicks = new LinkedList<Point>();
		
		if(compound.hasKey("scheduled"))
		{
			int[] scheduledList = compound.getIntArray("scheduled");
			for(int i = 0; i < scheduledList.length; i += 2)
			{
				scheduledTicks.add(new Point(scheduledList[i], scheduledList[i + 1]));
			}
		}
		
		return new CircuitData(size, parent, id, meta, scheduledTicks);
	}
	
	/** Used by the PCB to exclude the unused tick schedule for the client **/
	public NBTTagCompound writeToNBTRaw(NBTTagCompound compound)
	{
		NBTTagList idlist = new NBTTagList();
		for(int i = 0; i < size; i++)
		{
			idlist.appendTag(new NBTTagIntArray(id[i]));
		}
		
		NBTTagList metalist = new NBTTagList();
		for(int i = 0; i < size; i++)
		{
			metalist.appendTag(new NBTTagIntArray(meta[i]));
		}
		
		compound.setInteger("size", size);
		compound.setTag("id", idlist);
		compound.setTag("meta", metalist);
		
		return compound;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{			
		compound = writeToNBTRaw(compound);
		LinkedList<Integer> tmp = new LinkedList<Integer>();
		for(Point p : tickSchedule)
		{
			tmp.add(p.x);
			tmp.add(p.y);
		}
		compound.setIntArray("scheduled", Ints.toArray(tmp));		
		
		return compound;
	}
	
	public static CircuitData createShallowInstance(int state, ICircuit parent)
	{
		CircuitData data = new CircuitData();
		data.size = 3;
		data.id = new int[][]{new int[]{0, 1, 0}, new int[]{1, 0, 1}, new int[]{0, 1, 0}};
		data.meta = new int[][]{new int[]{0, 0, 0}, new int[]{0, state, 0}, new int[]{0, 0, 0}};
		data.parent = parent;
		return data;
	}
}
