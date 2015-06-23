package moe.nightfall.vic.integratedcircuits.cp;

import io.netty.buffer.ByteBuf;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import moe.nightfall.vic.integratedcircuits.cp.legacy.LegacyLoader;
import moe.nightfall.vic.integratedcircuits.cp.part.PartIOBit;
import moe.nightfall.vic.integratedcircuits.cp.part.PartNull;
import moe.nightfall.vic.integratedcircuits.misc.CraftingAmount;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

import com.google.common.primitives.Ints;

/**
 * Only {@link #updateMatrix()} is thread safe, so keep in mind to
 * {@code synchronize} any calls to the getters & setters that change the
 * internal arrays if you use them outside of the tick loop.
 */
public class CircuitData implements Cloneable {

	// cdata version
	public static final int version = 1;

	private int size;
	private int[][] meta;
	private int[][] id;

	private HashSet<Vec2> tickSchedule = new LinkedHashSet<Vec2>();
	private HashSet<Vec2> updateQueue = new LinkedHashSet<Vec2>();
	private HashSet<Vec2> inputQueue = new LinkedHashSet<Vec2>();

	private boolean hasChanged;

	private CraftingAmount cost;
	private int amount = -1;

	private ICircuit parent;
	private boolean queueEnabled = true;
	private CircuitProperties prop = new CircuitProperties();

	// private constructor for cloning
	private CircuitData() {
	};

	public CircuitData(int size, ICircuit parent) {
		this.parent = parent;
		this.size = size;
	}

	private CircuitData(int size, ICircuit parent, int[][] id, int[][] meta, LinkedHashSet<Vec2> tickSchedule, CircuitProperties prop) {
		this.parent = parent;
		this.prop = prop;
		this.size = size;
		this.id = id;
		this.meta = meta;
		this.tickSchedule = tickSchedule;
		this.hasChanged = !isEmpty();
	}

