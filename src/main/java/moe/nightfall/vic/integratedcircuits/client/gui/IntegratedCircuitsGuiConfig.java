package moe.nightfall.vic.integratedcircuits.client.gui;

import cpw.mods.fml.client.config.*;
import moe.nightfall.vic.integratedcircuits.Config;
import moe.nightfall.vic.integratedcircuits.Constants;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class IntegratedCircuitsGuiConfig extends GuiConfig
{

    public IntegratedCircuitsGuiConfig(GuiScreen parent)
    {
        super(parent,
                getConfigElements(),
                Constants.MOD_ID,
                false,
                false,
                getAbridgedConfigPath(Config.config.toString()));
        titleLine2 = "Configure IntegratedCircuits here"; //TODO add localization here if possible
    }

    public static List<IConfigElement> getConfigElements()
    {
        List<IConfigElement> configElements = new ArrayList<IConfigElement>();

        ConfigElement generalCategoryElement = new ConfigElement(Config.config.getCategory(Configuration.CATEGORY_GENERAL));
        ConfigElement partsCategoryElement = new ConfigElement(Config.config.getCategory("parts"));

        configElements.addAll(generalCategoryElement.getChildElements());

        configElements.add(partsCategoryElement);

        return configElements;
    }

    private static Pattern p = Pattern.compile("\r");

    @Override
    public void drawToolTip(List tooltip, int x, int y)
    {
        for(int i = 0; i < tooltip.size();i++) {
            tooltip.set(i, p.matcher((String) tooltip.get(i)).replaceAll(""));
        }
            //tooltip.set(i, ((String)tooltip.get(i)).replace("\r", "")); }
        super.drawToolTip(tooltip, x, y);
    }
}
