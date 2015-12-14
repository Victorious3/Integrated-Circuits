package moe.nightfall.vic.integratedcircuits.cp.legacy;

// TODO Legacy loading is obsolte since the minecraft version changed.
// Instead we should just remove all of this mess and have less code to carry around...
public final class LegacyLoader_0_8 /*extends LegacyLoader*/ {
	/*
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

	{
		// PartNull does not require any meaningful transformation
		addTransformer(new PartWireTransformer(), 1);
		addTransformer(new CircuitPartTransformer(), 2); // PartTorch
		addTransformer(new Part3I1OTransformer(), 3); // PartANDGate
		addTransformer(new Part3I1OTransformer(), 4); // PartORGate
		addTransformer(new Part3I1OTransformer(), 5); // PartNANDGate
		addTransformer(new Part3I1OTransformer(), 6); // PartNORGate
		addTransformer(new Part1I3OTransformer(), 7); // PartBufferGate
		addTransformer(new Part1I3OTransformer(), 8); // PartNOTGate
		addTransformer(new PartSimpleGateTransformer(), 9); // PartMultiplexer
		addTransformer(new PartRepeaterTransformer(), 10);
		addTransformer(new PartTimerTransformer(), 11);
		addTransformer(new PartSequencerTransformer(), 12);
		addTransformer(new PartStateCellTransformer(), 13);
		addTransformer(new PartRandomizerTransformer(), 14);
		addTransformer(new PartPulseFormerTransformer(), 15);
		addTransformer(new PartRSLatchTransformer(), 16);
		addTransformer(new PartToggleLatchTransformer(), 17);
		addTransformer(new PartTransparentLatchTransformer(), 18);
		addTransformer(new PartXORGateTransformer(), 19); // PartXORGate
		addTransformer(new PartXORGateTransformer(), 20); // PartXNORGate
		addTransformer(new PartSynchronizerTransformer(), 21);
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
			// Schedule input updates for the whole circuit, to eliminate glitches.
			cdata.scheduleInputChange(pos);

			// Tick the whole circuit once, because many gates might need it.
			cdata.scheduleTick(pos);
		}

		protected final boolean getInput(EnumFacing side) {
			return (getInt(oldInput) << (side.ordinal() - 2) & 8) != 0;
		}
	}

	// CircuitPart descendants:

	// PartTunnel was not present in 0.8
	// PartNull does not require any meaningful transformation

	// PartNullCell and PartTorch add nothing to CircuitPartTransformer

	private static abstract class PartCPGateTransformer extends CircuitPartTransformer {
		protected final int oldRotation = old.allocate(2);
		protected final int newRotation = transformed.allocate(2);

		@Override
		public void transformImpl() {
			super.transformImpl();
			setInt(newRotation, getInt(oldRotation));
		}

		protected final boolean getRotatedInput(EnumFacing side) {
			// TODO: This should not break in future, right?
			return getInput(MiscUtils.rotn(side, getInt(oldRotation)));
		}
	}

	private static class PartIOBitTransformer extends CircuitPartTransformer {
		protected final int oldRotFreq = old.allocate(6);
		protected final int newRotFreq = transformed.allocate(6);

		@Override
		public void transformImpl() {
			super.transformImpl();
			setInt(newRotFreq, getInt(oldRotFreq));
		}
	}

	private static class PartWireTransformer extends CircuitPartTransformer {
		protected final int oldColor = old.allocate(2);
		protected final int newColor = transformed.allocate(2);

		@Override
		public void transformImpl() {
			super.transformImpl();
			setInt(newColor, getInt(oldColor));
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
			super.transformImpl();
			boolean active = getBit(oldActive);
			int currentDelay = 0;
			if (active) {
				currentDelay = getDelay() - getInt(oldCurrentDelay);
				if (currentDelay < 0)
					currentDelay = 0;
			}
			setBit(newActive, active);
			setInt(newCurrentDelay, currentDelay);
		}

		protected abstract int getDelay();
	}

	private static class PartRSLatchTransformer extends PartCPGateTransformer {
		protected final int oldOut = old.allocate();
		protected final int oldTmp = old.allocate();
		protected final int oldMode = old.allocate(2);
		
		protected final int newState = transformed.allocate(2);
		protected final int newCheck = transformed.allocate();
		protected final int newMode = transformed.allocate(2);

		@Override
		public void transformImpl() {
			super.transformImpl();
			setInt(newMode, getInt(oldMode));
			setBit(newCheck, false);
			setInt(newState, getBit(oldOut) ? 1 : 2);
		}
	}

	private static class PartSimpleGateTransformer extends PartCPGateTransformer {
		protected final int oldOut = old.allocate();
		protected final int oldTmp = old.allocate();
		protected final int newOut = transformed.allocate();

		@Override
		public void transformImpl() {
			super.transformImpl();
			setInt(newOut, getInt(oldTmp));
		}
	}

	// PartSynchronizer was derived from PartCPGate in 0.8
	// It is derived from PartDelayedAction in 0.9
	private static class PartSynchronizerTransformer extends PartCPGateTransformer {
		protected final int oldEast = old.allocate();
		protected final int oldWest = old.allocate();
		protected final int oldOut = old.allocate();

		protected final int newActive = transformed.allocate();
		protected final int newCurrentDelay = transformed.allocate(8);
		protected final int newEast = transformed.allocate();
		protected final int newWest = transformed.allocate();
		protected final int newOldEast = transformed.allocate();
		protected final int newOldWest = transformed.allocate();

		@Override
		public void transformImpl() {
			super.transformImpl();
			setBit(newEast, getBit(oldEast));
			setBit(newWest, getBit(oldWest));
			
			// Start new delay if output is high
			setBit(newActive, getBit(oldOut));
			setInt(newCurrentDelay, 0);
			
			// Store current input as "old" (used for edge detection).
			setBit(newOldEast, getRotatedInput(EnumFacing.EAST));
			setBit(newOldWest, getRotatedInput(EnumFacing.WEST));
		}
	}

	private static class PartToggleLatchTransformer extends PartCPGateTransformer {
		protected final int oldOut = old.allocate();
		protected final int oldTmp = old.allocate();
		
		protected final int newOut = transformed.allocate();
		protected final int newOldNorth = transformed.allocate();
		protected final int newOldSouth = transformed.allocate();

		@Override
		public void transformImpl() {
			super.transformImpl();
			setBit(newOut, getBit(oldOut));
			
			// Store current input as "old" (used for edge detection).
			setBit(newOldNorth, getRotatedInput(EnumFacing.NORTH));
			setBit(newOldSouth, getRotatedInput(EnumFacing.SOUTH));
		}
	}

	private static class PartTransparentLatchTransformer extends PartCPGateTransformer {
		protected final int oldOut = old.allocate();
		protected final int oldTmp = old.allocate();
		
		protected final int newOut = transformed.allocate();

		@Override
		public void transformImpl() {
			super.transformImpl();
			setBit(newOut, getBit(oldOut));
		}
	}

	// PartDelayedAction descendants:

	private static class PartPulseFormerTransformer extends PartDelayedActionTransformer {
		protected final int newOldIn = transformed.allocate();

		@Override
		public void transformImpl() {
			super.transformImpl();
			setBit(newOldIn, getRotatedInput(EnumFacing.SOUTH));
		}

		@Override
		public int getDelay() {
			return 2;
		}
	}

	private static class PartRandomizerTransformer extends PartDelayedActionTransformer {
		protected final int oldRandom = old.allocate(3);
		protected final int newRandom = transformed.allocate(3);

		@Override
		public void transformImpl() {
			super.transformImpl();
			setBit(newRandom, getBit(oldRandom));
		}

		@Override
		public int getDelay() {
			return 2;
		}
	}

	private static class PartRepeaterTransformer extends PartDelayedActionTransformer {
		protected final int oldDelay = old.allocate(8);
		protected final int oldOut = old.allocate();
		protected final int newDelay = transformed.allocate(8);
		protected final int newOut = transformed.allocate();

		@Override
		public void transformImpl() {
			super.transformImpl();
			int delay = getInt(oldDelay);
			if (delay != 255)
				delay -= 1;
			setBit(newOut, getBit(oldOut));
			setInt(newDelay, delay);
		}

		@Override
		public int getDelay() {
			return getInt(oldDelay);
		}
	}

	private static class PartSequencerTransformer extends PartDelayedActionTransformer {
		// It was derived from timer in 0.8 and might be in an invalid state,
		//  because in 0.9 it works exactly as P:R counterpart.

		// Old PartTimer and PartSequencer properties
		protected final int oldOut = old.allocate();
		protected final int oldDelay = old.allocate(8);
		protected final int oldOutSide = old.allocate(2);

		// New PartSequencer properties
		protected final int newOutSide = transformed.allocate(2);
		protected final int newDelay = transformed.allocate(8);

		@Override
		public void transformImpl() {
			super.transformImpl();
			setInt(newDelay, getInt(Math.min(oldDelay + 2, 255)));
			// +2 is to keep total period the same (if possible)
			setInt(newOutSide, getInt(oldOutSide));
			
			// Start new delay if sequencer was stopped.
			// Next tick is scheduled by CircuitPartTransformer for all gates.
			if (!getBit(oldActive)) {
				setBit(newActive, true);
				setInt(newCurrentDelay, 0);
			}
		}

		@Override
		public int getDelay() {
			return getInt(oldDelay);
		}
	}

	private static class PartStateCellTransformer extends PartDelayedActionTransformer {
		protected final int oldDelay = old.allocate(8);
		protected final int oldOutWest = old.allocate();
		protected final int oldOutNorth = old.allocate();
		protected final int newDelay = transformed.allocate(8);
		protected final int newOutWest = transformed.allocate();
		protected final int newOutNorth = transformed.allocate();

		@Override
		public void transformImpl() {
			super.transformImpl();
			setInt(newDelay, getInt(oldDelay));
			if (getRotatedInput(EnumFacing.SOUTH)) {
				// Fix possibly invalid state
				setBit(newOutWest, true);
				setBit(newOutNorth, false);
				setBit(newActive, false);
				setInt(newCurrentDelay, 0);
			} else {
				setBit(newOutWest, getBit(oldOutWest));
				setBit(newOutNorth, getBit(oldOutNorth));
			}
		}

		@Override
		public int getDelay() {
			return getBit(oldOutNorth) ? 2 : getInt(oldDelay);
		}
	}

	private static class PartTimerTransformer extends PartDelayedActionTransformer {
		protected final int oldOut = old.allocate();
		protected final int oldDelay = old.allocate(8);
		protected final int newOut = transformed.allocate();
		protected final int newDelay = transformed.allocate(8);

		@Override
		public void transformImpl() {
			super.transformImpl();
			setBit(newOut, getBit(oldOut));
			setInt(newDelay, getInt(oldDelay));
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
			super.transformImpl();
			setInt(newConnectors, getInt(oldConnectors));
		}
	}

	// This works for all Part3I1O descendants
	private static class Part3I1OTransformer extends PartSimpleGateTransformer {
		protected final int oldConnectors = old.allocate(2);
		protected final int newConnectors = transformed.allocate(2);

		@Override
		public void transformImpl() {
			super.transformImpl();
			setInt(newConnectors, getInt(oldConnectors));
		}
	}

	// This works for both XOR and XNOR gates. They are Part2I1O in 0.9.
	private static class PartXORGateTransformer extends PartSimpleGateTransformer {
		protected final int newConnectors = transformed.allocate(2);

		@Override
		public void transformImpl() {
			super.transformImpl();
			setInt(newConnectors, 0);
		}
	}
	*/
}
