package moe.nightfall.vic.integratedcircuits.gate.peripheral;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import net.minecraftforge.fml.common.Optional.Interface;
import dan200.computercraft.api.filesystem.IMount;

@Interface(iface = "dan200.computercraft.api.filesystem.IMount", modid = "ComputerCraft")
public class FileMount implements IMount {
	private ArrayList<String> files;
	private String path;

	public FileMount(String path) {
		try {
			this.path = "assets/" + Constants.MOD_ID + "/" + path + "/";
			files = new ArrayList<String>();

			String jfpath = IntegratedCircuits.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			if (jfpath.contains("!")) {
				jfpath = jfpath.substring(0, jfpath.indexOf("!"));
				JarFile jar = new JarFile(new File(new URL(jfpath).toURI()));
				Enumeration<JarEntry> entries = jar.entries();
				while (entries.hasMoreElements()) {
					String name = entries.nextElement().getName();
					if (!name.equals(this.path) && name.startsWith(this.path))
						files.add(name.substring(this.path.length()));
				}
				jar.close();
			} else {
				URL url = IntegratedCircuits.class.getResource("/" + this.path);
				if (url != null) {
					File apps = new File(url.toURI());
					for (File app : apps.listFiles())
						files.add(app.getName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean exists(String path) throws IOException {
		return path.equals("") || files.contains(path);
	}

	@Override
	public boolean isDirectory(String path) throws IOException {
		return path.equals("");
	}

	@Override
	public void list(String path, List<String> contents) throws IOException {
		contents.addAll(files);
	}

	@Override
	public long getSize(String path) throws IOException {
		return IntegratedCircuits.class.getResourceAsStream("/" + this.path + path).available();
	}

	@Override
	public InputStream openForRead(String path) throws IOException {
		if (!exists(path))
			throw new IOException();
		return IntegratedCircuits.class.getResourceAsStream("/" + this.path + path);
	}
}