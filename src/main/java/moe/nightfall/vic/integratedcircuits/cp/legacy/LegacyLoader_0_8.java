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

public final class LegacyLoader_0_8 extends LegacyLoader {
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

	/* TODO: This will not work without a valid ICircuit. Not entirely necessary.
	@Override
	public void postTransform(CircuitData cdata) {
		super.postTransform(cdata);
		cdata.propagateSignals();
	}
	*/

	{
		addTransformer(new CircuitPartTransformer(), 0); // PartNull
		addTransformer(new PartWireTransformer(), 1);
		addTransformer(new CircuitPartTransformer(), 2); // PartTorch
		addTransformer(new Part3I1OTransformer(), 3); // PartANDGate
		addTransformer(new Part3I1OTransformer(), 4); // PartORGate
		addTransformer(new Part3I1OTransformer(), 5); // PartNANDGate
		addTransformer(new Part3I1OTransformer(), 6); // PartNORGate
		addTransformer(new Part1I3OTransformer(), 7); // PartBufferGate
		addTransformer(new Part1I3OTransformer(), 8); // PartNOTGate
		addTransformer(new PartSimpleGateTransformer(), 9); // PartMultiplexer
		//addTransformer(new PartRepeaterTransformer(), 10);
		addTransformer(new PartTimerTransformer(), 11);
		//addTransformer(new PartSequencerTransformer(), 12);
		//addTransformer(new PartStateCellTransformer(), 13);
		//addTransformer(new PartRandomizerTransformer(), 14);
		addTransformer(new PartPulseFormerTransformer(), 15);
		//addTransformer(new PartRSLatchTransformer(), 16);
		//addTransformer(new PartToggleLatchTransformer(), 17);
		//addTransformer(new PartTransparentLatchTransformer(), 18);
		addTransformer(new PartXORGateTransformer(), 19); // PartXORGate
		addTransformer(new PartXORGateTransformer(), 20); // PartXNORGate
		//addTransformer(new PartSynchronizerTransformer(), 21);
		addTransformer(new CircuitPartTransformer(), 22); // PartNullCell
		addTransformer(new PartIOBitTransformer(), 23);
		addTransformer(new PartSimpleGateTransformer(), 24); // PartInvertCell
		addTransformer(new PartSimpleGateTransformer(), 25); // PartBufferCell
		addTransformer(new PartSimpleGateTransformer(), 26); // PartANDCell
		// PartTunnel (27) was not present in 0.8
	}

	// Part transformers:

	private static class CircuitPartTransformer extends PartTransformer {
		protected final int oldInput = old.allocate(4);
		protected final int newInput = transformed.allocate(4);
		@Override
		public void transformImpl() {
			setInt(newInput, getInt(oldInput));
		}

		@Override
		public void postTransform(Vec2 pos, CircuitData cdata) {
			/* TODO: If CircuitData is stored to NBT *before it is ticked* at
			 *  least once, these scheduled input updates will be dropped, and
			 *  circuit will just remain glitched if it already was.
			 * This legacy loader should not introduce any new glitches.
			 */
			// Schedule input updates for the whole circuit, to eliminate glitches.
			// TODO: Uncomment after testing: cdata.scheduleInputChange(pos);

			// Tick the whole circuit once, because many gates might need it.
			cdata.scheduleTick(pos);
		}

		protected final boolean getInput(ForgeDirection side) {
			return (getInt(oldInput) << (side.ordinal() - 2) & 8) != 0;
		}
	}

	// CircuitPart descendants:

	// PartTunnel was not present in 0.8

	// PartNull, PartNullCell and PartTorch
	// add nothing to CircuitPartTransformer

	private static abstract class PartCPGateTransformer extends CircuitPartTransformer {
		protected final int oldRotation = old.allocate(2);
		protected final int newRotation = transformed.allocate(2);

		@Override
		public void transformImpl() {
			setInt(newRotation, getInt(oldRotation));
			super.transformImpl();
		}

		protected final boolean getRotatedInput(ForgeDirection side) {
			// TODO: This should not break in future, right?
			return getInput(MiscUtils.rotn(side, getInt(oldRotation)));
		}
	}

