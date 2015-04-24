package moe.nightfall.vic.integratedcircuits.ic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import moe.nightfall.vic.integratedcircuits.Config;

/** Used for undo & redo **/
public class CircuitCache {
	private HashMap<UUID, CircuitCacheEntry> cache = Maps.newHashMap();
	private ICircuitDataProvider provider;

	public CircuitCache(ICircuitDataProvider provider) {
		this.provider = provider;
	}

	public void capture(UUID user) {
		create(user);
		cache.get(user).capture(provider.getCircuitData());
	}

	public void undo(UUID user) throws ArrayIndexOutOfBoundsException {
		create(user);
		CircuitCacheEntry entry = cache.get(user);
		entry.undo();
		provider.setCircuitData(entry.current());
	}

	public void redo(UUID user) throws ArrayIndexOutOfBoundsException {
		create(user);
		CircuitCacheEntry entry = cache.get(user);
		entry.redo();
		provider.setCircuitData(entry.current());
	}

	public CircuitData getCurrent(UUID user) {
		if (!cache.containsKey(user))
			return null;
		return cache.get(user).current();
	}

	public void create(UUID user) {
		if (!cache.containsKey(user)) {
			CircuitCacheEntry entry = new CircuitCacheEntry();
			entry.capture(provider.getCircuitData());
			cache.put(user, entry);
		}
	}

	public static class CircuitCacheEntry {
		private int position = 0;
		private ArrayList<CircuitData> cache = Lists.newArrayList();

		public void capture(CircuitData data) {
			if (position > 0 && cache.size() > 0) {
				cache.subList(cache.size() - position, cache.size()).clear();
				position = 0;
			}
			if (cache.size() >= Config.circuitCacheSize)
				cache.remove(0);
			cache.add(data.clone());
		}

		public void undo() throws ArrayIndexOutOfBoundsException {
			if (position == cache.size() - 1)
				throw new ArrayIndexOutOfBoundsException();
			position++;
		}

		public void redo() throws ArrayIndexOutOfBoundsException {
			if (position == 0)
				throw new ArrayIndexOutOfBoundsException();
			position--;
		}

		public CircuitData current() {
			return cache.get(cache.size() - position - 1).clone();
		}
	}
}
