package vic.mod.integratedcircuits.ic.part.latch;

import java.util.ArrayList;

import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.ic.part.PartCPGate;
import vic.mod.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import vic.mod.integratedcircuits.misc.PropertyStitcher.IntProperty;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartRSLatch extends PartCPGate
{	
	public final BooleanProperty PROP_OUT = new BooleanProperty(stitcher);
	public final IntProperty PROP_MODE = new IntProperty(stitcher, 3);
	
	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) 
	{
		super.onClick(pos, parent, button, ctrl);
		if(button == 0 && ctrl)
		{
			cycleProperty(pos, parent, PROP_MODE);
			notifyNeighbours(pos, parent);
		}
	}
	
	private boolean isMirrored(Vec2 pos, ICircuit parent)
	{
		return (getProperty(pos, parent, PROP_MODE) & 2) != 0;
	}
	
	private boolean isSpecial(Vec2 pos, ICircuit parent)
	{
		return (getProperty(pos, parent, PROP_MODE) & 1) != 0;
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		updateInput(pos, parent);
		ForgeDirection s2 = toInternal(pos, parent, side);
		if(s2 == ForgeDirection.EAST || s2 == ForgeDirection.WEST) return;
		setProperty(pos, parent, PROP_OUT, s2 == ForgeDirection.NORTH);
		scheduleTick(pos, parent);
		markForUpdate(pos, parent);
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		ForgeDirection s2 = toInternal(pos, parent, side);
		ForgeDirection s3 = toExternal(pos, parent, ForgeDirection.NORTH);
		boolean b1 = !(getInputFromSide(pos, parent, s3) && getInputFromSide(pos, parent, s3.getOpposite()));
		if(((s2 == ForgeDirection.EAST && !isMirrored(pos, parent) 
			|| s2 == ForgeDirection.WEST && isMirrored(pos, parent)) 
			|| (s2 == ForgeDirection.NORTH && isSpecial(pos, parent) 
			&& !getInputFromSide(pos, parent, s3.getOpposite()))) 
			&& b1 && getProperty(pos, parent, PROP_OUT)) return true;
		if(((s2 == ForgeDirection.WEST && !isMirrored(pos, parent) 
			|| s2 == ForgeDirection.EAST && isMirrored(pos, parent)) 
			|| (s2 == ForgeDirection.SOUTH && isSpecial(pos, parent) 
			&& !getInputFromSide(pos, parent, s3))) 
			&& b1 && !getProperty(pos, parent, PROP_OUT)) return true;
		return false;
	}

	@Override
	public ArrayList<String> getInformation(Vec2 pos, ICircuit parent) 
	{
		ArrayList<String> text = super.getInformation(pos, parent);
		text.add("Mode: " + (isSpecial(pos, parent) ? 1 : 0));
		if(isMirrored(pos, parent)) text.add(EnumChatFormatting.ITALIC + "Mirrored");
		return text;
	}
}