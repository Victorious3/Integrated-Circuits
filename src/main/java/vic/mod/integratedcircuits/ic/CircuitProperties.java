package vic.mod.integratedcircuits.ic;

import net.minecraft.nbt.NBTTagCompound;

public class CircuitProperties implements Cloneable
{
	private String name, author;
	private int con;
	
	public static final int SIMPLE = 0;
	public static final int BUNDLED = 1;
	public static final int ANALOG = 2;
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setAuthor(String author)
	{
		this.author = author;
	}
	
	public void setCon(int con)
	{
		this.con = con;
	}
	
	public String getName()
	{
		return name != null ? name : "NO_NAME";
	}
	
	public String getAuthor()
	{
		return author != null ? author : "unknown";
	}
	
	public int getCon()
	{
		return con;
	}
	
	public int getModeAtSide(int side)
	{
		return con >> (side * 2) & 3;
	}
	
	public int setModeAtSide(int side, int mode)
	{
		int con = this.con;
		con &= ~(3 << (side * 2));
		con |= mode << (side * 2);
		return con;
	}
	
	public static CircuitProperties readFromNBT(NBTTagCompound comp)
	{
		CircuitProperties properties = new CircuitProperties();
		if(comp.hasKey("name")) properties.name = comp.getString("name");
		if(comp.hasKey("author")) properties.author = comp.getString("author");
		properties.con = comp.getInteger("con");
		return properties;
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound comp)
	{
		if(name != null) comp.setString("name", name);
		if(author != null) comp.setString("author", author);
		comp.setInteger("con", con);
		return comp;
	}
}
