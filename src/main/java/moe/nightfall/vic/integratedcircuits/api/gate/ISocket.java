package moe.nightfall.vic.integratedcircuits.api.gate;

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
		SIMPLE, ANALOG, BUNDLED, NONE;

		public boolean isBundled() {
			return this == BUNDLED;
		}

		public boolean isRedstone() {
			return this == SIMPLE || this == ANALOG;
		}
	}

	/**
	 * Rotates the socket (or gate in the socket)
	 * @return true if successful.
	 */
	boolean rotate();
}
