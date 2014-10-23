package vic.mod.integratedcircuits;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public abstract class Config 
{
	public static Configuration config;
	
	public static boolean showConfirmMessage;
	
	public static void initialize(File file)
	{
		config = new Configuration(file);
		config.load();
		
		showConfirmMessage = config.getBoolean("showConfirmMessage", "GENERAL", true, "Show the CAD's confirm promt.");
		
		config.save();
	}
	
	public static void save()
	{
		config.save();
	}
}
