package moe.nightfall.vic.integratedcircuits.misc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

public class PropertyStitcher 
{
	private int offset;
	private HashMap<String, IProperty> properties = Maps.newHashMap();
	
	private int shift(IProperty property, int bits)
	{
		int shift = shift(bits);
		if(properties.containsKey(property.getName()))
			throw new RuntimeException("There is a already a property by the name " + property.getName() + " registered!");
		properties.put(property.getName(), property);
		return shift;
	}
	
	public int shift(int bits)
	{
		int oldOffset = offset;
		if(offset + bits > Integer.SIZE) 
			throw new RuntimeException("Exceeded the total number of bits adressable (" + Integer.SIZE + ")");
		offset += bits;
		return oldOffset;
	}
	
	public int getOffset()
	{
		return offset;
	}
	
	public Map<String, IProperty> getProperties()
	{
		return Collections.unmodifiableMap(properties);
	}
	
	public IProperty getPropertyByName(String name)
	{
		return properties.get(name);
	}
	
	public static interface IProperty<T extends Comparable>
	{
		public String getName();
		
		public int getFlag();
		
		public int getOffset();
		
		public int set(T value, int data);
		
		public int invert(int data);
		
		public T get(int data);
	}
	
	public static class EnumProperty<T extends Enum<T>> extends ValueProperty
	{
		private final Class<T> clazz;
		
		public EnumProperty(String name, PropertyStitcher stitcher, Class<T> base)
		{
			super(name, stitcher, (byte)validate(base.getEnumConstants().length, base));
			clazz = base;
		}
		
		private static int validate(int i, Class clazz)
		{
			if(clazz.getEnumConstants().length > Byte.MAX_VALUE)
				throw new IllegalArgumentException("The size of enum " + clazz + " exceeded " + Byte.MAX_VALUE);
			return i;
		}

		@Override
		public T get(int data) 
		{
			return (T)(clazz.getEnumConstants()[(Integer)super.get(data)]);
		}

		@Override
		public int set(Comparable value, int data) 
		{
			if(clazz.isAssignableFrom(value.getClass()))
				return super.set(((T)value).ordinal(), data);
			else if(value instanceof Number)
			{
				int i = ((Number)value).intValue();
				if(i < 0) throw new IllegalArgumentException("Got negative index!");
				if(i >= clazz.getEnumConstants().length)
					throw new ArrayIndexOutOfBoundsException("Index " + i + " exceeded the total number of values assigned by " + clazz + " (" + clazz.getEnumConstants().length + ")");
				return super.set(i, data);
			}
			throw new IllegalArgumentException("Can only accept instances of " + clazz + " or numeric values");
		}
	}
	
	public static class IntProperty extends ValueProperty<Integer>
	{
		public IntProperty(String name, PropertyStitcher stitcher, int max) 
		{
			super(name, stitcher, max);
		}

		@Override
		public int set(Integer value, int data) 
		{
			if(value > getLimit()) throw new IllegalArgumentException("Exceeded maximum value of " + getLimit());
			return super.set(value, data);
		}
	}

	public static abstract class ValueProperty<T extends Comparable> implements IProperty<T>
	{
		private final int flag;
		private final int offset;
		private final int max;
		private final String name;
		
		public ValueProperty(String name, PropertyStitcher stitcher, int max)
		{
			this.name = name;
			int size = 1;
			if(max > 0) size = Integer.SIZE - Integer.numberOfLeadingZeros(max);
			this.offset = stitcher.shift(this, size);
			this.flag = ((1 << size) - 1) << this.offset;
			this.max = max;
		}

		@Override
		public int getFlag() 
		{
			return flag;
		}
		
		@Override
		public int getOffset() 
		{
			return offset;
		}
		
		public int getLimit()
		{
			return max;
		}
		
		@Override
		public T get(int data) 
		{
			return (T)(Integer)((data & flag) >>> offset);
		}

		@Override
		public int set(T value, int data) 
		{
			return ((data & ~flag) | (Integer)value << offset & flag);
		}

		@Override
		public int invert(int data) 
		{
			return (data ^ flag);
		}

		@Override
		public String getName() 
		{
			return name;
		}	
	}
	
	public static class BooleanProperty implements IProperty<Boolean>
	{
		private final int offset;
		private final String name;
		
		public BooleanProperty(String name, PropertyStitcher stitcher)
		{
			this.name = name;
			offset = stitcher.shift(this, 1);
		}

		@Override
		public int getFlag() 
		{
			return 1 << offset;
		}

		@Override
		public int getOffset() 
		{
			return offset;
		}
		
		@Override
		public Boolean get(int data) 
		{
			return (data & getFlag()) != 0;
		}

		@Override
		public int set(Boolean value, int data) 
		{
			int flag = getFlag();
			return value ? data | flag : data & ~flag;
		}
		
		@Override
		public int invert(int data)
		{
			return data ^ getFlag();
		}

		@Override
		public String getName() 
		{
			return name;
		}
	}
}