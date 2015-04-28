package moe.nightfall.vic.integratedcircuits.client;

import moe.nightfall.vic.integratedcircuits.Config;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import moe.nightfall.vic.integratedcircuits.api.IPartRenderer;
import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.IGate;
import moe.nightfall.vic.integratedcircuits.api.gate.IGateItem;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket.EnumConnectionType;
import moe.nightfall.vic.integratedcircuits.client.model.ModelBase;
import moe.nightfall.vic.integratedcircuits.proxy.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

import org.lwjgl.opengl.GL11;

import codechicken.lib.lighting.LightModel;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SocketRenderer extends PartRenderer<ISocket> {
	private ISocket socket;
	private float partialTicks;

	public SocketRenderer(String iconName) {
		models.add(new ModelBase(iconName));
	}

	public void prepare(ISocket socket) {
		this.socket = socket;
		if (socket != null && socket.getGate() != null)
			IntegratedCircuitsAPI.getGateRegistry().getRenderer(socket.getGate().getClass()).prepare(socket.getGate());
	}

	@Override
	public void prepareInv(ItemStack stack) {
		socket = null;
	}

	public void prepareDynamic(ISocket socket, float partialTicks) {
		this.partialTicks = partialTicks;
		this.socket = socket;
		if (socket != null && socket.getGate() != null)
			IntegratedCircuitsAPI.getGateRegistry().getRenderer(socket.getGate().getClass())
				.prepareDynamic(socket.getGate(), partialTicks);
	}

	public void renderStatic(Transformation t) {
		if (socket != null) {
			Transformation rotation = Rotation.sideOrientation(socket.getSide(), socket.getRotation()).at(Vector3.center);
			Transformation rotation2 = Rotation.sideOrientation(socket.getSide(), 0).at(Vector3.center);

			renderConnections(rotation2.with(t));
			t = rotation.with(t);
		}
		super.renderStatic(t);

		t = new Translation(0, 2 / 16F, 0).with(t);
		if (socket != null && socket.getGate() != null)
			IntegratedCircuitsAPI.getGateRegistry().getRenderer(socket.getGate().getClass())
				.renderStatic(t);
	}

	protected void renderConnections(Transformation t) {
		if (socket == null || socket.getGate() == null)
			return;
		for (int i = 0; i < 4; i++) {
			double size = getInset(i);
			EnumConnectionType type = socket.getConnectionTypeAtSide(i);
			if (type.isRedstone()) {
				// TODO Someone should go over it and standardize every
				// rotation.
				IIcon icon = (socket.getRedstoneIO() & 1 << (4 - i) % 4) != 0 ? Resources.ICON_IC_RSWIRE_ON : Resources.ICON_IC_RSWIRE_OFF;
				CCModel model = CCModel.quadModel(72);
				model.generateBox(00, 7, 2, 0, 2, 0.32, size, 0, 0, 16, 16, 16);
				model.generateBox(24, 6, 2, 0, 1, 0.16, size, 9, 0, 16, 16, 16);
				model.generateBox(48, 9, 2, 0, 1, 0.16, size, 9, 0, 16, 16, 16);
				model.computeNormals();
				model.apply(new Rotation((i - socket.getRotation()) * Math.PI / 2F, 0, 1, 0).at(Vector3.center));
				model.apply(t);
				model.computeLighting(LightModel.standardLightModel);
				model.render(new IconTransformation(icon));
			} else if (type.isBundled()) {
				CCModel model = CCModel.quadModel(24);
				model.generateBlock(0, 5 / 16D, 0, 0, 11 / 16D, 4 / 16D, size / 16D);
				model.computeNormals();
				model.apply(new Rotation(i * Math.PI / 2F, 0, 1, 0).at(Vector3.center));
				model.apply(t);
				model.computeLighting(LightModel.standardLightModel);
				model.render(new IconTransformation(i == 2 || i == 1 ? Resources.ICON_IC_WIRE_FLIPPED : Resources.ICON_IC_WIRE));
			}
		}
	}

	protected double getInset(int side) {
		double inset;
		Cuboid6 dimensions = socket.getGate().getDimension().copy();
		dimensions.apply(new Rotation(socket.getRotation() * Math.PI / 2, 0, 1, 0).at(new Vector3(8, 8, 8)));

		switch (side) {
			case 0:
				inset = dimensions.min.x;
				break;
			case 1:
				inset = 16 - dimensions.max.z;
				break;
			case 2:
				inset = 16 - dimensions.max.x;
				break;
			default:
				inset = dimensions.min.z;
				break;
		}
		return inset;
	}

	public void renderDynamic(Transformation t) {
		if (socket != null && socket.getGate() != null) {
			Transformation rotation = Rotation.sideOrientation(socket.getSide(), socket.getRotation()).at(Vector3.center);
			IntegratedCircuitsAPI.getGateRegistry().getRenderer(socket.getGate().getClass())
				.renderDynamic(new Translation(0, 2 / 16F, 0).with(rotation).with(t));
		} else if (socket != null) {
			// Render translucent version
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			ItemStack stack;
			if ((stack = player.getCurrentEquippedItem()) != null && stack.getItem() instanceof IGateItem) {
				MovingObjectPosition mop = Minecraft.getMinecraft().objectMouseOver;
				BlockCoord pos = socket.getPos();
				if (mop.typeOfHit == MovingObjectType.BLOCK && mop.blockX == pos.x && mop.blockY == pos.y
						&& mop.blockZ == pos.z) {
					if (!player.inventory.hasItem(IntegratedCircuits.itemSolderingIron)) {
						if (Config.enableTooltips) {
							ClientProxy.drawTooltip(I18n.format("tooltip.integratedcircuits.socket"));
						}
						return;
					}

					String gateID = ((IGateItem) stack.getItem()).getGateID(stack, Minecraft.getMinecraft().thePlayer, pos);
					IPartRenderer<IGate> renderer = IntegratedCircuitsAPI.getGateRegistry().getRenderer(gateID);

					int rotation = Rotation.getSidedRotation(player, socket.getSide() ^ 1);
					t = new Translation(0, 2 / 16F, 0).with(Rotation.sideOrientation(socket.getSide(), rotation).at(Vector3.center)).with(t);

					TextureUtils.bindAtlas(0);
					CCRenderState.reset();
					CCRenderState.setDynamic();
					CCRenderState.pullLightmap();

					GL11.glEnable(GL11.GL_BLEND);
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					GL11.glEnable(GL11.GL_ALPHA_TEST);

					renderer.prepareInv(stack);
					CCRenderState.alphaOverride = (int) (120 + 20 * Math.sin((ClientProxy.clientTicks + partialTicks) / 5D));
					CCRenderState.startDrawing();
					renderer.renderStatic(t);
					CCRenderState.draw();
					renderer.renderDynamic(t);
					CCRenderState.alphaOverride = 0;

					GL11.glDisable(GL11.GL_BLEND);
				}
			}
		}
	}
}
