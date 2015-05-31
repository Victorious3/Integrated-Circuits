package moe.nightfall.vic.integratedcircuits.asm;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import cpw.mods.fml.common.ModClassLoader;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;

@MCVersion(value = "1.7.10")
public class FMLLoadingPlugin implements IFMLLoadingPlugin {

	@Override
	public String[] getASMTransformerClass() {
		return new String[] { GPInjectorTransformer.class.getName() };
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

	// Needed because I have to be the first one transforming my class...
	public static void ensureFirst() {
		try {
			LaunchClassLoader lc = (LaunchClassLoader) ModClassLoader.class.getClassLoader();
			Field transformersField = LaunchClassLoader.class.getDeclaredField("transformers");
			transformersField.setAccessible(true);
			List<IClassTransformer> transformers = (List<IClassTransformer>) transformersField.get(lc);
			GPInjectorTransformer gp = null;
			Iterator<IClassTransformer> iterator = transformers.iterator();
			while (iterator.hasNext()) {
				IClassTransformer transformer = iterator.next();
				if (transformer instanceof GPInjectorTransformer) {
					gp = (GPInjectorTransformer) transformer;
					iterator.remove();
				}
			}
			if (gp != null)
				transformers.add(0, gp);
			transformersField.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
