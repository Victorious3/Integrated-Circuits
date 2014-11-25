package vic.mod.integratedcircuits.ic;

import io.netty.buffer.ByteBuf;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.part.PartIOBit;
import vic.mod.integratedcircuits.ic.part.PartNull;
import vic.mod.integratedcircuits.misc.CraftingAmount;
import vic.mod.integratedcircuits.misc.Vec2;

import com.google.common.primitives.Ints;

public class CircuitData implements Cloneable
{
	private int size;
	private int[][] meta;
	private CircuitPart[][] parts;
	private LinkedHashSet<Vec2> tickSchedule;
	private LinkedHashSet<Vec2> updateQueue = new LinkedHashSet<Vec2>();
	private CraftingAmount cost;
	private ICircuit parent;
	private boolean queueEnabled = true;
	private CircuitProperties prop = new CircuitProperties();
	
	private CircuitData(){};
	
	public CircuitData(int size, ICircuit parent)
	{
		this.parent = parent;
		this.size = size;
		clear(size);
	}
	
	private CircuitData(int size, ICircuit parent, int[][] id, int[][] meta, LinkedHashSet<Vec2> tickSchedule, CircuitProperties prop)
	{
		this.parent = parent;
		this.prop = prop;
		this.size = size;
		this.parts = new CircuitPart[size][size];
		for(int x = 0; x < size; x++)
		{
			for(int y = 0; y < size; y++)
			{
				parts[x][y] = CircuitPart.getPart(id[x][y], x, y, this);
			}
		}
		this.meta = meta;
		this.tickSchedule = tickSchedule;
	}
	
