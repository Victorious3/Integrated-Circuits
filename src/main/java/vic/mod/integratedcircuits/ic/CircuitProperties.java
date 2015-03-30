package vic.mod.integratedcircuits.ic;

import net.minecraft.nbt.NBTTagCompound;
import vic.mod.integratedcircuits.gate.ISocket.EnumConnectionType;

public class CircuitProperties implements Cloneable
{
	private String name = "NO_NAME", author = "unknown";
	private int con;
	
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
		return name;
	}
	
	public String getAuthor()
	{
		return author;
	}
	
	public int getCon()
	{
		return con;
	}
	
	public EnumConnectionType getModeAtSide(int side)
	{
		return EnumConnectionType.values()[con >> (side * 2) & 3];
	}
	
	public int setModeAtSide(int side, EnumConnectionType type)
	{
		int con = this.con;
		con &= ~(3 << (side * 2));
		con |= type.ordinal() << (side * 2);
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
		comp.setString("name", name);
		comp.setString("author", author);
		comp.setInteger("con", con);
		return comp;
	}
}
