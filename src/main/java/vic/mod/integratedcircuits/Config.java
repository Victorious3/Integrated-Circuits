package vic.mod.integratedcircuits;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public abstract class Config 
{
	public static Configuration config;
	
	public static Property showConfirmMessage;
	
	//TODO Generalize!
	public static void initialize(File file)
	{
		config = new Configuration(file);
		config.load();
		
		showConfirmMessage = config.get("showConfirmMessage", "GENERAL", true, "Show the CAD's confirm promt.");
		
		config.save();
	}
	
	public static void save()
	{
		if(!showConfirmMessage.hasChanged()) return;
		config.save();
	}
}
