package moe.nightfall.vic.integratedcircuits.compat.buildcraft;

import moe.nightfall.vic.integratedcircuits.api.IPartRenderer;
import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.IGate;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.core.render.ITextureStates;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.pluggable.IPipePluggableDynamicRenderer;
import buildcraft.api.transport.pluggable.IPipePluggableRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BCGateRenderer implements IPipePluggableRenderer, IPipePluggableDynamicRenderer {

	public static final BCGateRenderer instance = new BCGateRenderer();

	private BCGateRenderer() {
	}

	private Transformation getTransformation(IGate gate, ForgeDirection side, double x, double y, double z) {
		return new Translation(0, 0.85, 0).with(new Scale(0.75).at(Vector3.center))
			.with(Rotation.sideOrientation(side.getOpposite().ordinal(), gate.getProvider().getRotation()).at(Vector3.center))
			.with(new Translation(x, y, z));
	}

	@Override
	public void renderPluggable(IPipe pipe, ForgeDirection side, PipePluggable pipePluggable, double x, double y, double z) {
		GatePipePluggable gpp = (GatePipePluggable) pipePluggable;
		IGate gate = gpp.getSocket().getGate();
		IPartRenderer<IGate> renderer = IntegratedCircuitsAPI.getGateRegistry().getRenderer(gate.getClass());
		renderer.prepareDynamic(gate, 0);
		renderer.renderDynamic(getTransformation(gate, side, x, y, z));
	}

	@Override
	public void renderPluggable(RenderBlocks renderblocks, IPipe pipe, ForgeDirection side, PipePluggable pipePluggable, ITextureStates blockStateMachine, int renderPass, int x, int y, int z) {
		GatePipePluggable gpp = (GatePipePluggable) pipePluggable;
		IGate gate = gpp.getSocket().getGate();
		IPartRenderer<IGate> renderer = IntegratedCircuitsAPI.getGateRegistry().getRenderer(gate.getClass());
		renderer.prepare(gate);
		renderer.renderStatic(getTransformation(gate, side, x, y, z));
	}
}
