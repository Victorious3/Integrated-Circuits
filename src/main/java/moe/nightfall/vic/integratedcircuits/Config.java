package moe.nightfall.vic.integratedcircuits;

import java.io.File;
import java.util.Set;

import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class Config
{
	private Config() {}

	public static Configuration config;

	public static Property showConfirmMessage;
	public static boolean showStartupMessage;
	public static boolean enablePropertyEdit;
	public static boolean enableTracker;
	public static int circuitCacheSize;
	public static boolean enableTooltips;
	public static int sevenSegmentMaxDigits;

	//TODO Generalize!
	public static void preInitialize(File file)
	{
		config = new Configuration(file);
		config.load();

		loadValues();
		
		FMLCommonHandler.instance().bus().register(new ChangeHandler());
	}

	//TODO refactor into Properties ?
	public static void loadValues()
	{
		showConfirmMessage = config.get("GENERAL", "showConfirmMessage", true);
		showStartupMessage = config.getBoolean("showStartupMessage", "GENERAL", true, "");
		enablePropertyEdit = config.getBoolean("enablePropertyEdit", "GENERAL", true, "");
		circuitCacheSize = config.getInt("circuitCacheSize", "GENERAL", 20, 0, Integer.MAX_VALUE, "");
		enableTooltips = config.getBoolean("enableTooltips", "GENERAL", true, "");
		enableTracker = config.getBoolean("enableTracker", "GENERAL", true, "");
		sevenSegmentMaxDigits = config.getInt("sevenSegmentMaxDigits", "GENERAL", 16, 1, 16, "");

		config.setCategoryRequiresMcRestart("PARTS", true);
	}

	public static void postInitialize() {
		loadlangKeys();
		loadComments();
		config.save();
	}

	public static void save()
	{
		if(!showConfirmMessage.hasChanged()) return;
		config.save();
	}

	/** Reloads the config values upon change */
	public static class ChangeHandler {
		@SubscribeEvent
		public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
			if(event.modID.equals(Constants.MOD_ID))
			{
				loadValues();
				loadComments();
				config.save();
			}
		}
	}

	private static void loadlangKeys() {
		Set<String> categories = Config.config.getCategoryNames();
		for(String category: categories)
		{
			for(Property p : (Config.config.getCategory(category).setLanguageKey("config.integratedcircuits.category.l" + category).values()))
			{
				addLanguageKeyOrRedirect(p, "config.integratedcircuits." + category + "." + p.getName());
			}
		}
	}

	private static void addLanguageKeyOrRedirect(Property p, String languageKey){
		String name = StatCollector.translateToLocal(languageKey);
		if(name != null && !name.equals(languageKey)) p.setLanguageKey(languageKey);
		else
		{
			String redirectKey = languageKey.replace(p.getName(), "redirect");
			String redirectValue = StatCollector.translateToLocal(redirectKey);
			if(redirectValue != null && !redirectValue.equals(redirectKey))
			{
				redirectValue = redirectValue.replace("%s", p.getName().toLowerCase());
				addLanguageKeyOrRedirect(p, redirectValue);
			}
			else p.setLanguageKey(languageKey);
		}
	}

	private static void loadComments() {
		Set<String> categories = Config.config.getCategoryNames();
		for(String category: categories)
		{
			String categoryComment = MiscUtils.translateFormattedOrNUll("config.integratedcircuits.category." + category + ".tooltip");
			if(categoryComment != null && !categoryComment.isEmpty()) config.addCustomCategoryComment(category, categoryComment.replace("\\n", "\n").replaceAll("\r", ""));

			for(Property p : config.getCategory(category).values())
			{
				String langKey = p.getLanguageKey();
				if(langKey.equals(p.getName()))langKey = "config.integratedcircuits." + category + "." + p.getName().toLowerCase();
				String comment = MiscUtils.translateFormattedOrNUll(langKey + ".tooltip");
				if(comment != null && !comment.isEmpty()) p.comment= comment.replace("\\n", "\n").replaceAll("\r", "") + " [default: " + p.getDefault() + "]";
				else
				{
					p.comment = "[default: " + p.getDefault() + "]";
					if(IntegratedCircuits.developmentEnvironment) p.comment += " \n[debug] missing description in " + langKey + ".tooltip ";
				}
			}
		}
	}
}
