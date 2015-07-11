package moe.nightfall.vic.integratedcircuits.client.gui;

import moe.nightfall.vic.integratedcircuits.ContainerAssembler;
import moe.nightfall.vic.integratedcircuits.ContainerPCBLayout;
import moe.nightfall.vic.integratedcircuits.client.gui.cad.GuiCAD;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityAssembler;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityCAD;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class IntegratedCircuitsGuiHandler implements IGuiHandler {
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (id) {
			case 0:
				/*return new Container() {

					@Override
					public boolean canInteractWith(EntityPlayer p_75145_1_) {
						// TODO Auto-generated method stub
						return true;
					}
				};*/
				return new ContainerPCBLayout((TileEntityCAD) world.getTileEntity(x, y, z));
			case 1:
				return new ContainerAssembler(player.inventory, (TileEntityAssembler) world.getTileEntity(x, y, z));
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (id) {
			case 0:
				// return new TestGUI();
				return new GuiCAD(new ContainerPCBLayout((TileEntityCAD) world.getTileEntity(x, y, z)));
			case 1:
				return new GuiAssembler(new ContainerAssembler(player.inventory, (TileEntityAssembler) world.getTileEntity(
						x, y, z)));
		}
		return null;
	}

	// Bunch of test code
	/*private static class TestGUI extends GuiScreen {

		private GuiTextArea textArea = new GuiTextArea(10, 10)
			.setText("AAAAAAAA\nBBBBBBBB\nCCCCCCCC\nDDDDDDDD\nEEEEEEEE")
			.setBackgroundColor(0xFFFFFFFF)
			.setBorderColor(0xFF000000)
			.setTextColor(0xFF000000);

		@Override
		protected void mouseClicked(int p_73864_1_, int p_73864_2_, int p_73864_3_) {
			textArea.onMouseDown(p_73864_1_, p_73864_2_, p_73864_3_);
		}

		@Override
		protected void mouseMovedOrUp(int p_146286_1_, int p_146286_2_, int p_146286_3_) {
			if (p_146286_3_ == 0) {
				textArea.onMouseUp(p_146286_1_, p_146286_2_, 0);
			}
		}

		@Override
		protected void mouseClickMove(int p_146273_1_, int p_146273_2_, int p_146273_3_, long p_146273_4_) {
			textArea.onMouseDragged(p_146273_1_, p_146273_2_);
		}

		@Override
		public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
			try {
				textArea.render(p_73863_1_, p_73863_2_);
			} catch (Exception e) {
				e.printStackTrace();
				mc.displayGuiScreen(null);
				mc.setIngameFocus();
			}
		}

		@Override
		protected void keyTyped(char p_73869_1_, int p_73869_2_) {
			super.keyTyped(p_73869_1_, p_73869_2_);
			textArea.onKeyTyped(p_73869_2_, p_73869_1_);
		}

		@Override
		public boolean doesGuiPauseGame() {
			return false;
		}
	}*/
}