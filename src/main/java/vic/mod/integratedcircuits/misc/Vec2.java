package vic.mod.integratedcircuits.misc;

import net.minecraftforge.common.util.ForgeDirection;

/** An int value pair **/
public class Vec2 implements Cloneable
{
	public final int x, y;
	
	public Vec2(int a, int b)
	{
		this.x = a;
		this.y = b;
	}
	
	@Override
	public Vec2 clone()
	{
		return new Vec2(x, y);
	}
	
	public Vec2 offset(ForgeDirection dir)
	{
		return new Vec2(x + dir.offsetX, y + dir.offsetZ);
	}

	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Vec2 other = (Vec2)obj;
		return x == other.x && y == other.y;
	}

	@Override
	public String toString() 
	{
		return "Vec2[" + x + ", " + y + "]";
	}
}
