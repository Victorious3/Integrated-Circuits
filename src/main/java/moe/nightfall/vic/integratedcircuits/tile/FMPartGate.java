package moe.nightfall.vic.integratedcircuits.tile;

import java.util.Arrays;
import java.util.List;

import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket.EnumConnectionType;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketWrapper;
import moe.nightfall.vic.integratedcircuits.compat.BPDevice;
import moe.nightfall.vic.integratedcircuits.gate.Socket;
import moe.nightfall.vic.integratedcircuits.proxy.ClientProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.IFaceRedstonePart;
import codechicken.multipart.IRedstonePart;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.MultipartHelper;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.RedstoneInteractions;
import codechicken.multipart.TFacePart;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class FMPartGate extends JCuboidPart implements JNormalOcclusion, TFacePart, IFaceRedstonePart, ISocketWrapper {
	private ISocket socket = new Socket(this);

	// TODO Re-implement
	private BPDevice bpDevice;

	@Override
	public String getType() {
		return Constants.MOD_ID + ".socket_fmp";
	}

	@Override
	public void load(NBTTagCompound tag) {
		socket.readFromNBT(tag);
	}

	@Override
	public void save(NBTTagCompound tag) {
		socket.writeToNBT(tag);
	}

	@Override
	public void readDesc(MCDataInput packet) {
		socket.readDesc(packet.readNBTTagCompound());
	}

	@Override
	public void writeDesc(MCDataOutput packet) {
		NBTTagCompound compound = new NBTTagCompound();
		socket.writeDesc(compound);
		packet.writeNBTTagCompound(compound);
	}

	@Override
	public void read(MCDataInput packet) {
		socket.read(packet);
	}

	@Override
	public MCDataOutput getWriteStream(int disc) {
		return getWriteStream().writeByte(disc);
	}

	@Override
	public Cuboid6 getBounds() {
		return Socket.box.copy().apply(Socket.getRotationTransformation(socket));
	}

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes() {
		return Arrays.asList(getBounds());
	}

	@Override
	public boolean occlusionTest(TMultiPart npart) {
		return NormalOcclusionTest.apply(this, npart);
	}

	@Override
	public int getSlotMask() {
		return 1 << socket.getSide();
	}

	@Override
	public int redstoneConductionMap() {
		return 0;
	}

	@Override
	public void update() {
		socket.update();
	}

	@Override
	public void scheduledTick() {
		socket.scheduledTick();
	}

	@Override
	public boolean solid(int arg0) {
		return false;
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item) {
		return socket.activate(player, hit, item);
	}

	@Override
	public void onAdded() {
		socket.onAdded();
	}

	@Override
	public void onRemoved() {
		socket.onRemoved();
	}

	@Override
	public void onMoved() {
		socket.onMoved();
	}

	@Override
	public Iterable<ItemStack> getDrops() {
		List<ItemStack> list = Lists.newArrayList();
		socket.addDrops(list);
		list.add(new ItemStack(IntegratedCircuits.itemSocketFMP));
		return list;
	}

	@Override
	public ItemStack pickItem(MovingObjectPosition hit) {
		return socket.pickItem(hit);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderStatic(Vector3 pos, int pass) {
		if (pass == 0) {
			CCRenderState.setBrightness(getWorld(), x(), y(), z());
			ClientProxy.socketRendererFMP.prepare(socket);
			ClientProxy.socketRendererFMP.renderStatic(new Translation(pos), socket.getOrientation());
			return true;
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float frame, int pass) {
		if (pass == 0) {
			TextureUtils.bindAtlas(0);
			ClientProxy.socketRendererFMP.prepareDynamic(socket, frame);
			ClientProxy.socketRendererFMP.renderDynamic(new Translation(pos));
		}
	}

	@Override
	public void onNeighborChanged() {
		socket.onNeighborChanged();
	}

	@Override
	public void onPartChanged(TMultiPart part) {
		socket.onNeighborChanged();
	}

	@Override
	public final boolean canConnectRedstone(int arg0) {
		if ((arg0 & 6) == (socket.getSide() & 6))
			return false;
		return socket.getConnectionTypeAtSide(socket.getSideRel(arg0)).isRedstone();
	}

	@Override
	public int strongPowerLevel(int arg0) {
		if ((arg0 & 6) == (socket.getSide() & 6))
			return 0;
		int rot = socket.getSideRel(arg0);
		EnumConnectionType type = socket.getConnectionTypeAtSide(rot);
		if (type.isRedstone())
			return 0;
		return socket.getRedstoneOutput(rot);
	}

	@Override
	public int weakPowerLevel(int arg0) {
		return strongPowerLevel(arg0);
	}

	@Override
	public int getFace() {
		return socket.getSide();
	}

	@Override
	public void markRender() {
		if (tile() != null)
			tile().markRender();
	}

	@Override
	public World getWorld() {
		return world();
	}

	@Override
	public void notifyPartChange() {
		tile().notifyPartChange(this);
	}

	@Override
	public void notifyBlocksAndChanges() {
		tile().markDirty();
		notifyPartChange();
		tile().notifyNeighborChange(socket.getSide());
	}

	@Override
	public BlockCoord getPos() {
		return new BlockCoord(x(), y(), z());
	}

	@Override
	public void destroy() {
		tile().remPart(this);
	}

	@Override
	public byte[] updateBundledInput(int side) {
		return IntegratedCircuitsAPI.updateBundledInput(getSocket(), side);
	}

	@Override
	public int updateRedstoneInput(int side) {
		// FMP part signal
		int rotation = socket.getRotationAbs(side);
		int face = socket.getSide();
		int abs = Rotation.rotateSide(face, rotation);

		int power = RedstoneInteractions.getPowerTo(this, abs);
		if (power != 0)
			return power;

		// Internal signal
		TMultiPart tp = ((TileMultipart) ((FMPartGate) socket.getWrapper()).getTile()).partMap(abs);
		if (tp instanceof IRedstonePart) {
			IRedstonePart rp = (IRedstonePart) tp;
			power = Math.max(rp.strongPowerLevel(face), rp.weakPowerLevel(face)) << 4;
			if (power != 0)
				return power;
		}

		return IntegratedCircuitsAPI.updateRedstoneInput(getSocket(), side);
	}

	@Override
	public void sendDescription() {
		MultipartHelper.sendDescPacket(getWorld(), getTile());
	}

	@Override
	public ISocket getSocket() {
		return socket;
	}

	@Override
	public void updateInput() {
		socket.updateInput();
	}
}
