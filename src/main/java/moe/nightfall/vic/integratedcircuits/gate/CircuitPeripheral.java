package moe.nightfall.vic.integratedcircuits.gate;

import java.util.Map;

import moe.nightfall.vic.integratedcircuits.Config;
import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.ic.CircuitData;
import moe.nightfall.vic.integratedcircuits.ic.CircuitPart;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IProperty;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraftforge.common.util.ForgeDirection;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;

// TODO Test me!
public class CircuitPeripheral extends GatePeripheral {

	private GateCircuit circuit;

	public CircuitPeripheral(GateCircuit circuit) {
		this.circuit = circuit;
	}

	@Override
	public String getType() {
		return "ic_circuit";
	}

	@LuaMethod
	public Object[] getGateAt(double x, double y) {
		CircuitData cdata = circuit.getCircuitData();
		Vec2 pos = getPos(x, y);
		CircuitPart cp = cdata.getPart(pos);
		String name = cp.getName(pos, circuit);
		int id = CircuitPart.getId(cp);
		int meta = cp.getState(pos, circuit);
		return new Object[] { name, id, meta };
	}

	@LuaMethod
	public boolean[] getPowerTo(double x, double y) {
		CircuitData cdata = circuit.getCircuitData();
		Vec2 pos = getPos(x, y);
		CircuitPart cp = cdata.getPart(pos);

		boolean b1 = cp.getInputFromSide(pos, circuit, ForgeDirection.NORTH);
		boolean b2 = cp.getInputFromSide(pos, circuit, ForgeDirection.EAST);
		boolean b3 = cp.getInputFromSide(pos, circuit, ForgeDirection.SOUTH);
		boolean b4 = cp.getInputFromSide(pos, circuit, ForgeDirection.WEST);

		return new boolean[] { b1, b2, b3, b4 };
	}

	@LuaMethod
	public Object[] getGateProperties(double x, double y) {
		CircuitData cdata = circuit.getCircuitData();
		CircuitPart cp = cdata.getPart(getPos(x, y));

		Map<String, IProperty> properties = cp.stitcher.getProperties();
		return properties.keySet().toArray();
	}

	@LuaMethod
	public Object[] getGateProperty(double x, double y, String name) throws LuaException {
		CircuitData cdata = circuit.getCircuitData();
		Vec2 pos = getPos(x, y);
		CircuitPart cp = cdata.getPart(pos);

		int state = cp.getState(pos, circuit);
		IProperty property = getProperty(cp, pos, name);

		return new Object[] { property.get(state), property.getClass().getSimpleName() };
	}

	@LuaMethod
	public void setGateProperty(double x, double y, String name, Object obj) throws LuaException {
		CircuitData cdata = circuit.getCircuitData();
		Vec2 pos = getPos(x, y);
		CircuitPart cp = cdata.getPart(pos);

		int state = cp.getState(pos, circuit);
		IProperty property = getProperty(cp, pos, name);

		if (!Config.enablePropertyEdit)
			throw new LuaException("Property editing is disabled from the config file.");
		if (obj instanceof Double)
			obj = ((Double) obj).intValue();
		cp.setProperty(pos, circuit, property, (Comparable) obj);
		cp.notifyNeighbours(pos, circuit);
	}

	@LuaMethod
	public byte[] getOutputToSide(double side) throws LuaException {
		if (side < 0 || side > 3)
			throw new LuaException(String.format("Illegal side provided. (%s) [0->3]", side));
		return circuit.provider.getOutput()[(int) side];
	}

	@LuaMethod
	public byte[] getInputFromSide(double side) throws LuaException {
		if (side < 0 || side > 3)
			throw new LuaException(String.format("Illegal side provided. (%s) [0->3]", side));
		return circuit.provider.getInput()[(int) side];
	}

	@LuaMethod
	public double getSize() {
		return circuit.getCircuitData().getSize();
	}

	@LuaMethod
	public String getName() {
		return circuit.getCircuitData().getProperties().getName();
	}

	@LuaMethod
	public String getAuthor() {
		return circuit.getCircuitData().getProperties().getAuthor();
	}

	// TODO And this is supposed to work?
	public String getGateName(double id) {
		CircuitPart cp = CircuitPart.getPart((int) id);
		return cp.toString();
	}

	private Vec2 getPos(double x, double y) {
		return new Vec2((int) x, (int) y);
	}

	private IProperty getProperty(CircuitPart part, Vec2 pos, String name) throws LuaException {
		IProperty property = part.stitcher.getPropertyByName(name);
		if (property == null)
			throw new LuaException(String.format("No property by the name of '&s' found for gate %s", name, part.getName(pos, circuit)));
		return property;
	}

	@Override
	public void attach(IComputerAccess computer) {
		computer.mount("rom/programs/" + Constants.MOD_ID, new FileMount("lua"));
	}
}
