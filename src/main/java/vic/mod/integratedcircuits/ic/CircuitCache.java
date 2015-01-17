package vic.mod.integratedcircuits.ic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/** Used for undo & redo **/
public class CircuitCache 
{
	private HashMap<UUID, CircuitCacheEntry> cache = Maps.newHashMap();
	private ICircuitDataProvider provider;
	
	public CircuitCache(ICircuitDataProvider provider)
	{
		this.provider = provider;
	}
	
	public void capture(UUID user)
	{
		create(user);
		cache.get(user).capture(provider.getCircuitData());
	}
	
	public void undo(UUID user) throws ArrayIndexOutOfBoundsException
	{
		create(user);
		CircuitCacheEntry entry = cache.get(user);
		entry.undo(provider.getCircuitData());
		provider.setCircuitData(entry.current());
	}
	
	public void redo(UUID user) throws ArrayIndexOutOfBoundsException
	{
		create(user);
		CircuitCacheEntry entry = cache.get(user);
		entry.redo();
		provider.setCircuitData(entry.current());
	}
	
	public CircuitData getCurrent(UUID user)
	{
		if(!cache.containsKey(user)) return null;
		return cache.get(user).current();
	}
	
	private void create(UUID user)
	{
		if(!cache.containsKey(user))
			cache.put(user, new CircuitCacheEntry());
	}
	
	public static class CircuitCacheEntry
	{
		private int position = 0;
		//TODO Config option?
		private static final int MAX_SIZE = 20;
		private ArrayList<CircuitData> cache = Lists.newArrayList();
		
		public void capture(CircuitData data)
		{
			if(position < cache.size() - 1 && cache.size() > 0)
				cache.subList(position, cache.size() - 1).clear();
			if(cache.size() >= MAX_SIZE)
				cache.remove(0);
			else if(cache.size() > 0) position++;
			cache.add(data.clone());
			
			System.out.println(position + " " + cache.size());
		}
		
		public void undo(CircuitData data) throws ArrayIndexOutOfBoundsException
		{
			if(position == 0) throw new ArrayIndexOutOfBoundsException();
			if(position == cache.size() - 1)
				capture(data);
			position--;
			System.out.println(position + " " + cache.size());
		}
		
		public void redo() throws ArrayIndexOutOfBoundsException
		{
			if(position >= cache.size() - 1) throw new ArrayIndexOutOfBoundsException();
			else position++;
			
			System.out.println(position + " " + cache.size());
		}
		
		public CircuitData current()
		{
			return cache.size() > position ? cache.get(position) : null;
		}
	}
}
