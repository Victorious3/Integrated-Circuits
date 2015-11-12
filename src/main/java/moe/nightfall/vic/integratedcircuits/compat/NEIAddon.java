package moe.nightfall.vic.integratedcircuits.compat;

import codechicken.nei.NEIClientConfig;
import codechicken.nei.api.API;
import moe.nightfall.vic.integratedcircuits.Content;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import net.minecraft.item.ItemStack;

public class NEIAddon {
	public void initialize() {
		API.hideItem(new ItemStack(Content.blockSocket));
		API.hideItem(new ItemStack(Content.itemPCBPrint));
	}

	public static void hideGUI(boolean hide) {
		if (IntegratedCircuits.isNEILoaded) {
			NEIClientConfig.setInternalEnabled(!hide);
		}
	}
}
