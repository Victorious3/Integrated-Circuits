package vic.mod.integratedcircuits;

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
	
	//TODO Config options for enabling/disabling gates.
	//TODO Generalize!
	public static void initialize(File file)
	{
		config = new Configuration(file);
		config.load();
		
		showConfirmMessage = config.get("GENERAL", "showConfirmMessage", true, "Show the CAD's confirm promt.");
		showStartupMessage = config.getBoolean("showStartupMessage", "GENERAL", true, "Show a message on startup warning your players from the risk they are undertaking by playing with this mod.");
		enablePropertyEdit = config.getBoolean("enablePropertyEdit", "GENERAL", true, "Enable property editing for the circuit peripheral. I don't take any warranty for crashes that might arise because of this.");
		
		config.save();
	}
	
	public static void save()
	{
		if(!showConfirmMessage.hasChanged()) return;
		config.save();
	}
}
