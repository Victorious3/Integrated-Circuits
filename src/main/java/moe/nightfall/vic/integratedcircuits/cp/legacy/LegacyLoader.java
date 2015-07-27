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

public abstract class LegacyLoader implements Comparable<LegacyLoader> {

	private static final List<LegacyLoader> legacyLoaders = new ArrayList<LegacyLoader>();
	static {
		legacyLoaders.add(new LegacyLoader_0_8());
		Collections.sort(legacyLoaders);
	}

	private Map<Integer, PartTransformer> partTransformers = new HashMap<Integer, PartTransformer>();

	/** Returns version FROM which it converts.
	 *  Should always convert to the next version.
	 **/
	public abstract int getVersion();

	public static List<LegacyLoader> getLegacyLoaders(int version) {
		int i = 0;
		while (i < legacyLoaders.size()) {
			LegacyLoader loader = legacyLoaders.get(i);
			if (loader.getVersion() >= version) {
				break;
			}
			i++;
		}
		return new ArrayList<LegacyLoader>(legacyLoaders.subList(i, legacyLoaders.size()));
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

		protected AllocationMap old = new AllocationMap(32);
		protected AllocationMap transformed = new AllocationMap(32);
		private BitSet oldMeta, transformedMeta;

		public final void transform() {
			oldMeta = BitSet.valueOf(new long[] { meta });
			transformedMeta = new BitSet(32);
			transformImpl();
			// toLongArray is zero length for all-0s bitset
			meta = transformedMeta.length() == 0 ? 0 : (int) transformedMeta.toLongArray()[0];
		}

		protected final int getInt(int id) {
			BitSet bs = old.get(id, oldMeta);
			// toLongArray is zero length for all-0s bitset
			return bs.length() == 0 ? 0 : (int) bs.toLongArray()[0];
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
		class BaseTransformer extends PartTransformer {
			// Conversion: 0bAAAAAxxxx => 0bBBAAAAA

			int oldA, newA, newB;
			{
				old.skip(4);
				oldA = old.allocate(5);

				newA = transformed.allocate(5);
				newB = transformed.allocate(2);
			}

			@Override
			protected void transformImpl() {
				setInt(newA, getInt(oldA));
				setInt(newB, 3);
			}
		};

		PartTransformer transformer = new BaseTransformer();

		transformer.meta = 0x1F0; // 0b111110000
		transformer.transform();

		assertEquals(0x7F, transformer.meta); // 0b1111111


		class DerivedTransformer extends BaseTransformer {
			// Conversion: 0bEEDCAAAAAxxxx => 0bDEEECBBAAAAA (newE is oldE+1)

			int oldC, oldD, oldE, newC, newD, newE;
			{
				oldC = old.allocate();
				oldD = old.allocate();
				oldE = old.allocate(2);

				newC = transformed.allocate();
				newE = transformed.allocate(3);
				newD = transformed.allocate();
			}

			@Override
			protected void transformImpl() {
				super.transformImpl();
				setBit(newC, getBit(oldC));
				setBit(newD, getBit(oldD));
				setInt(newE, getInt(oldE) + 1);
			}
		}

		transformer = new DerivedTransformer();

		transformer.meta = 0x1D55; // 0b1110101010101
		transformer.transform();

		assertEquals(0xC75, transformer.meta); // 0b110001110101
	}*/

	public static class AllocationMap {

		protected int index;
		protected int size;
		protected int id = 0;

		private Map<Integer, Allocation> allocationMap = new HashMap<Integer, Allocation>();

		public AllocationMap(int size) {
			this.size = size;
		}

		public void skip(int size) {
			index += size;
			if (index >= this.size)
				throw new ArrayIndexOutOfBoundsException();
		}

		public int allocate(int size) {
			allocationMap.put(id, new Allocation(index, size));
			skip(size);
			return id++;
		}

		public int allocate() {
			return allocate(1);
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
				throw new ArrayIndexOutOfBoundsException();
			Allocation allocation = allocationMap.get(id);

			BitSet value = new BitSet(allocation.size);
			for (int i = 0; i < allocation.size; i++) {
				value.set(i, data.get(i + allocation.index));
			}
			return value;
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
		return Integer.compare(getVersion(), other.getVersion());
	}
}
