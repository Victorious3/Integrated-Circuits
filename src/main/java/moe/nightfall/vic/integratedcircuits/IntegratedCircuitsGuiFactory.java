package moe.nightfall.vic.integratedcircuits;

import cpw.mods.fml.client.IModGuiFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.util.Set;

public class IntegratedCircuitsGuiFactory implements IModGuiFactory
{

    @Override
    public void initialize(Minecraft minecraftInstance) {
        IntegratedCircuitsGuiConfig.initialize(minecraftInstance);
    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return IntegratedCircuitsGuiConfig.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }
}
