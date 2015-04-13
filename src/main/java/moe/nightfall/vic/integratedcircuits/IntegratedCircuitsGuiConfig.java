package moe.nightfall.vic.integratedcircuits;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import java.util.ArrayList;

public class IntegratedCircuitsGuiConfig extends GuiConfig
{
    private static ArrayList<IConfigElement> configElements;

    public IntegratedCircuitsGuiConfig(GuiScreen parent)
    {
        super(parent,
                configElements,
                Constants.MOD_ID,
                true,
                false,
                "Configure IntegratedCircuits here");
        titleLine2 = Config.config.getConfigFile().getAbsolutePath();

    }

    public static void initialize(Minecraft minecraftInstance) {
        configElements = new ArrayList<IConfigElement>();

        ArrayList<IConfigElement> generalCategory = new ArrayList<IConfigElement>();
        ArrayList<IConfigElement> partCategory = new ArrayList<IConfigElement>();

        generalCategory.addAll(new ConfigElement(Config.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements());
        //partCategory.addAll(new ConfigElement(Config.config.getCategory("parts")).getChildElements());

        partCategory.add(new ConfigElement(Config.config.getCategory("parts")));

        configElements.addAll(generalCategory);
        configElements.addAll(partCategory);

        //partCategory.addAll(BetterStorage.globalConfig.getSettings("tile").values());

        //configElements.add(new ConfigElement.CategoryElement("item", "config.betterstorage.category.item", itemCategory));
        //configElements.add(new ConfigElement.CategoryElement("tile", "config.betterstorage.category.tile", blockCategory));
        //configElements.add(new DummyConfigElement.DummyCategoryElement("enchantment", "config.betterstorage.category.enchantment", enchantmentCategory));
        //configElements.addAll(BetterStorage.globalConfig.getSettings("general").values());
    }
}
