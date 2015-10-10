package moe.nightfall.vic.integratedcircuits.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import moe.nightfall.vic.integratedcircuits.Config;
import moe.nightfall.vic.integratedcircuits.Constants;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

public class IntegratedCircuitsGuiConfig extends GuiConfig
{

    public IntegratedCircuitsGuiConfig(GuiScreen parent)
    {
        super(parent,
                getConfigElements(),
                Constants.MOD_ID,
                false,
                false,
                getAbridgedConfigPath(Config.config.toString()),
                StatCollector.translateToLocal("gui.integratedcircuits.config.titleline2"));
    }

    public static List<IConfigElement> getConfigElements()
    {
        List<IConfigElement> configElements = new ArrayList<IConfigElement>();

        IConfigElement generalCategoryElement = new ConfigElement(Config.config.getCategory(Configuration.CATEGORY_GENERAL));
        IConfigElement partsCategoryElement = new ConfigElement(Config.config.getCategory("parts"));
		IConfigElement appearanceCategoryElement = new ConfigElement(Config.config.getCategory("appearance"));

        configElements.addAll(generalCategoryElement.getChildElements());

        configElements.add(partsCategoryElement);
		configElements.add(appearanceCategoryElement);

        return configElements;
    }

    private static Pattern p = Pattern.compile("\r");
    private List lastTooltip;

    @Override
    public void drawToolTip(List tooltip, int x, int y)
    {
        if(lastTooltip != null && lastTooltip == tooltip)
        {
            super.drawToolTip(tooltip, x, y);
            return;
        }
        lastTooltip = tooltip;

        for(int i = 0; i < tooltip.size();i++) {
            tooltip.set(i, p.matcher((String) tooltip.get(i)).replaceAll(""));
        }

        super.drawToolTip(tooltip, x, y);
    }
}