	private static class PartIOBitTransformer extends CircuitPartTransformer {
		protected final int oldRotFreq = old.allocate(6);
		protected final int newRotFreq = transformed.allocate(6);

		@Override
		public void transformImpl() {
			setInt(newRotFreq, getInt(oldRotFreq));
			super.transformImpl();
		}
	}

	private static class PartWireTransformer extends CircuitPartTransformer {
		protected final int oldColor = old.allocate(2);
		protected final int newColor = transformed.allocate(2);

		@Override
		public void transformImpl() {
			setInt(newColor, getInt(oldColor));
			super.transformImpl();
		}
	}

	// PartCPGate descendants:

	private static abstract class PartDelayedActionTransformer extends PartCPGateTransformer {
		protected final int oldActive = old.allocate();
		protected final int oldCurrentDelay = old.allocate(8);
		protected final int newActive = transformed.allocate();
		protected final int newCurrentDelay = transformed.allocate(8);

		@Override
		public void transformImpl() {
			boolean active = getBit(oldActive);
			int currentDelay = 0;
			if (active) {
				currentDelay = getDelay() - getInt(oldCurrentDelay);
			}
			setBit(newActive, active);
			setInt(newCurrentDelay, currentDelay);
			super.transformImpl();
		}

		protected abstract int getDelay();
	}

	// PartRSLatch

	private static class PartSimpleGateTransformer extends PartCPGateTransformer {
		protected final int oldOut = old.allocate();
		protected final int oldTmp = old.allocate();
		protected final int newOut = transformed.allocate();

		@Override
		public void transformImpl() {
			setInt(newOut, getInt(oldTmp));
			super.transformImpl();
		}
	}

	// PartSynchronizer was derived from PartCPGate in 0.8
	// It is derived from PartDelayedAction in 0.9

	// PartToggleLatch

	// PartTransparentLatch

	// PartDelayedAction descendants:

	private static class PartPulseFormerTransformer extends PartDelayedActionTransformer {
		protected final int newOldIn = transformed.allocate();

		@Override
		public void transformImpl() {
			setBit(newOldIn, getRotatedInput(ForgeDirection.SOUTH));
			super.transformImpl();
		}

		@Override
		public int getDelay() {
			return 2;
		}
	}

	// PartRandomizer

	// PartRepeater

	// PartSequencer

	// PartStateCell

	private static class PartTimerTransformer extends PartDelayedActionTransformer {
		protected final int oldOut = old.allocate();
		protected final int oldDelay = old.allocate(8);
		protected final int newOut = transformed.allocate();
		protected final int newDelay = transformed.allocate(8);

		@Override
		public void transformImpl() {
			setBit(newOut, getBit(oldOut));
			setInt(newDelay, getInt(oldDelay));
			super.transformImpl();
		}

		@Override
		public int getDelay() {
			return getBit(oldOut) ? 2 : getInt(oldDelay);
		}
	}

	// PartSimpleGate descendants:

	// PartANDCell, PartBufferCell and PartMultiplexer add nothing

	// This works for all Part1I3O descendants
	private static class Part1I3OTransformer extends PartSimpleGateTransformer {
		protected final int oldConnectors = old.allocate(3);
		protected final int newConnectors = transformed.allocate(3);

		@Override
		public void transformImpl() {
			setInt(newConnectors, getInt(oldConnectors));
			super.transformImpl();
		}
	}

	// This works for all Part3I1O descendants
	private static class Part3I1OTransformer extends PartSimpleGateTransformer {
		protected final int oldConnectors = old.allocate(2);
		protected final int newConnectors = transformed.allocate(2);

		@Override
		public void transformImpl() {
			setInt(newConnectors, getInt(oldConnectors));
			super.transformImpl();
		}
	}

	// This works for both XOR and XNOR gates. They are Part2I1O in 0.9.
	private static class PartXORGateTransformer extends PartSimpleGateTransformer {
		protected final int newConnectors = transformed.allocate(2);

		@Override
		public void transformImpl() {
			setInt(newConnectors, 0);
			super.transformImpl();
		}
	}

}
