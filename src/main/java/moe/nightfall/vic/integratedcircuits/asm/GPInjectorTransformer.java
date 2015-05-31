package moe.nightfall.vic.integratedcircuits.asm;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Set;

import moe.nightfall.vic.integratedcircuits.API;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.GateIOProvider;
import moe.nightfall.vic.integratedcircuits.gate.GateRegistry;
import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

public class GPInjectorTransformer implements IClassTransformer, Opcodes {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (transformedName.startsWith("moe.nightfall.vic.integratedcircuits.api")
				|| transformedName.equals("moe.nightfall.vic.integratedcircuits.IntegratedCircuits")
				|| transformedName.equals("moe.nightfall.vic.integratedcircuits.API"))
			return basicClass;

		for (IntegratedCircuitsAPI.Type type : IntegratedCircuitsAPI.Type.values()) {
			if (type.classname.equals(transformedName)) {
				return transform(type, basicClass);
			}
		}
		return basicClass;
	}

	private byte[] transform(IntegratedCircuitsAPI.Type type, byte[] basicClass) {

		final Type ioProviderType = Type.getType(GateIOProvider.class);

		GateRegistry gateRegistry = IntegratedCircuits.API.getGateRegistry();
		gateRegistry.lock();

		IntegratedCircuits.logger.info("Injecting gate providers to class " + type.classname);

		ClassNode clazzNode = new ClassNode();
		ClassReader cr = new ClassReader(basicClass);
		cr.accept(clazzNode, 0);

		Set<Class<?>> intfMapping = gateRegistry.getInterfaceMapping(type);
		for (Class<?> intf : intfMapping) {
			clazzNode.interfaces.add(Type.getInternalName(intf));
			for (Method m : intf.getMethods()) {
				boolean isVoid = m.getReturnType() == null;
				String desc = Type.getMethodDescriptor(m);

				MethodVisitor mv = clazzNode.visitMethod(ACC_PUBLIC, m.getName(), desc, null, getExceptionTypes(m));
				mv.visitCode();

				mv.visitFieldInsn(GETSTATIC, Type.getInternalName(IntegratedCircuits.class), "API", Type.getDescriptor(API.class));
				mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(API.class), "getGateRegistry", Type.getMethodDescriptor(Type.getType(GateRegistry.class)), false);
				mv.visitLdcInsn(Type.getType(intf));
				mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(GateRegistry.class), "getProvider", Type.getMethodDescriptor(ioProviderType, Type.getType(Class.class)), false);

				// inject own instance as socket
				mv.visitVarInsn(ALOAD, 0);
				mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(StaticForwarder.class), "inject", Type.getMethodDescriptor(ioProviderType, ioProviderType, Type.getType(Object.class)), false);

				// add parameters
				Class<?>[] parameters = m.getParameterTypes();
				for (int i = 0; i < m.getParameterCount(); i++) {
					mv.visitVarInsn(Type.getType(parameters[i]).getOpcode(ILOAD), i + 1);
				}

				// invoke
				mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(intf), m.getName(), desc, true);
				mv.visitInsn(isVoid ? RETURN : Type.getType(m.getReturnType()).getOpcode(IRETURN));
				mv.visitMaxs(0, 0);

				mv.visitEnd();
			}
		}

		IntegratedCircuits.logger.info("Injected interfaces " + intfMapping);

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		clazzNode.accept(cw);
		byte[] bytes = cw.toByteArray();



		return bytes;
	}

	public String[] getExceptionTypes(Executable exec) {
		Class<?>[] execTypes = exec.getExceptionTypes();
		String[] exc = new String[execTypes.length];
		for (int i = 0; i < exec.getExceptionTypes().length; i++) {
			exc[i] = Type.getInternalName(execTypes[i]);
		}
		return exc;
	}
}
