package moe.nightfall.vic.integratedcircuits.api.gate;

import moe.nightfall.vic.integratedcircuits.api.gate.ISocket.EnumConnectionType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ISocketBridge {

	public void markRender();

	public MCDataOutput getWriteStream(int disc);

	public World getWorld();

	public void notifyBlocksAndChanges();

	public void notifyPartChange();

	public BlockCoord getPos();

	public void destroy();

	public void updateInput();

	public int updateRedstoneInput(int side);

	public byte[] updateBundledInput(int side);

	public void scheduleTick(int delay);

	public void sendDescription();

	public static interface ISocketBase extends ISocketBridge {

		public void setGate(IGate gate);

		public void setGate(ItemStack stack, EntityPlayer player);

		public IGate getGate();

		public ISocketWrapper getWrapper();

		public byte getOrientation();

		public int getSide();

		public int getSideRel(int side);

		public void setSide(int side);

		public int getRotation();

		public int getRotationAbs(int rel);

		public int getRotationRel(int abs);

		public void setRotation(int rot);

		@SideOnly(Side.CLIENT)
		public byte getRedstoneIO();

		public byte[][] getInput();

		public byte[][] getOutput();

		public void setInput(byte[][] input);

		public void setOutput(byte[][] output);

		public byte getRedstoneInput(int side);

		public byte getBundledInput(int side, int frequency);

		public byte getRedstoneOutput(int side);

		public byte getBundledOutput(int side, int frequency);

		public void setInput(int side, int frequency, byte input);

		public void setOutput(int side, int frequency, byte output);

		public void resetInput();

		public void resetOutput();

		public EnumConnectionType getConnectionTypeAtSide(int side);

		/**
		 * Use this to store additional data on this socket, meant for use with
		 * {@link GateIOProvider}.
		 * 
		 * @param key
		 * @return T
		 */
		public <T> T get(String key);

		public void put(String key, Object o);

		public void updateInputPre();

		public void updateInputPost();
	}
}
