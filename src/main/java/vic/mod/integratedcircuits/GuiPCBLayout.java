package vic.mod.integratedcircuits;

import net.minecraft.client.gui.inventory.GuiContainer;

import org.lwjgl.opengl.GL11;

public class GuiPCBLayout extends GuiContainer
{
	public GuiPCBLayout(ContainerPCBLayout container) 
	{
		super(container);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int x, int y) 
	{		
		int[][][] matrix = ((ICircuit)inventorySlots).getMatrix();
		
		matrix[0][1][1] = 1;
		matrix[0][2][1] = 1;
		matrix[0][3][1] = 1;
		
		matrix[0][4][1] = 2;
		
		matrix[0][4][2] = 1;
		matrix[0][3][3] = 1;
		matrix[0][2][3] = 1;
		matrix[0][4][3] = 1;
		matrix[0][5][3] = 1;
		
		matrix[0][6][3] = 3;
		matrix[1][6][3] = matrix[1][5][3] | 0 << 4;
		
		matrix[0][6][2] = 1;
		matrix[0][6][1] = 1;
		matrix[0][6][4] = 2;
		matrix[0][7][3] = 2;
		
		SubLogicPart.getPart(4, 1, (ICircuit)inventorySlots).onPlaced();
		SubLogicPart.getPart(6, 4, (ICircuit)inventorySlots).onPlaced();
		SubLogicPart.getPart(7, 3, (ICircuit)inventorySlots).onPlaced();

		SubLogicPart.getPart(6, 3, (ICircuit)inventorySlots).onUpdateTick();
		
		GL11.glScalef(0.5F, 0.5F, 1F);
		
		for(int x2 = 0; x2 < matrix[0].length; x2++)
		{
			int[] i1 = matrix[0][x2];
			for(int y2 = 0; y2 < i1.length; y2++) SubLogicPartRenderer.renderPart(SubLogicPart.getPart(x2, y2, (ICircuit)inventorySlots), this);
		}
	}
}
