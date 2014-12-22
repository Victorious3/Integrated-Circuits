package vic.mod.integratedcircuits.misc;

public class PropertyStitcher 
{
	private int offset;
	
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
	
	public static interface IProperty<T extends Comparable>
	{
		public int getFlag();
		
		public int getOffset();
		
		public int set(T value, int data);
		
		public int invert(int data);
		
		public T get(int data);
	}
	
	public static class EnumProperty<T extends Enum<T>> extends ValueProperty
	{
		private Class<T> clazz;
		
		public EnumProperty(PropertyStitcher stitcher, Class<T> base)
		{
			super(stitcher, (byte) validate(base.getEnumConstants().length, base));
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
		public IntProperty(PropertyStitcher stitcher, int max) 
		{
			super(stitcher, max);
		}

		@Override
		public int set(Integer value, int data) 
		{
			return super.set(value, data);
		}
	}

	public static abstract class ValueProperty<T extends Comparable> implements IProperty<T>
	{
		private int flag;
		private int offset;
		private int max;
		
		public ValueProperty(PropertyStitcher stitcher, int max)
		{
			int size = 1;
			if(max > 0) size = Integer.SIZE - Integer.numberOfLeadingZeros(max);
			this.offset = stitcher.shift(size);
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
			return (T)(Integer)((data & flag) >> offset);
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
	}
	
	public static class BooleanProperty implements IProperty<Boolean>
	{
		private int offset;
		
		public BooleanProperty(PropertyStitcher stitcher)
		{
			offset = stitcher.shift(1);
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
	}
}