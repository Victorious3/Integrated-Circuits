package moe.nightfall.vic.integratedcircuits.ic.part;

import java.util.ArrayList;

import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class Part3I1O extends PartSimpleGate
{
	public final IntProperty PROP_CONNECTORS = new IntProperty("CONNECTORS", stitcher, 3);
	
	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) 
	{
		if(button == 0 && ctrl)
			cycleProperty(pos, parent, PROP_CONNECTORS);
		super.onClick(pos, parent, button, ctrl);
	}
	
	@Override
	public boolean canConnectToSide(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		ForgeDirection s2 = toInternal(pos, parent, side);
		if(s2 == ForgeDirection.NORTH) return true;
		int i = getProperty(pos, parent, PROP_CONNECTORS);
		if(s2 == ForgeDirection.EAST && i == 1) return false;
		if(s2 == ForgeDirection.SOUTH && i == 2) return false;
		if(s2 == ForgeDirection.WEST && i == 3) return false;
		return true;
	}

	@Override
	protected boolean hasOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection fd) 
	{
		return fd == ForgeDirection.NORTH;
	}
	
	@Override
	public ArrayList<String> getInformation(Vec2 pos, ICircuit parent, boolean edit, boolean ctrlDown) 
	{
		ArrayList<String> text = super.getInformation(pos, parent, edit, ctrlDown);
		if(edit && ctrlDown) text.add(I18n.format("gui.integratedcircuits.cad.mode"));
		return text;
	}
}