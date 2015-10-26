package moe.nightfall.vic.integratedcircuits.api.gate;

import java.util.ArrayList;
import java.util.List;

import moe.nightfall.vic.integratedcircuits.api.gate.ISocketBridge.ISocketBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.vec.BlockCoord;

/**
 * Contains all methods that have to be called by an {@link ISocketWrapper} and
 * documentation.
 * 
 * @author Vic Nightfall
 */
public interface ISocket extends ISocketBase {
	void update();

	void readFromNBT(NBTTagCompound compound);

	void writeToNBT(NBTTagCompound compound);

	void writeDesc(NBTTagCompound compound);

	void readDesc(NBTTagCompound compound);

	void read(MCDataInput packet);

	/**
	 * Only has to be called when the orientation needs to be set automatically,
	 * relative to the player's position and look vector. If you want to set the
	 * orientation manually, use the two methods {@link #setSide(int)} and
	 * {@link #setRotation(int)}
	 * 
	 * @param player
	 * @param pos
	 * @param side
	 * @param stack
	 */
	void preparePlacement(EntityPlayer player, BlockCoord pos, int side, ItemStack stack);

	boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack stack);

	void onNeighborChanged();

	void addDrops(List<ItemStack> list);
	
	/**
	 * Called when a gate (circuit) is placed onto the socket.
	 * This is to allow a socket, if it wants, to allow a player to place something on it, without using it up.
	 * A creative mode check should happen in other code, presumably the code calling this method.
	 * 
	 * This is also used to determine if the gate should be dropped when removed.
	 * 
	 * @return If the gate is to be used up (removed from inventory)
	 */
	boolean usesUpPlacedGate();

	ItemStack pickItem(MovingObjectPosition target);

	void scheduledTick();

	void onAdded();

	void onMoved();

	void onRemoved();

	enum EnumConnectionType {
		SIMPLE(Size.SINGLE), ANALOG(Size.SIXTEEN), BUNDLED(Size.SIXTEEN), NONE(Size.NONE);

		EnumConnectionType(Size size) { this.size = size; }
		public final Size size;
		/** Possible sizes for the connection type. The order is important, so checks can be made using them. **/
		public enum Size { NONE, AVAILABLE, SINGLE, SIXTEEN }

		public boolean isBundled() { return this == BUNDLED; }
		public boolean isRedstone() { return this == SIMPLE || this == ANALOG; }
		public boolean isDisabled() { return this.size == Size.NONE; }
		public boolean isSingle() { return this.size == Size.SINGLE; }
		public boolean isFull() { return this.size == Size.AVAILABLE; }
		public boolean isAnalog() { return this == ANALOG; }
		
		/** Get single character (as a string) that uniquely identifies this connection type. **/
		public String singleID() { return Character.toString(singleCharID()); }
		/** Get single character (as a character) that uniquely identifies this connection type. **/
		public char singleCharID() { return name().charAt(0); }
		
		/** Get a List of supported connection types based on the maximum size supported. **/
		public static List<EnumConnectionType> getSupportedList(Size size) {
			ArrayList<EnumConnectionType> list = new ArrayList<EnumConnectionType>();
			for (EnumConnectionType connectionType : EnumConnectionType.values()) {
				if (connectionType.size.ordinal() <= size.ordinal()) list.add(connectionType);
			}
			return list;
		}
	}

	/**
	 * Rotates the socket (or gate in the socket)
	 * @return true if successful.
	 */
	boolean rotate();
}
