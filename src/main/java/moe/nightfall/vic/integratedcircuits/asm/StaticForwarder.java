package moe.nightfall.vic.integratedcircuits.asm;

import moe.nightfall.vic.integratedcircuits.api.gate.GateIOProvider;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketWrapper;

public final class StaticForwarder {

	private StaticForwarder() {

	}

	public static GateIOProvider inject(GateIOProvider provider, Object obj) {
		if (obj instanceof ISocketWrapper) {
			provider.socket = ((ISocketWrapper) obj).getSocket();
		}
		return provider;
	}
}
