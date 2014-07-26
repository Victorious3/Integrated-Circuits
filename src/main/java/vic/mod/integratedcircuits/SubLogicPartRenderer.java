package vic.mod.integratedcircuits;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.SubLogicPart.PartWire;

public class SubLogicPartRenderer 
{
	public static void renderPart(SubLogicPart part)
	{
		GL11.glTranslatef(part.getX(), part.getY(), 0);
		Class<? extends SubLogicPart> clazz = part.getClass();
		if(clazz == SubLogicPart.PartWire.class) renderPartWire((PartWire)part);
	}
	
	public static void renderPartWire(PartWire wire)
	{
		
	}
}
