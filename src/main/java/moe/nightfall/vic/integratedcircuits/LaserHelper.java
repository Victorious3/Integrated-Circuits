package moe.nightfall.vic.integratedcircuits;

import moe.nightfall.vic.integratedcircuits.cp.CircuitPart;
import moe.nightfall.vic.integratedcircuits.misc.CraftingAmount;
import moe.nightfall.vic.integratedcircuits.misc.ItemAmount;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.net.PacketAssemblerChangeLaser;
import moe.nightfall.vic.integratedcircuits.net.PacketAssemblerUpdate;
import moe.nightfall.vic.integratedcircuits.net.PacketAssemblerUpdateInsufficient;
import moe.nightfall.vic.integratedcircuits.proxy.ClientProxy;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityAssembler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LaserHelper {
	private Laser[] lasers = new Laser[4];
	private TileEntityAssembler te;
	public boolean isRunning;
	public int offset;
	public int position;

	public LaserHelper(TileEntityAssembler te, int offset) {
		this.te = te;
		this.offset = offset;
	}

	public int getPosition() {
		return position;
	}

	public Laser getLaser(int id) {
		return lasers[id];
	}

	public int getLaserAmount() {
		int r = 0;
		for (int i = 0; i < 4; i++)
			if (lasers[i] != null)
				r++;
		return r;
	}

	@SideOnly(Side.CLIENT)
	public void updateStatus() {
		isRunning = false;
		for (Laser laser : lasers) {
			if (laser != null && laser.isRunning) {
				isRunning = true;
				return;
			}
		}
	}

	public void createLaser(int id, ItemStack laser) {
		if (laser == null)
			lasers[id] = null;
		else
			lasers[id] = new Laser(te, id);
		te.contents[offset + id] = laser;
		if (MiscUtils.isServer() && te.getWorldObj() != null)
			CommonProxy.networkWrapper.sendToDimension(new PacketAssemblerChangeLaser(te.xCoord, te.yCoord, te.zCoord,
					id, laser), te.getWorldObj().provider.dimensionId);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		NBTTagList lasers = new NBTTagList();
		for (int i = 0; i < 4; i++) {
			NBTTagCompound comp = new NBTTagCompound();
			if (getLaser(i) != null)
				getLaser(i).writeToNBT(comp);
			lasers.appendTag(comp);
		}
		tag.setTag("lasers", lasers);
		tag.setBoolean("isRunning", isRunning);
		return tag;
	}

	public void readFromNBT(NBTTagCompound tag) {
		NBTTagList lasers = tag.getTagList("lasers", NBT.TAG_COMPOUND);
		for (int i = 0; i < 4; i++) {
			NBTTagCompound comp = lasers.getCompoundTagAt(i);
			if (comp.hasNoTags())
				continue;
			ItemStack stack = te.contents[i + offset];
			createLaser(i, stack);
			getLaser(i).readFromNBT(comp);
		}
		isRunning = tag.getBoolean("isRunning");
	}

	public void reload() {
		for (Laser laser : lasers)
			if (laser != null)
				laser.reload();
	}

	public void reset() {
		isRunning = false;
		position = 0;
		for (Laser laser : lasers) {
			if (laser != null) {
				laser.isRunning = false;
				laser.reset();
			}
		}
	}

	public void start() {
		te.excMatrix = new boolean[te.size][te.size];
		isRunning = true;
		for (Laser laser : lasers)
			if (laser != null)
				laser.start();
	}

	public void update() {
		if (isRunning) {
			boolean b = false;
			for (int i = 0; i < 4; i++) {
				Laser laser = getLaser(i);
				if (laser == null)
					continue;
				laser.update(0);
				if (laser.isRunning)
					b = true;
				if (laser.canUpdate())
					laser.findNext();
			}
			if (!b) {
				isRunning = false;
				te.onCircuitFinished();
			}
		}
	}

	public static class Laser {
		public int x, y, id;
		public float iX, iY, iZ, length;
		private float rotSpeed = 0.4F, laserSpeed = 1F; // 75F
		private float lastAY, lastAZ, aY, aZ, rotTimeAZ, rotTimeAY;
		private TileEntityAssembler te;
		public boolean isActive = true, isRunning = false;
		private int lastModified;
		private ForgeDirection direction;
		private int step, max, turn;

		private Laser(TileEntityAssembler te, int id) {
			this.te = te;
			this.id = id;
			if (MiscUtils.isClient())
				lastModified = ClientProxy.clientTicks;
			else {
				lastModified = CommonProxy.serverTicks;
				reset();
			}
		}

		public void readFromNBT(NBTTagCompound tag) {
			isActive = tag.getBoolean("isActive");
			isRunning = tag.getBoolean("isRunning");
			x = tag.getInteger("x");
			y = tag.getInteger("y");
			step = tag.getInteger("step");
			max = tag.getInteger("max");
			turn = tag.getInteger("turn");
			direction = ForgeDirection.getOrientation(tag.getInteger("direction"));
		}

		public NBTTagCompound writeToNBT(NBTTagCompound tag) {
			tag.setBoolean("isActive", isActive);
			tag.setBoolean("isRunning", isRunning);
			tag.setInteger("x", x);
			tag.setInteger("y", y);
			tag.setInteger("step", step);
			tag.setInteger("max", max);
			tag.setInteger("turn", turn);
			tag.setInteger("direction", direction.ordinal());
			return tag;
		}

		private void reload() {
			float w2 = 0;
			lastAY = aY;
			lastAZ = aZ;
			iY = aY;
			iZ = aZ;

			if (te.refMatrix != null && isRunning) {
				float x2 = x + 0.5F;
				float y2 = y + 0.5F;

				if (id == 3 || id == 1) {
					x2 = y + 0.5F;
					y2 = x + 0.5F;
				}

				if (id == 3 || id == 0)
					x2 = te.size - x2;
				if (id == 1 || id == 0)
					y2 = te.size - y2;

				float w1 = (10 / 16F * (x2 / (float) te.size))
						+ (3 / 16F - (0.5F - (float) Math.sin(Math.PI / 4D) * 0.5F));
				float h1 = (10 / 16F * (y2 / (float) te.size))
						+ (3 / 16F - (0.5F - (float) Math.sin(Math.PI / 4D) * 0.5F));
				aZ = (float) Math.atan(w1 / h1);
				w2 = (float) (w1 / Math.sin(aZ));
				aZ = (float) Math.toDegrees(aZ) - 45F;
				aY = 90F - (float) Math.toDegrees(Math.atan(w2 / (6 / 16F - 2 / 80F)));
				length = w2 / (float) Math.sin(Math.toRadians(90F - aY));
			} else {
				aY = 0;
				aZ = 0;
			}
		}

		public float getInterpolated(float partialTicks, float f1, float f2, float speed) {
			float dif = f2 - f1;
			float dif2 = MathHelper
				.clamp_float((ClientProxy.clientTicks + partialTicks - lastModified) / speed, 0F, 1F);
			if (dif2 >= 1F)
				return f2;
			return f1 + dif * dif2;
		}

		public void findNext() {
			while (isRunning) {
				if (!te.excMatrix[x][y]) {
					// Check if the items needed to craft the selected part are
					// supplied
					Vec2 pos = new Vec2(x, y);
					CircuitPart part = te.cdata.getPart(pos);
					CraftingAmount amount = new CraftingAmount();
					part.getCraftingCost(amount, te.cdata, pos);
					ItemAmount insufficient = te.craftingSupply.getInsufficient();
					if (!te.craftingSupply.request(amount)) {
						te.updateStatus(te.OUT_OF_MATERIALS);
						ItemAmount insufficient2 = te.craftingSupply.getInsufficient();
						if (insufficient == null || !insufficient.hasEqualItem(insufficient2))
							CommonProxy.networkWrapper.sendToAllAround(new PacketAssemblerUpdateInsufficient(te.xCoord,
									te.yCoord, te.zCoord, te.craftingSupply.getInsufficient()),
									new TargetPoint(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord,
											te.zCoord, 8));
						return;
					} else
						te.updateStatus(te.RUNNING);

					te.excMatrix[x][y] = true;
					if (te.refMatrix[x][y] != 0) {
						setAim(x, y);
						return;
					}
				}

				x += direction.offsetX;
				y += direction.offsetZ;

				step--;
				te.laserHelper.position++;

				if (step <= 0) {
					turn++;
					step = max;
					direction = MiscUtils.rot(direction);
					if (turn == 2) {
						max--;
						turn = 0;
						if (max < 0) {
							isRunning = false;
							reset();
						}
					}
				}
			}
		}

		public void start() {
			isRunning = true;
			reset();
		}

		public void setAim(int x, int y) {
			if (MiscUtils.isServer())
				CommonProxy.networkWrapper.sendToDimension(new PacketAssemblerUpdate(isRunning, x, y, id, te.xCoord,
						te.yCoord, te.zCoord), te.getWorldObj().provider.dimensionId);

			this.x = x;
			this.y = y;
			this.isActive = false;

			if (MiscUtils.isClient())
				lastModified = ClientProxy.clientTicks;
			else
				lastModified = CommonProxy.serverTicks;

			reload();
			rotTimeAZ = Math.abs(lastAZ - aZ) * rotSpeed;
			rotTimeAY = Math.abs(lastAY - aY) * rotSpeed;
		}

		public void reset() {
			step = te.size - 1;
			max = te.size - 1;
			turn = 0;

			x = 0;
			y = 0;

			switch (id) {
				case 0:
					x = te.size - 1;
					y = te.size - 1;
					break;
				case 1:
					x = te.size - 1;
					break;
				case 3:
					y = te.size - 1;
					break;
			}

			switch (id) {
				case 0:
					direction = ForgeDirection.WEST;
					break;
				case 1:
					direction = ForgeDirection.SOUTH;
					break;
				case 2:
					direction = ForgeDirection.EAST;
					break;
				default:
					direction = ForgeDirection.NORTH;
					break;
			}

			if (MiscUtils.isClient())
				lastModified = ClientProxy.clientTicks;
			else
				lastModified = CommonProxy.serverTicks;

			reload();
			if (te.getWorldObj() != null && !MiscUtils.isClient())
				CommonProxy.networkWrapper.sendToDimension(new PacketAssemblerUpdate(isRunning, x, y, id, te.xCoord,
						te.yCoord, te.zCoord), te.getWorldObj().provider.dimensionId);
		}

		public boolean canUpdate() {
			return isActive && CommonProxy.serverTicks >= lastModified + laserSpeed;
		}

		public void update(float partialTicks) {
			if (!isActive) {
				if (te.getWorldObj().isRemote) {
					iZ = getInterpolated(partialTicks, lastAZ, aZ, rotTimeAZ);
					iY = getInterpolated(partialTicks, lastAY, aY, rotTimeAY);
					if (iZ == aZ && iY == aY) {
						isActive = true;
						lastModified = ClientProxy.clientTicks;
					}
				} else {
					if (CommonProxy.serverTicks - lastModified > rotTimeAY
							&& CommonProxy.serverTicks - lastModified > rotTimeAZ) {
						isActive = true;
						lastModified = CommonProxy.serverTicks;
					}
				}
			}
		}
	}
}
