package vic.mod.integratedcircuits.client;

import net.minecraft.item.ItemStack;
import vic.mod.integratedcircuits.client.model.ModelBase;
import vic.mod.integratedcircuits.gate.IGate;
import vic.mod.integratedcircuits.gate.ISocket;
import codechicken.lib.vec.Transformation;

public class SocketRenderer extends PartRenderer<ISocket>
{	
	private IGate part;
	
	public SocketRenderer(String iconName)
	{
		models.add(new ModelBase(iconName));
	}

	public void prepare(ISocket part) 
	{
		this.part = part.getGate();
	}
	
	@Override
	public void prepareInv(ItemStack stack)
	{
		this.part = null;
	}

	public void prepareDynamic(ISocket part, float partialTicks) 
	{
		this.part = part.getGate();
	}
	
	public void renderStatic(Transformation t, int orient)
	{
		super.renderStatic(t, orient);
		if(part != null) part.getRenderer().renderStatic(t, orient);
	}
	
	public void renderDynamic(Transformation t) 
	{
		if(part != null) part.getRenderer().renderDynamic(t);
	}
}