	public void setup()
	{
		int o = supportsBundled() ? size / 2 - 8 : 1;
		int cid = CircuitPart.getIdFromClass(PartIOBit.class);
		
		for(int i = 0; i < (supportsBundled() ? 16 : 1); i++)
		{
			setID(size - 1 - (i + o), 0, cid);
			setID(size - 1, size - 1 - (i + o), cid);
			setID(i + o, size - 1, cid);
			setID(0, i + o, cid);
			
			PartIOBit io1 = (PartIOBit)getPart(size - 1 - (i + o), 0);
			PartIOBit io2 = (PartIOBit)getPart(size - 1, size - 1 - (i + o));
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
		int o = supportsBundled() ? size / 2 - 8 : 1;
		
		for(int i = 0; i < (supportsBundled() ? 16 : 1); i++)
		{	
			PartIOBit io1 = (PartIOBit)getPart(size - 1 - (i + o), 0);
			PartIOBit io2 = (PartIOBit)getPart(size - 1, size - 1 - (i + o));
			PartIOBit io3 = (PartIOBit)getPart(i + o, size - 1);
			PartIOBit io4 = (PartIOBit)getPart(0, i + o);
			
			io1.notifyNeighbours();
			io2.notifyNeighbours();
			io3.notifyNeighbours();
			io4.notifyNeighbours();
		}
	}
	
	/** Syncs the circuit's IO bits with the suspected output **/
	public void updateOutput()
	{
		int o = supportsBundled() ? size / 2 - 8 : 1;
		
		for(int i = 0; i < (supportsBundled() ? 16 : 1); i++)
		{		
			PartIOBit io1 = (PartIOBit)getPart(size - 1 - (i + o), 0);
			PartIOBit io2 = (PartIOBit)getPart(size - 1, size - 1 - (i + o));
			PartIOBit io3 = (PartIOBit)getPart(i + o, size - 1);
			PartIOBit io4 = (PartIOBit)getPart(0, i + o);
			
			io1.onInputChange(ForgeDirection.SOUTH);
			io2.onInputChange(ForgeDirection.WEST);
			io3.onInputChange(ForgeDirection.NORTH);
			io4.onInputChange(ForgeDirection.EAST);
		}
	}
	
	public CircuitProperties getProperties()
	{
		return prop;
	}
	
	public boolean supportsBundled()
	{
		return size > 16;
	}
	
	public int getMeta(int x, int y)
	{
		if(x < 0 || y < 0 || x >= size || y >= size) return 0;
		return meta[x][y];
	}
	
	public void setMeta(int x, int y, int m)
	{
		if(x < 0 || y < 0 || x >= size || y >= size) return;
		meta[x][y] = m;
	}
	
	public int getID(int x, int y) 
	{
		if(x < 0 || y < 0 || x >= size || y >= size) return 0;
		return CircuitPart.getId(parts[x][y]);
	}
	
	public void setID(int x, int y, int i)
	{
		if(x < 0 || y < 0 || x >= size || y >= size) return;
		parts[x][y] = CircuitPart.getPart(i, x, y, this);
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
		this.parts = new CircuitPart[size][size];
		for(int x = 0; x < size; x++)
		{
			for(int y = 0; y < size; y++)
			{
				parts[x][y] = CircuitPart.getPart(0, x, y, this);
			}
		}
		this.meta = new int[size][size];
		tickSchedule = new LinkedHashSet<Vec2>();
		updateQueue = new LinkedHashSet<Vec2>();
		this.size = size;
		setup();
		if(!supportsBundled()) prop.setCon(0);
	}
	
	public CircuitPart getPart(int x, int y)
	{
		if(x < 0 || y < 0 || x >= size || y >= size) return null;
		return parts[x][y];
	}
	
	public void scheduleTick(int x, int y)
	{
		Vec2 p = new Vec2(x, y);
		tickSchedule.add(p);
	}
	
	public void markForUpdate(int x, int y)
	{
		if(!queueEnabled) return;
		Vec2 p = new Vec2(x, y);
		updateQueue.add(p);
	}
	
	public void updateMatrix()
	{
		LinkedHashSet<Vec2> tmp = (LinkedHashSet<Vec2>)tickSchedule.clone();
		tickSchedule.clear();
		for(Vec2 v : tmp)
		{
			getPart(v.x, v.y).onScheduledTick();
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
		
		CircuitProperties prop = CircuitProperties.readFromNBT(compound.getCompoundTag("properties"));
		
		int size = compound.getInteger("size");
		LinkedHashSet<Vec2> scheduledTicks = new LinkedHashSet<Vec2>();
		
		int[] scheduledList = compound.getIntArray("scheduled");
		for(int i = 0; i < scheduledList.length; i += 2)
		{
			scheduledTicks.add(new Vec2(scheduledList[i], scheduledList[i + 1]));
		}
		
		return new CircuitData(size, parent, id, meta, scheduledTicks, prop);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{			
		NBTTagList idlist = new NBTTagList();
		for(int i = 0; i < size; i++)
		{
			int[] id = new int[size];
			for(int j = 0; j < size; j++)
			{
				id[j] = CircuitPart.getId(parts[i][j]);
			}
			idlist.appendTag(new NBTTagIntArray(id));
		}
		
		NBTTagList metalist = new NBTTagList();
		for(int i = 0; i < size; i++)
		{
			metalist.appendTag(new NBTTagIntArray(meta[i].clone()));
		}
		
		compound.setInteger("size", size);
		compound.setTag("id", idlist);
		compound.setTag("meta", metalist);
		compound.setTag("properties", prop.writeToNBT(new NBTTagCompound()));
		
		LinkedList<Integer> tmp = new LinkedList<Integer>();
		for(Vec2 v : tickSchedule)
		{
			tmp.add(v.x);
			tmp.add(v.y);
		}
		compound.setIntArray("scheduled", Ints.toArray(tmp));
		
		return compound;
	}
	
	public void writeToStream(ByteBuf buf)
	{
		buf.writeInt(updateQueue.size());
		for(Vec2 v : updateQueue)
		{
			buf.writeByte(v.x);
			buf.writeByte(v.y);
			buf.writeByte(CircuitPart.getId(parts[v.x][v.y]));
			buf.writeInt(meta[v.x][v.y]);
		}
		updateQueue.clear();
	}
	
	public void readFromStream(ByteBuf buf)
	{
		int length = buf.readInt();
		for(int i = 0; i < length; i++)
		{
			int x = buf.readByte();
			int y = buf.readByte();
			int cid = buf.readByte();
			int data = buf.readInt();
			setID(x, y, cid);
			meta[x][y] = data;
		}
	}
	
	public boolean checkUpdate()
	{
		return updateQueue.size() > 0;
	}
	
	public void setQueueEnabled(boolean enabled)
	{
		queueEnabled = enabled;
	}
	
	public static CircuitData createShallowInstance(int state, ICircuit parent)
	{
		CircuitData data = new CircuitData();
		data.size = 3;
		data.parts = new CircuitPart[3][3];
		
		data.parts[0][0] = CircuitPart.getPart(0, 0, 0, data);
		data.parts[1][0] = CircuitPart.getPart(1, 0, 0, data);
		data.parts[2][0] = CircuitPart.getPart(0, 0, 0, data);
		data.parts[0][1] = CircuitPart.getPart(1, 0, 0, data);
		data.parts[1][1] = CircuitPart.getPart(0, 0, 0, data);
		data.parts[2][1] = CircuitPart.getPart(1, 0, 0, data);
		data.parts[0][2] = CircuitPart.getPart(0, 0, 0, data);
		data.parts[1][2] = CircuitPart.getPart(1, 0, 0, data);
		data.parts[2][2] = CircuitPart.getPart(0, 0, 0, data);
		
		data.meta = new int[][]{new int[]{0, 0, 0}, new int[]{0, state, 0}, new int[]{0, 0, 0}};
		data.parent = parent;
		return data;
	}
	
	public CraftingAmount getCost()
	{
		if(cost == null) calculateCost();
		return cost;
	}
	
	public void calculateCost()
	{
		cost = new CraftingAmount();
		for(CircuitPart[] p1 : parts)
		{
			for(CircuitPart part : p1)
			{
				if(part instanceof PartNull) continue;
				part.getCraftingCost(cost);
			}
		}
	}
}
