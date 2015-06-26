package moe.nightfall.vic.integratedcircuits.cp.legacy;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.nbt.NBTTagCompound;

public class LegacyLoader implements Comparable<LegacyLoader> {

	private static final List<LegacyLoader> legacyLoaders = new ArrayList<LegacyLoader>();

	public final int version;
	private Map<Integer, PartTransformer> partTransformers = new HashMap<Integer, PartTransformer>();

	public LegacyLoader(int version) {
		this.version = version;
	}

	public static void addLegacyLoader(LegacyLoader loader) {
		legacyLoaders.add(loader);
		Collections.sort(legacyLoaders);
	}

	public static List<LegacyLoader> getLegacyLoaders(int version) {
		for (int i = 0; i < legacyLoaders.size(); i++) {
			LegacyLoader loader = legacyLoaders.get(i);
			if (loader.version == version) {
				return legacyLoaders.subList(0, i);
			}
		}
		return new ArrayList<LegacyLoader>();
	}

	public final LegacyLoader addTransformer(PartTransformer transformer, int id) {
		partTransformers.put(id, transformer);
		return this;
	}

	public void transformNBT(NBTTagCompound data) {

	}

	public void transform(int size, int[][] id, int[][] meta) {
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				int pid = id[x][y];
				if (partTransformers.containsKey(pid)) {
					PartTransformer transformer = partTransformers.get(pid);

					transformer.id = pid;
					transformer.meta = meta[x][y];

					transformer.transform();

					id[x][y] = transformer.id;
					meta[x][y] = transformer.meta;
				}
			}
		}
	}
	
	public void postTransform(CircuitData cdata) {
		for (int x = 0; x < cdata.getSize(); x++) {
			for (int y = 0; y < cdata.getSize(); y++) {
				Vec2 pos = new Vec2(x, y);
				int pid = cdata.getID(pos);
				if (partTransformers.containsKey(pid)) {
					PartTransformer transformer = partTransformers.get(pid);

					transformer.id = pid;
					transformer.meta = cdata.getMeta(pos);

					transformer.postTransform(pos, cdata);
				}
			}
		}
	}

	public abstract static class PartTransformer {

		protected int id;
		private int meta;

		protected AllocationMap old, transformed;
		private BitSet oldMeta, transformedMeta;

		protected PartTransformer() {
			old = new AllocationMap(32);
			transformed = new AllocationMap(32);
		}

		public final void transform() {
			oldMeta = BitSet.valueOf(new long[] { meta });
			transformedMeta = (BitSet) oldMeta.clone();
			old.copyTo(transformed, transformedMeta);
			transformImpl();
			meta = (int) transformedMeta.toLongArray()[0];
		}

		protected final int getInt(int id) {
			return (int) old.get(id, oldMeta).toLongArray()[0];
		}

		protected final boolean getBit(int id) {
			return old.get(id, oldMeta).get(0);
		}

		protected final void setInt(int id, int value) {
			transformed.set(id, BitSet.valueOf(new long[] { value }), transformedMeta);
		}

		protected final void setBit(int id, boolean value) {
			BitSet data = new BitSet(1);
			data.set(0, value);
			transformed.set(id, data, transformedMeta);
		}

		protected void transformImpl() {
		}
		
		public void postTransform(Vec2 crd, CircuitData cdata) {
		}
	}

	// TODO Create test cases
	/*
	@Test
	public void test() {
		PartTransformer transformer = new PartTransformer() {{
				old.skip(4);
				old.allocate(0, 5);
	
				transformed.allocate(0, 5);
				transformed.allocate(1, 2);
			}
	
			@Override
			protected void transformImpl() {
				setInt(1, 3);
			}	
		};

		transformer.meta = 0x1F0;
		transformer.transform();

		assertEquals(0x7F, transformer.meta);
	}*/

	public static class AllocationMap {

		protected int index;
		protected int size;

		private Map<Integer, Allocation> allocationMap = new HashMap<Integer, Allocation>();

		public AllocationMap(int size) {
			this.size = size;
		}

		public AllocationMap skip(int size) {
			index += size;
			if (index >= this.size)
				throw new ArrayIndexOutOfBoundsException();
			return this;
		}

		public AllocationMap allocate(int id, int size) {
			allocationMap.put(id, new Allocation(index, size));
			skip(size);
			return this;
		}

		public AllocationMap allocate(int id) {
			allocationMap.put(id, new Allocation(index, 1));
			skip(1);
			return this;
		}

		public void set(int id, BitSet value, BitSet data) {
			if (!allocationMap.containsKey(id))
				return;
			Allocation allocation = allocationMap.get(id);

			for (int i = 0; i < allocation.size; i++) {
				data.set(i + allocation.index, value.get(i));
			}
		}

		public BitSet get(int id, BitSet data) {
			if (!allocationMap.containsKey(id))
				return new BitSet();
			Allocation allocation = allocationMap.get(id);

			BitSet value = new BitSet(allocation.size);
			for (int i = 0; i < allocation.size; i++) {
				value.set(i, data.get(i + allocation.index));
			}
			return value;
		}

		public void copyTo(AllocationMap other, BitSet data) {
			BitSet data1 = (BitSet) data.clone();
			data.clear();
			for (int id : allocationMap.keySet()) {
				Allocation allocation = allocationMap.get(id);
				other.set(id, get(id, data1), data);
			}
		}

		private static class Allocation {

			private Allocation(int index, int size) {
				this.index = index;
				this.size = size;
			}

			private final int index;
			private final int size;
		}
	}

	@Override
	public int compareTo(LegacyLoader other) {
		return Integer.compare(version, other.version);
	}
}
