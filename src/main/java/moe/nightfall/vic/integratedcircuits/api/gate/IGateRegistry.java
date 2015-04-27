package moe.nightfall.vic.integratedcircuits.api.gate;

import moe.nightfall.vic.integratedcircuits.api.IPartRenderer;

public interface IGateRegistry {
	public void registerGate(String gateID, Class<? extends IGate> clazz);

	public <T extends IGate> void registerGateRenderer(Class<T> clazz, IPartRenderer<T> renderer);

	public IPartRenderer<IGate> getRenderer(Class<? extends IGate> clazz);

	public IPartRenderer<IGate> getRenderer(String gateID);

	public String getName(Class<? extends IGate> gate);

	public IGate createGateInstace(String gateID);

	/**
	 * Creates a default socket renderer with a texture name.
	 * 
	 * @param iconName
	 * @return
	 */
	public IPartRenderer<ISocket> createDefaultSocketRenderer(String iconName);

	public void registerGateIOProvider(GateIOProvider instance, Class<?>... classes);

	public <T> T createProxyInstance(Class<T> clazz);

	public <T> Class<T> createProxyClass(Class<T> clazz);
}