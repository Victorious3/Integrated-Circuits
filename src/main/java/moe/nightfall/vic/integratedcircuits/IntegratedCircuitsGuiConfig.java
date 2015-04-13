package moe.nightfall.vic.integratedcircuits;

import cpw.mods.fml.client.config.GuiConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

public class IntegratedCircuitsGuiConfig extends GuiConfig
{
    public IntegratedCircuitsGuiConfig(GuiScreen parent)
    {
        super(parent,
                new ConfigElement(Config.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
                Constants.MOD_ID,
                false,
                false,
                "Configure IntegratedCircuits here");
        titleLine2 = Config.config.getConfigFile().getAbsolutePath();

    }

}
