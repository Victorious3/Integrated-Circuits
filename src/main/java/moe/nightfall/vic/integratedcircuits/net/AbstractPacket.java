package moe.nightfall.vic.integratedcircuits.net;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/** Gently Inspired by BetterStorage, thanks copy :D **/
public abstract class AbstractPacket<T extends AbstractPacket<T>> implements IMessage, IMessageHandler<T, IMessage> {
	@Override
	public IMessage onMessage(T message, MessageContext ctx) {
		message.process(ctx.side.isServer() ? ctx.getServerHandler().playerEntity : MiscUtils.thePlayer(), ctx.side);
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		try {
			read(new PacketBuffer(buf));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		try {
			write(new PacketBuffer(buf));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public abstract void read(PacketBuffer buffer) throws IOException;

	public abstract void write(PacketBuffer buffer) throws IOException;

	public abstract void process(EntityPlayer player, Side side);

	public static <T extends IMessage & IMessageHandler<T, IMessage>> void registerPacket(Class<T> clazz, Side side,
			int id) {
		if (side == null || side == Side.CLIENT)
			CommonProxy.networkWrapper.registerMessage(clazz, clazz, id, Side.CLIENT);
		if (side == null || side == Side.SERVER)
			CommonProxy.networkWrapper.registerMessage(clazz, clazz, id, Side.SERVER);
	}
}
