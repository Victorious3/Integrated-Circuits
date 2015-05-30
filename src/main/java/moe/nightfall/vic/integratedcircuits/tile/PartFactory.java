package moe.nightfall.vic.integratedcircuits.tile;

import java.util.HashMap;

import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.TMultiPart;

import com.google.common.collect.Maps;

public class PartFactory implements IPartFactory {
	private static HashMap<String, Class<? extends TMultiPart>> parts = Maps.newHashMap();
	public static PartFactory instance = new PartFactory();

	private PartFactory() {
	}

	public static void register(String type, Class<? extends TMultiPart> clazz) {
		parts.put(type, clazz);
	}

	@Override
	public TMultiPart createPart(String arg0, boolean arg1) {
		Class clazz = parts.get(arg0);
		if (clazz == null)
			return null;
		try {
			return (TMultiPart) clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void initialize() {
		String[] keys = parts.keySet().toArray(new String[parts.keySet().size()]);
		MultiPartRegistry.registerParts(instance, keys);
	}
}
