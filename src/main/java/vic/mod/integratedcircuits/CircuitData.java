package vic.mod.integratedcircuits;

import java.awt.Point;
import java.util.LinkedList;

import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.ic.CircuitPart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

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
		//TODO Insertion of the IO bit parts on the sides. Should refresh the adjacent neighbors.
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
	
	public ICircuit getParent()
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
	}
	
	public CircuitPart getPart(int x, int y)
	{
		try {
			return CircuitPart.getPart(id[x][y]).getConstructor(int.class, int.class, ICircuit.class).newInstance(x, y, parent);
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
