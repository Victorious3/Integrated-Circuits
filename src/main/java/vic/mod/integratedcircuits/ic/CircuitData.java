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
	private int[][] id;
	private LinkedHashSet<Vec2> tickSchedule;
	private LinkedHashSet<Vec2> updateQueue = new LinkedHashSet<Vec2>();
	
	private CraftingAmount cost;
	private int amount = -1;
	
	private ICircuit parent;
	private boolean queueEnabled = true;
	private CircuitProperties prop = new CircuitProperties();
	
	private CircuitData(){};
	
	public CircuitData(int size, ICircuit parent)
	{
		this.parent = parent;
		this.size = size;
	}
	
	private CircuitData(int size, ICircuit parent, int[][] id, int[][] meta, LinkedHashSet<Vec2> tickSchedule, CircuitProperties prop)
	{
		this.parent = parent;
		this.prop = prop;
		this.size = size;
		this.id = id;
		this.meta = meta;
		this.tickSchedule = tickSchedule;
	}
	
	public void setup()
	{
		int o = supportsBundled() ? size / 2 - 8 : 1;
		int cid = CircuitPart.getId(PartIOBit.class);
		
		for(int i = 0; i < (supportsBundled() ? 16 : 1); i++)
		{
			Vec2 pos1 = new Vec2(size - 1 - (i + o), 0);
			Vec2 pos2 = new Vec2(size - 1, size - 1 - (i + o));
			Vec2 pos3 = new Vec2(i + o, size - 1);
			Vec2 pos4 = new Vec2(0, i + o);
			
			setID(pos1, cid);
			setID(pos2, cid);
			setID(pos3, cid);
			setID(pos4, cid);
			
			PartIOBit io1 = (PartIOBit)getPart(pos1);
			PartIOBit io2 = (PartIOBit)getPart(pos2);
			PartIOBit io3 = (PartIOBit)getPart(pos3);
			PartIOBit io4 = (PartIOBit)getPart(pos4);
			
			io1.setFrequency(pos1, parent, i);
			io2.setFrequency(pos2, parent, i);
			io3.setFrequency(pos3, parent, i);
			io4.setFrequency(pos4, parent, i);
			
			io1.setRotation(pos1, parent, 0);
			io2.setRotation(pos2, parent, 1);
			io3.setRotation(pos3, parent, 2);
			io4.setRotation(pos4, parent, 3);
		}
	}
	
	/** Syncs the circuit's IO bits with the suspected input **/
	public void updateInput()
	{
		int o = supportsBundled() ? size / 2 - 8 : 1;
		
		for(int i = 0; i < (supportsBundled() ? 16 : 1); i++)
		{	
			Vec2 pos1 = new Vec2(size - 1 - (i + o), 0);
			Vec2 pos2 = new Vec2(size - 1, size - 1 - (i + o));
			Vec2 pos3 = new Vec2(i + o, size - 1);
			Vec2 pos4 = new Vec2(0, i + o);
			
			PartIOBit io1 = (PartIOBit)getPart(pos1);
			PartIOBit io2 = (PartIOBit)getPart(pos2);
			PartIOBit io3 = (PartIOBit)getPart(pos3);
			PartIOBit io4 = (PartIOBit)getPart(pos4);
			
			io1.notifyNeighbours(pos1, parent);
			io2.notifyNeighbours(pos2, parent);
			io3.notifyNeighbours(pos3, parent);
			io4.notifyNeighbours(pos4, parent);
		}
	}
	
	/** Syncs the circuit's IO bits with the suspected output **/
	public void updateOutput()
	{
		int o = supportsBundled() ? size / 2 - 8 : 1;
		
		for(int i = 0; i < (supportsBundled() ? 16 : 1); i++)
		{		
			Vec2 pos1 = new Vec2(size - 1 - (i + o), 0);
			Vec2 pos2 = new Vec2(size - 1, size - 1 - (i + o));
			Vec2 pos3 = new Vec2(i + o, size - 1);
			Vec2 pos4 = new Vec2(0, i + o);
			
			PartIOBit io1 = (PartIOBit)getPart(pos1);
			PartIOBit io2 = (PartIOBit)getPart(pos2);
			PartIOBit io3 = (PartIOBit)getPart(pos3);
			PartIOBit io4 = (PartIOBit)getPart(pos4);
			
			io1.onInputChange(pos1, parent, ForgeDirection.SOUTH);
			io2.onInputChange(pos2, parent, ForgeDirection.WEST);
			io3.onInputChange(pos3, parent, ForgeDirection.NORTH);
			io4.onInputChange(pos4, parent, ForgeDirection.EAST);
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
	
	public int getMeta(Vec2 pos)
	{
		if(pos.x < 0 || pos.y < 0 || pos.x >= size || pos.y >= size) return 0;
		return meta[pos.x][pos.y];
	}
	
	public void setMeta(Vec2 pos, int m)
	{
		if(pos.x < 0 || pos.y < 0 || pos.x >= size || pos.y >= size) return;
		meta[pos.x][pos.y] = m;
	}
	
	public int getID(Vec2 pos) 
	{
		if(pos.x < 0 || pos.y < 0 || pos.x >= size || pos.y >= size) return 0;
		return id[pos.x][pos.y];
	}
	
	public void setID(Vec2 pos, int id)
	{
		if(pos.x < 0 || pos.y < 0 || pos.x >= size || pos.y >= size) return;
		this.id[pos.x][pos.y] = id;
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
		tickSchedule = new LinkedHashSet<Vec2>();
		updateQueue = new LinkedHashSet<Vec2>();
		this.size = size;
		setup();
		if(!supportsBundled()) prop.setCon(0);	
	}

	public CircuitPart getPart(Vec2 pos)
	{
		if(pos.x < 0 || pos.y < 0 || pos.x >= size || pos.y >= size) return CircuitPart.getPart(PartNull.class);
		return CircuitPart.getPart(id[pos.x][pos.y]);
	}
	
	public void scheduleTick(Vec2 pos)
	{
		tickSchedule.add(pos.clone());
	}
	
	public void markForUpdate(Vec2 pos)
	{
		if(!queueEnabled) return;
		updateQueue.add(pos.clone());
	}
	
	public void updateMatrix()
	{
		LinkedHashSet<Vec2> tmp = (LinkedHashSet<Vec2>)tickSchedule.clone();
		tickSchedule.clear();
		for(Vec2 v : tmp)
		{
			getPart(v).onScheduledTick(v, parent);
		}
		for(int x = 0; x < size; x++)
		{
			for(int y = 0; y < size; y++)
			{
				Vec2 pos = new Vec2(x, y);
				getPart(pos).onTick(pos, parent);
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
				id[j] = this.id[i][j];
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
			buf.writeByte(id[v.x][v.y]);
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
			setID(new Vec2(x, y), cid);
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
		data.id = new int[3][3];
		data.id[1][0] = 1;
		data.id[0][1] = 1;
		data.id[2][1] = 1;
		data.id[1][2] = 1;
		
		data.meta = new int[][]{new int[]{0, 0, 0}, new int[]{0, state, 0}, new int[]{0, 0, 0}};
		data.parent = parent;
		return data;
	}
	
	/** Cached, recalculate with {@link #calculateCost()} **/
	public CraftingAmount getCost()
	{
		if(cost == null) calculateCost();
		return cost;
	}
	
	/** Cached, recalculate with {@link #calculateCost()} **/
	public int getPartAmount()
	{
		if(amount == -1) calculateCost();
		return amount;
	}
	
	public void calculateCost()
	{
		cost = new CraftingAmount();
		amount = 0;
		for(int[] i1 : id)
		{
			for(int i : i1)
			{
				CircuitPart part = CircuitPart.getPart(i);
				if(part instanceof PartNull) continue;
				part.getCraftingCost(cost);
				amount++;
			}
		}
	}
}
