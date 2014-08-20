package vic.mod.integratedcircuits.net;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.Misc;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

/** Gently Inspired by BetterStorage, thanks copy :D **/
public abstract class AbstractPacket<T extends AbstractPacket<T>> implements IMessage, IMessageHandler<T, IMessage>
{
	@Override
	public IMessage onMessage(T message, MessageContext ctx) 
	{
		message.process(ctx.side.isServer() ? ctx.getServerHandler().playerEntity : Misc.thePlayer(), ctx.side);
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) 
	{
		try {
			read(new PacketBuffer(buf));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) 
	{
		try {
			write(new PacketBuffer(buf));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public abstract void read(PacketBuffer buffer) throws IOException;
	
	public abstract void write(PacketBuffer buffer) throws IOException;
	
	public abstract void process(EntityPlayer player, Side side);
}
