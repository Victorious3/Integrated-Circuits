package moe.nightfall.vic.integratedcircuits.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;

import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import net.minecraft.client.renderer.OpenGlHelper;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** See http://lwjgl.org/wiki/index.php?title=GLSL_Shaders_with_LWJGL **/
@SideOnly(Side.CLIENT)
public class ShaderHelper {
	public static int SHADER_BLUR;

	public static void loadShaders() {
		IntegratedCircuits.logger.info("Loading shaders, GLSL version supported: "
				+ GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
		if (!OpenGlHelper.shadersSupported)
			return;
		SHADER_BLUR = createProgramm("/assets/integratedcircuits/shader/blur.vert",
				"/assets/integratedcircuits/shader/blur.frag");
	}

	public static int createProgramm(String vertexShader, String fragmentShader) {
		int vertShader = 0, fragShader = 0;
		try {
			if (vertexShader != null)
				vertShader = createShader(vertexShader, ARBVertexShader.GL_VERTEX_SHADER_ARB);
			if (fragmentShader != null)
				fragShader = createShader(fragmentShader, ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
		} catch (Exception exc) {
			exc.printStackTrace();
			return 0;
		}

		int program = ARBShaderObjects.glCreateProgramObjectARB();
		if (program == 0)
			return 0;

		if (vertShader != 0)
			ARBShaderObjects.glAttachObjectARB(program, vertShader);
		if (fragShader != 0)
			ARBShaderObjects.glAttachObjectARB(program, fragShader);

		ARBShaderObjects.glLinkProgramARB(program);
		if (ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
			IntegratedCircuits.logger
				.fatal(ARBShaderObjects.glGetInfoLogARB(program, ARBShaderObjects.glGetObjectParameteriARB(program,
						ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB)));
			return 0;
		}

		ARBShaderObjects.glValidateProgramARB(program);
		if (ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE) {
			IntegratedCircuits.logger
				.fatal(ARBShaderObjects.glGetInfoLogARB(program, ARBShaderObjects.glGetObjectParameteriARB(program,
						ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB)));
			return 0;
		}

		return program;
	}

	public static int createShader(String filename, int shaderType) throws Exception {
		int shader = 0;
		try {
			shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);
			if (shader == 0)
				return 0;

			InputStream in = ShaderHelper.class.getResourceAsStream(filename);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			CharBuffer cbuf = CharBuffer.allocate(in.available());
			reader.read(cbuf);
			reader.close();

			ARBShaderObjects.glShaderSourceARB(shader, new String(cbuf.array()));
			ARBShaderObjects.glCompileShaderARB(shader);

			if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE)
				throw new RuntimeException("Error creating shader: "
						+ ARBShaderObjects.glGetInfoLogARB(shader, ARBShaderObjects.glGetObjectParameteriARB(shader,
								ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB)));

			return shader;

		} catch (Exception exc) {
			ARBShaderObjects.glDeleteObjectARB(shader);
			throw exc;
		}
	}

	public static void printErrorLog(int program) {
		IntBuffer intBuffer = BufferUtils.createIntBuffer(1);
		ARBShaderObjects.glGetObjectParameterARB(program, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB, intBuffer);

		int length = intBuffer.get();
		if (length > 1) {
			ByteBuffer infoLog = BufferUtils.createByteBuffer(length);
			intBuffer.flip();
			ARBShaderObjects.glGetInfoLogARB(program, intBuffer, infoLog);
			byte[] infoBytes = new byte[length];
			infoLog.get(infoBytes);
			String out = new String(infoBytes);
			IntegratedCircuits.logger.fatal("Shader info log:\n" + out);
		}
	}

	public static void bindShader(int program) {
		if (!OpenGlHelper.shadersSupported)
			return;
		ARBShaderObjects.glUseProgramObjectARB(program);
	}

	public static void releaseShader() {
		ARBShaderObjects.glUseProgramObjectARB(0);
	}
}
