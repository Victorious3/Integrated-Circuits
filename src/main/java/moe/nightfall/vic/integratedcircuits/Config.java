package moe.nightfall.vic.integratedcircuits;

import java.io.File;

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
	
	//TODO Config options for enabling/disabling gates.
	//TODO Generalize!
	public static void initialize(File file)
	{
		config = new Configuration(file);
		config.load();

		// General Configuration
		showConfirmMessage = config.get("GENERAL", "showConfirmMessage", true, "Show the CAD's confirm prompt.");
		showStartupMessage = config.getBoolean("showStartupMessage", "GENERAL", true, "Show a message on startup warning your players from the risk they are undertaking by playing with this mod.");
		enablePropertyEdit = config.getBoolean("enablePropertyEdit", "GENERAL", true, "Enable property editing for the circuit peripheral. I don't take any warranty for crashes that might arise because of this.");
		circuitCacheSize = config.getInt("circuitCacheSize", "GENERAL", 20, 0, Integer.MAX_VALUE, "The maximum number of undos that can be used in the CAD");
		enableTracker = config.getBoolean("enableTracker", "GENERAL", true,
			"This setting will make the game visit this URL on startup: https://raw.githubusercontent.com/Victorious3/Integrated-Circuits/master/version.dat\n" +
			"The connection is established over bit.ly for statistics. The data collected is publicly visible on https://bitly.com/1GIaUA6+. Bit.ly will track\n" +
			"your country via your IP address, no other, or personal, information is gathered. I like statistics, and I would like you to keep this setting enabled,\n" +
			"so that I get a better overview of how often my mod is used. Thanks.");

		config.save();
	}
	
	public static void save()
	{
		if(!showConfirmMessage.hasChanged()) return;
		config.save();
	}
}
