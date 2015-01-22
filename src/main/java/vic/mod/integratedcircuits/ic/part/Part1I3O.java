package vic.mod.integratedcircuits.ic.part;

import java.util.ArrayList;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.PropertyStitcher.IntProperty;
import vic.mod.integratedcircuits.misc.Vec2;

public abstract class Part1I3O extends PartSimpleGate
{
	public final IntProperty PROP_CONNECTORS = new IntProperty("CONNECTORS", stitcher, 6);
	
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
		if(s2 == ForgeDirection.SOUTH) return true;
		int i = getProperty(pos, parent, PROP_CONNECTORS);
		if(s2 == ForgeDirection.EAST && (i == 3 || i == 4 || i == 5)) return false;
		if(s2 == ForgeDirection.NORTH && (i == 2 || i == 4 || i == 6)) return false;
		if(s2 == ForgeDirection.WEST && (i == 1 || i == 5 || i == 6)) return false;
		return true;
	}
	
	@Override
	protected boolean hasOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection fd) 
	{
		return fd != ForgeDirection.SOUTH;
	}
	
	@Override
	public ArrayList<String> getInformation(Vec2 pos, ICircuit parent, boolean edit, boolean ctrlDown) 
	{
		ArrayList<String> text = super.getInformation(pos, parent, edit, ctrlDown);
		if(edit && ctrlDown) text.add(I18n.format("gui.integratedcircuits.cad.mode"));
		return text;
	}
}