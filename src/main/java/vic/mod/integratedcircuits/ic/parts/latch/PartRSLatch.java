package vic.mod.integratedcircuits.ic.parts.latch;

import java.util.ArrayList;

import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.parts.PartGate;
import vic.mod.integratedcircuits.util.MiscUtils;

//TODO Acts a little bit different then described on the P:R wiki. I'll come back to this.
public class PartRSLatch extends PartGate
{	
	@Override
	public void onClick(int button, boolean ctrl) 
	{
		super.onClick(button, ctrl);
		if(button == 0 && ctrl)
		{
			int state = (getState() & 768) >> 8;
			state++;
			state = state > 3 ? 0 : state;
			setState(getState() & ~768);
			setState(getState() | state << 8);
			notifyNeighbours();
		}
	}
	
	private boolean isMirrored()
	{
		return (getState() & 512) > 0;
	}
	
	private boolean isSpecial()
	{
		return (getState() & 256) > 0;
	}

	@Override
	public void onInputChange(ForgeDirection side) 
	{
		updateInput();
		ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
		if(s2 == ForgeDirection.EAST || s2 == ForgeDirection.WEST) return;
		if(s2 == ForgeDirection.NORTH) setState(getState() | 128);
		else setState(getState() & ~128);
		scheduleTick();
		markForUpdate();
	}

	@Override
	public boolean getOutputToSide(ForgeDirection side) 
	{
		ForgeDirection s2 = MiscUtils.rotn(side, -getRotation());
		ForgeDirection s3 = MiscUtils.rotn(ForgeDirection.NORTH, getRotation());
		boolean b1 = !(getInputFromSide(s3) && getInputFromSide(s3.getOpposite()));
		if(((s2 == ForgeDirection.EAST && !isMirrored() 
			|| s2 == ForgeDirection.WEST && isMirrored()) 
			|| (s2 == ForgeDirection.NORTH && isSpecial() 
			&& !getInputFromSide(s3.getOpposite()))) 
			&& b1 && (getState() & 128) != 0) return true;
		if(((s2 == ForgeDirection.WEST && !isMirrored() 
			|| s2 == ForgeDirection.EAST && isMirrored()) 
			|| (s2 == ForgeDirection.SOUTH && isSpecial() 
			&& !getInputFromSide(s3))) 
			&& b1 && (getState() & 128) == 0) return true;
		return false;
	}

	@Override
	public ArrayList<String> getInformation() 
	{
		ArrayList<String> text = super.getInformation();
		text.add("Mode: " + (isSpecial() ? 1 : 0));
		if(isMirrored()) text.add(EnumChatFormatting.ITALIC + "Mirrored");
		return text;
	}
}