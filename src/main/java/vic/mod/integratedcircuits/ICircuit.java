package vic.mod.integratedcircuits;

import net.minecraftforge.common.util.ForgeDirection;

public interface ICircuit 
{
	public int[][][] getMatrix();
	
	public void setMatrix(int[][][] matrix);
	
	public boolean getInputFromSide(ForgeDirection dir, int frequency);
	
	public void setOutputToSide(ForgeDirection dir, int frequency, boolean output);
	
	public void scheduleTick(int x, int y);
}
