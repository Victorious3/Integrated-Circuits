package moe.nightfall.vic.integratedcircuits.net;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import codechicken.lib.data.MCDataOutputWrapper;

public class MCDataOutputImpl extends MCDataOutputWrapper
{
	private ByteArrayOutputStream out;
	
	public MCDataOutputImpl(ByteArrayOutputStream out) 
	{
		super(new DataOutputStream(out));
		this.out = out;
	}
	
	public byte[] toByteArray()
	{
		return out.toByteArray();
	}
}
