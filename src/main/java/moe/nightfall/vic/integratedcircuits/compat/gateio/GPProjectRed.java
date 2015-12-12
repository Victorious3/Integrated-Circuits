package moe.nightfall.vic.integratedcircuits.compat.gateio;

import moe.nightfall.vic.integratedcircuits.api.gate.GateIOProvider;
import mrtjp.projectred.api.IBundledEmitter;
import mrtjp.projectred.transmission.APIImpl_Transmission;
import mrtjp.projectred.transmission.IRedwireEmitter;
import net.minecraft.tileentity.TileEntity;
import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.TileMultipart;
import net.minecraftforge.fml.common.Optional.Method;

public class GPProjectRed extends GateIOProvider {

	@Override
	@Method(modid = "ProjRed|Core")
	public byte[] calculateBundledInput(int side, int rotation, int abs, BlockCoord offset) {

		// Straight signal
		TileEntity t = socket.getWorld().getTileEntity(offset.x, offset.y, offset.z);

		if (t instanceof IBundledEmitter) {
			return updateBundledPartSignal(t, abs ^ 1);
		} else if (t instanceof TileMultipart) {
			return updateBundledPartSignal(((TileMultipart) t).partMap(socket.getSide()), (rotation + 2) % 4);
		} else {
			return APIImpl_Transmission.getBundledSignal(socket.getWorld(), offset, abs ^ 1);
		}
	}

	@Method(modid = "ProjRed|Transmission")
	protected byte[] updateBundledPartSignal(Object part, int r)
	{
		if (part instanceof IBundledEmitter)
			return ((IBundledEmitter) part).getBundledSignal(r);
		return null;
	}

	@Method(modid = "ForgeMultipart")
	protected int updatePartSignal(Object part, int r)
	{
		if (part instanceof IRedwireEmitter)
			return ((IRedwireEmitter) part).getRedwireSignal(r);
		return 0;
	}
}
