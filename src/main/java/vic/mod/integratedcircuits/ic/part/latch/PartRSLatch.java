package vic.mod.integratedcircuits.ic.part.latch;

import java.util.ArrayList;

import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.ic.part.PartCPGate;
import vic.mod.integratedcircuits.misc.Vec2;

//TODO Acts a little bit different then described on the P:R wiki. I'll come back to this.
public class PartRSLatch extends PartCPGate
{	
	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) 
	{
		super.onClick(pos, parent, button, ctrl);
		if(button == 0 && ctrl)
		{
			int state = (getState(pos, parent) & 768) >> 8;
			state++;
			state = state > 3 ? 0 : state;
			setState(pos, parent, getState(pos, parent) & ~768);
			setState(pos, parent, getState(pos, parent) | state << 8);
			notifyNeighbours(pos, parent);
		}
	}
	
	private boolean isMirrored(Vec2 pos, ICircuit parent)
	{
		return (getState(pos, parent) & 512) > 0;
	}
	
	private boolean isSpecial(Vec2 pos, ICircuit parent)
	{
		return (getState(pos, parent) & 256) > 0;
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		updateInput(pos, parent);
		ForgeDirection s2 = toInternal(pos, parent, side);
		if(s2 == ForgeDirection.EAST || s2 == ForgeDirection.WEST) return;
		if(s2 == ForgeDirection.NORTH) setState(pos, parent, getState(pos, parent) | 128);
		else setState(pos, parent, getState(pos, parent) & ~128);
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
			&& b1 && (getState(pos, parent) & 128) != 0) return true;
		if(((s2 == ForgeDirection.WEST && !isMirrored(pos, parent) 
			|| s2 == ForgeDirection.EAST && isMirrored(pos, parent)) 
			|| (s2 == ForgeDirection.SOUTH && isSpecial(pos, parent) 
			&& !getInputFromSide(pos, parent, s3))) 
			&& b1 && (getState(pos, parent) & 128) == 0) return true;
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