	@Override
	/** Deep copy **/
	protected CircuitData clone() {
		CircuitData clone = new CircuitData();

		clone.size = size;
		clone.id = new int[size][size];
		clone.meta = new int[size][size];
		clone.amount = amount;
		clone.queueEnabled = queueEnabled;
		clone.cost = cost;
		clone.parent = parent;
		clone.prop = prop;
		clone.hasChanged = hasChanged;

		for (int i = 0; i < size; i++)
			clone.id[i] = id[i].clone();
		for (int i = 0; i < size; i++)
			clone.meta[i] = meta[i].clone();

		for (Vec2 vec : tickSchedule)
			clone.tickSchedule.add(vec);
		for (Vec2 vec : updateQueue)
			clone.tickSchedule.add(vec);

		return clone;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof CircuitData))
			return false;
		CircuitData cdata = (CircuitData) obj;

		if (cdata.size != size)
			return false;
		if (cdata.parent != parent)
			return false;

		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				if (cdata.meta[x][y] != meta[x][y])
					return false;
				if (cdata.id[x][y] != id[x][y])
					return false;
			}
		}

		return true;
	}

	public void setup() {
		int o = supportsBundled() ? size / 2 - 8 : 1;
		int cid = CircuitPart.getId(PartIOBit.class);

		for (int i = 0; i < (supportsBundled() ? 16 : 1); i++) {
			Vec2 pos1 = new Vec2(size - 1 - (i + o), 0);
			Vec2 pos2 = new Vec2(size - 1, size - 1 - (i + o));
			Vec2 pos3 = new Vec2(i + o, size - 1);
			Vec2 pos4 = new Vec2(0, i + o);

			setID(pos1, cid);
			setID(pos2, cid);
			setID(pos3, cid);
			setID(pos4, cid);

			PartIOBit io1 = (PartIOBit) getPart(pos1);
			PartIOBit io2 = (PartIOBit) getPart(pos2);
			PartIOBit io3 = (PartIOBit) getPart(pos3);
			PartIOBit io4 = (PartIOBit) getPart(pos4);

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
	public void updateInput() {
		int o = supportsBundled() ? size / 2 - 8 : 1;

		for (int i = 0; i < (supportsBundled() ? 16 : 1); i++) {
			Vec2 pos1 = new Vec2(size - 1 - (i + o), 0);
			Vec2 pos2 = new Vec2(size - 1, size - 1 - (i + o));
			Vec2 pos3 = new Vec2(i + o, size - 1);
			Vec2 pos4 = new Vec2(0, i + o);

			PartIOBit io1 = (PartIOBit) getPart(pos1);
			PartIOBit io2 = (PartIOBit) getPart(pos2);
			PartIOBit io3 = (PartIOBit) getPart(pos3);
			PartIOBit io4 = (PartIOBit) getPart(pos4);

			io1.notifyNeighbours(pos1, parent);
			io2.notifyNeighbours(pos2, parent);
			io3.notifyNeighbours(pos3, parent);
			io4.notifyNeighbours(pos4, parent);
		}
		propagateSignals();
	}

	/** Syncs the circuit's IO bits with the suspected output **/
	public void updateOutput() {
		int o = supportsBundled() ? size / 2 - 8 : 1;

		for (int i = 0; i < (supportsBundled() ? 16 : 1); i++) {
			Vec2 pos1 = new Vec2(size - 1 - (i + o), 0);
			Vec2 pos2 = new Vec2(size - 1, size - 1 - (i + o));
			Vec2 pos3 = new Vec2(i + o, size - 1);
			Vec2 pos4 = new Vec2(0, i + o);

			PartIOBit io1 = (PartIOBit) getPart(pos1);
			PartIOBit io2 = (PartIOBit) getPart(pos2);
			PartIOBit io3 = (PartIOBit) getPart(pos3);
			PartIOBit io4 = (PartIOBit) getPart(pos4);

			io1.onInputChange(pos1, parent);
			io2.onInputChange(pos2, parent);
			io3.onInputChange(pos3, parent);
			io4.onInputChange(pos4, parent);
		}
	}

	public CircuitProperties getProperties() {
		return prop;
	}

	public boolean supportsBundled() {
		return size > 16;
	}

	public int getMeta(Vec2 pos) {
		if (pos.x < 0 || pos.y < 0 || pos.x >= size || pos.y >= size)
			return 0;
		return meta[pos.x][pos.y];
	}

	public void setMeta(Vec2 pos, int m) {
		if (pos.x < 0 || pos.y < 0 || pos.x >= size || pos.y >= size)
			return;
		if (m != meta[pos.x][pos.y])
			setChanged(true);
		meta[pos.x][pos.y] = m;
	}

	public int getID(Vec2 pos) {
		if (pos.x < 0 || pos.y < 0 || pos.x >= size || pos.y >= size)
			return 0;
		return id[pos.x][pos.y];
	}

	public void setID(Vec2 pos, int id) {
		if (pos.x < 0 || pos.y < 0 || pos.x >= size || pos.y >= size)
			return;
		if (id != this.id[pos.x][pos.y])
			setChanged(true);
		this.id[pos.x][pos.y] = id;
	}

	public ICircuit getCircuit() {
		return parent;
	}

	public void setParent(ICircuit parent) {
		this.parent = parent;
	}

	public int getSize() {
		return size;
	}

	public void clear(int size) {
		this.id = new int[size][size];
		this.meta = new int[size][size];
		tickSchedule.clear();
		updateQueue.clear();
		this.size = size;
		setup();
		if (!supportsBundled())
			prop.setCon(0);
		this.setChanged(false);
	}

	public CircuitPart getPart(Vec2 pos) {
		if (pos.x < 0 || pos.y < 0 || pos.x >= size || pos.y >= size)
			return CircuitPart.getPart(PartNull.class);
		CircuitPart part = CircuitPart.getPart(id[pos.x][pos.y]);
		if (part == null) {
			IntegratedCircuits.logger.warn("Removed circuit part! " + pos);
			setID(pos, 0);
			setMeta(pos, 0);
			part = getPart(pos);
		}
		return part;
	}

	public void scheduleTick(Vec2 pos) {
		tickSchedule.add(pos);
	}

	public void scheduleInputChange(Vec2 pos) {
		inputQueue.add(pos);
	}

	public void markForUpdate(Vec2 pos) {
		if (!queueEnabled)
			return;
		updateQueue.add(pos);
	}

	/** "Instantaneously" propagate signals through wires and e.g. null cell */
	public synchronized void propagateSignals() {
		while (inputQueue.size() > 0) {
			HashSet<Vec2> tmp = (HashSet<Vec2>) inputQueue.clone();
			inputQueue.clear();
			for (Vec2 pos : tmp) {
				CircuitPart part = getPart(pos);
				part.updateInput(pos, parent);
				part.onInputChange(pos, parent);
			}
		}
	}

	public synchronized void updateMatrix() {
		// Tick all circuit parts that need to be ticked
		HashSet<Vec2> tmp = (HashSet<Vec2>) tickSchedule.clone();
		tickSchedule.clear();
		for (Vec2 pos : tmp)
			getPart(pos).onScheduledTick(pos, parent);
		
		propagateSignals();
	}

	public static CircuitData readFromNBT(NBTTagCompound compound) {
		return readFromNBT(compound, null);
	}

	public static CircuitData readFromNBT(NBTTagCompound compound, ICircuit parent) {
		int version = compound.getInteger("version");

		List<LegacyLoader> legacyLoaders = null;
		if (version < CircuitData.version) {
			legacyLoaders = LegacyLoader.getLegacyLoaders(version);
			for (LegacyLoader loader : legacyLoaders) {
				loader.transformNBT(compound);
			}
		}

		NBTTagList idlist = compound.getTagList("id", NBT.TAG_INT_ARRAY);
		int[][] id = new int[idlist.tagCount()][];
		for (int i = 0; i < idlist.tagCount(); i++) {
			id[i] = idlist.func_150306_c(i);
		}

		NBTTagList metalist = compound.getTagList("meta", NBT.TAG_INT_ARRAY);
		int[][] meta = new int[metalist.tagCount()][];
		for (int i = 0; i < metalist.tagCount(); i++) {
			meta[i] = metalist.func_150306_c(i);
		}

		CircuitProperties prop = CircuitProperties.readFromNBT(compound.getCompoundTag("properties"));
		int size = compound.getInteger("size");

		if (version < CircuitData.version) {
			for (LegacyLoader loader : legacyLoaders) {
				loader.transform(size, id, meta);
			}
		}

		LinkedHashSet<Vec2> scheduledTicks = new LinkedHashSet<Vec2>();

		int[] scheduledList = compound.getIntArray("scheduled");
		for (int i = 0; i < scheduledList.length; i += 2) {
			scheduledTicks.add(new Vec2(scheduledList[i], scheduledList[i + 1]));
		}

		CircuitData cdata = new CircuitData(size, parent, id, meta, scheduledTicks, prop);

		if (version < CircuitData.version) {
			for (LegacyLoader loader : legacyLoaders) {
				loader.postTransform(cdata);
			}
		}

		return cdata;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList idlist = new NBTTagList();
		for (int i = 0; i < size; i++) {
			int[] id = new int[size];
			for (int j = 0; j < size; j++)
				id[j] = this.id[i][j];
			idlist.appendTag(new NBTTagIntArray(id));
		}

		NBTTagList metalist = new NBTTagList();
		for (int i = 0; i < size; i++) {
			metalist.appendTag(new NBTTagIntArray(meta[i].clone()));
		}

		compound.setInteger("size", size);
		compound.setTag("id", idlist);
		compound.setTag("meta", metalist);
		compound.setTag("properties", prop.writeToNBT(new NBTTagCompound()));

		LinkedList<Integer> tmp = new LinkedList<Integer>();
		for (Vec2 v : tickSchedule) {
			tmp.add(v.x);
			tmp.add(v.y);
		}
		compound.setIntArray("scheduled", Ints.toArray(tmp));

		compound.setInteger("version", version);

		return compound;
	}

	public void writeToStream(ByteBuf buf) {
		buf.writeInt(updateQueue.size());
		for (Vec2 v : updateQueue) {
			buf.writeByte(v.x);
			buf.writeByte(v.y);
			buf.writeByte(id[v.x][v.y]);
			buf.writeInt(meta[v.x][v.y]);
		}
		updateQueue.clear();
	}

	public void readFromStream(ByteBuf buf) {
		int length = buf.readInt();
		for (int i = 0; i < length; i++) {
			int x = buf.readByte();
			int y = buf.readByte();
			int cid = buf.readByte();
			int data = buf.readInt();
			setID(new Vec2(x, y), cid);
			meta[x][y] = data;
		}
	}

	public boolean checkUpdate() {
		return updateQueue.size() > 0;
	}

	public void setQueueEnabled(boolean enabled) {
		queueEnabled = enabled;
	}

	public static CircuitData createShallowInstance(int state, ICircuit parent) {
		CircuitData data = new CircuitData();
		data.size = 3;
		data.id = new int[3][3];
		data.id[1][0] = 1;
		data.id[0][1] = 1;
		data.id[2][1] = 1;
		data.id[1][2] = 1;

		data.meta = new int[][] { new int[] { 0, 0, 0 }, new int[] { 0, state, 0 }, new int[] { 0, 0, 0 } };
		data.parent = parent;
		return data;
	}

	/** Cached, recalculate with {@link #calculateCost()} **/
	public CraftingAmount getCost() {
		if (cost == null)
			calculateCost();
		return cost;
	}

	/** Cached, recalculate with {@link #calculateCost()} **/
	public int getPartAmount() {
		if (amount == -1)
			calculateCost();
		return amount;
	}

	public void calculateCost() {
		cost = new CraftingAmount();
		amount = 0;
		for (int x = 1; x < size - 1; x++) {
			for (int y = 1; y < size - 1; y++) {
				Vec2 pos = new Vec2(x, y);
				CircuitPart part = getPart(pos);
				if (part instanceof PartNull)
					continue;
				part.getCraftingCost(cost, this, pos);
				amount++;
			}
		}
	}

	public boolean hasChanged() {
		return hasChanged;
	}

	public void setChanged(boolean hasChanged) {
		this.hasChanged = hasChanged;
	}

	public boolean isEmpty() {
		for (int x = 1; x < size - 1; x++) {
			for (int y = 1; y < size - 1; y++) {
				if (id[x][y] != 0)
					return false;
			}
		}
		return true;
	}
}
