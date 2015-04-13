package moe.nightfall.vic.integratedcircuits;

import java.io.File;

import moe.nightfall.vic.integratedcircuits.ic.CircuitPart;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

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

	//TODO Generalize!
	public static void preInitialize(File file)
	{
		config = new Configuration(file);
		config.load();

		showConfirmMessage = config.get("GENERAL", "showConfirmMessage", true, "Show the CAD's confirm prompt.");
		showStartupMessage = config.getBoolean("showStartupMessage", "GENERAL", true, "Show a message on startup warning your players from the risk they are undertaking by playing with this mod.");
		enablePropertyEdit = config.getBoolean("enablePropertyEdit", "GENERAL", true, "Enable property editing for the circuit peripheral. I don't take any warranty for crashes that might arise because of this.");
		circuitCacheSize = config.getInt("circuitCacheSize", "GENERAL", 20, 0, Integer.MAX_VALUE, "The maximum number of undos that can be used in the CAD");
		enableTooltips = config.getBoolean("enableTooltips", "GENERAL", true, "Enable help tooltips. Recommended to be enabled unless you are familiar with the mod.");
		enableTracker = config.getBoolean("enableTracker", "GENERAL", true,
			"This setting will make the game visit this URL on startup: https://raw.githubusercontent.com/Victorious3/Integrated-Circuits/master/version.dat\n" +
			"The connection is established over bit.ly for statistics. The data collected is publicly visible on https://bitly.com/1GIaUA6+. Bit.ly will track\n" +
			"your country via your IP address, no other, or personal, information is gathered. I like statistics, and I would like you to keep this setting enabled,\n" +
			"so that I get a better overview of how often my mod is used. Thanks.\n");

		config.addCustomCategoryComment("PARTS",
			"Enables / Disables circuit parts.\n" +
			"If you disable ANYTHING, ALL circuits with that part will BREAK!\n" +
			"Circuits broken in this way will not be fixed when the part is re-enabled.\n" +
			"The only way to \"fix\" the circuits is to edit the blueprint in the CAD to put the part back,\n" +
			"and then remake and replace the circuits.");
	}

	public static void postInitialize() {
		config.save();
	}
	
	public static void save()
	{
		if(!showConfirmMessage.hasChanged()) return;
		config.save();
	}
}
