package moe.nightfall.vic.integratedcircuits.api.gate;

import moe.nightfall.vic.integratedcircuits.api.IPartRenderer;
import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI.Type;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IGateRegistry {
	public void registerGate(String gateID, Class<? extends IGate> clazz);

	@SideOnly(Side.CLIENT)
	public <T extends IGate> void registerGateRenderer(Class<T> clazz, IPartRenderer<T> renderer);

	@SideOnly(Side.CLIENT)
	public IPartRenderer<IGate> getRenderer(Class<? extends IGate> clazz);

	@SideOnly(Side.CLIENT)
	public IPartRenderer<IGate> getRenderer(String gateID);

	public String getName(Class<? extends IGate> gate);

	public IGate createGateInstace(String gateID);

	public ISocket createSocketInstance(ISocketWrapper wrapper);

	/**
	 * Creates a default socket renderer with a texture name.
	 * 
	 * @param iconName
	 * @return
	 */
	@SideOnly(Side.CLIENT)
	public IPartRenderer<ISocket> createDefaultSocketRenderer(String iconName);

	public void registerGateIOProvider(GateIOProvider instance, Type... elements);
}