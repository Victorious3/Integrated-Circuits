package moe.nightfall.vic.integratedcircuits.api;

public interface IGateRegistry
{
	public void registerGate(String name, Class<? extends IGate> clazz);

	public <T extends IGate> void registerGateRenderer(Class<T> clazz, IPartRenderer<T> renderer);

	public IPartRenderer<IGate> getRenderer(Class<? extends IGate> clazz);

	public IPartRenderer<IGate> getRenderer(String gateID);

	public String getName(Class<? extends IGate> gate);

	public IGate createGateInstace(String name);
	
	public void registerGateIOProvider(GateIOProvider instance, Class<? extends ISocketWrapper> clazz);
}