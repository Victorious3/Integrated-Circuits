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

public class LegacyLoader_0_8 extends LegacyLoader {
	@Override
	public int getVersion() {
		return 0;
	}

	private int swapBundledAnalog(int con) {
		// Analog is 1 (0b01), Bundled is 2 (0b10)
		// In 0.8 it was the other way around, so swap 0b01 and 0b10
		int mask = (con ^ (con << 1)) & 0xAA; // 0b10101010 is unsupported :(
		mask |= mask >> 1; // Build mask with all sides to change
		return con ^ mask; // Change them!
	}

	@Override
	public void transformNBT(NBTTagCompound data) {
		// Swap analog and bundled IO modes
		NBTTagCompound props = data.getCompoundTag("properties");
		props.setInteger("con", swapBundledAnalog(props.getInteger("con")));
	}
}
