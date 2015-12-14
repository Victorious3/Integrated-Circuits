package moe.nightfall.vic.integratedcircuits.misc;

import java.util.Objects;

import net.minecraft.util.EnumFacing;

/** An int value pair **/
public class Vec2 {

	public static final Vec2 zero = new Vec2(0, 0);

	public final int x, y;

	public Vec2(int a, int b) {
		this.x = a;
		this.y = b;
	}

	public Vec2 offset(EnumFacing dir) {
		return new Vec2(x + dir.getDirectionVec().getX(), y + dir.getDirectionVec().getZ());
	}

	public double distanceTo(Vec2 other) {
		return Math.sqrt(Math.pow(other.x - x, 2) + Math.pow(other.y - y, 2));
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vec2 other = (Vec2) obj;
		return x == other.x && y == other.y;
	}

	@Override
	public String toString() {
		return "Vec2[" + x + ", " + y + "]";
	}
}
