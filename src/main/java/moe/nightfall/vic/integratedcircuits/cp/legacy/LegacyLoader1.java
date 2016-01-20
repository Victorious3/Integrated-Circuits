package moe.nightfall.vic.integratedcircuits.cp.legacy;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.cp.legacy.LegacyLoader;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public final class LegacyLoader1 extends LegacyLoader {
	@Override
	public int getVersion() {
		return 1;
	}

	{
		addTransformer(new PartTransparentLatchTransformer(), 18);
		addTransformer(new PartTunnelTransformer(), 27);
	}

	private static class PartTransparentLatchTransformer extends PartTransformer {
		protected final int oldBits = old.allocate(4 + 2 + 1);
		protected final int newBits = transformed.allocate(4 + 2 + 1);
		protected final int newConfig = transformed.allocate(3);

		@Override
		public void transformImpl() {
			setInt(newBits, getInt(oldBits));
			setInt(newConfig, 0);
		}
	}

	private static class PartTunnelTransformer extends PartTransformer {
		// The old PartTunnel property map
		// PartCPGate:
		protected final int oldInput = old.allocate(4);
		// PartTunnel:
		protected final int oldPosX = old.allocate(8);
		protected final int oldPosY = old.allocate(8);
		protected final int oldIn = old.allocate();

		// Tunnels are now derived from wires
		// PartCPGate:
		protected final int newInput = transformed.allocate(4);
		// PartWire:
		protected final int newColor = transformed.allocate(2);
		// PartTunnel:
		protected final int newPosX = transformed.allocate(8);
		protected final int newPosY = transformed.allocate(8);
		protected final int newIn = transformed.allocate();

		@Override
		public void transformImpl() {
			setInt(newInput, getInt(oldInput));
			// Old tunnels are converted to green tunnels
			setInt(newColor, 0); // 0 is green
			setInt(newPosX, getInt(oldPosX));
			setInt(newPosY, getInt(oldPosY));
			setBit(newIn, getBit(oldIn));
		}
	}

}
