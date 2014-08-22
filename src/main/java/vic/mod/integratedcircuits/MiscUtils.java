package vic.mod.integratedcircuits;

import static net.minecraftforge.common.util.ForgeDirection.EAST;
import static net.minecraftforge.common.util.ForgeDirection.NORTH;
import static net.minecraftforge.common.util.ForgeDirection.SOUTH;
import static net.minecraftforge.common.util.ForgeDirection.WEST;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MiscUtils 
{
	public static int[][][] readPCBMatrix(NBTTagCompound compound)
	{
		NBTTagCompound matrixCompound = compound.getCompoundTag("matrix");
		NBTTagList idlist = matrixCompound.getTagList("id", NBT.TAG_INT_ARRAY);
		int[][] id = new int[idlist.tagCount()][];
		for(int i = 0; i < idlist.tagCount(); i++)
		{
			id[i] = idlist.func_150306_c(i);
		}
		
		NBTTagList metalist = matrixCompound.getTagList("meta", NBT.TAG_INT_ARRAY);
		int[][] meta = new int[metalist.tagCount()][];
		for(int i = 0; i < metalist.tagCount(); i++)
		{
			meta[i] = metalist.func_150306_c(i);
		}
		
		int[][][] pcbMatrix = new int[2][][];
		pcbMatrix[0] = id;
		pcbMatrix[1] = meta;
		
		return pcbMatrix;
	}

	public static void writePCBMatrix(NBTTagCompound compound, int[][][] pcbMatrix)
	{
		NBTTagList idlist = new NBTTagList();
		for(int i = 0; i < pcbMatrix[0].length; i++)
		{
			idlist.appendTag(new NBTTagIntArray(pcbMatrix[0][i]));
		}
		
		NBTTagList metalist = new NBTTagList();
		for(int i = 0; i < pcbMatrix[1].length; i++)
		{
			metalist.appendTag(new NBTTagIntArray(pcbMatrix[1][i]));
		}
		
		NBTTagCompound matrixCompound = new NBTTagCompound();
		matrixCompound.setTag("id", idlist);
		matrixCompound.setTag("meta", metalist);
		compound.setTag("matrix", matrixCompound);
	}

	@SideOnly(Side.CLIENT)
	public static EntityPlayer thePlayer() 
	{
		return Minecraft.getMinecraft().thePlayer;
	}

	public static <T extends IMessage & IMessageHandler<T, IMessage>> void registerPacket(Class<T> clazz, Side side, int id)
	{
		if(side == null || side == Side.CLIENT) IntegratedCircuits.networkWrapper.registerMessage(clazz, clazz, id, Side.CLIENT);
		if(side == null || side == Side.SERVER) IntegratedCircuits.networkWrapper.registerMessage(clazz, clazz, id, Side.SERVER);
	}

	public static ForgeDirection[] order = new ForgeDirection[]{NORTH, EAST, SOUTH, WEST};
	public static int[] index = new int[]{-1, -1, 0, 2, 3, 1, -1};
	
	public static ForgeDirection rotn(ForgeDirection fd, int offset)
	{
		int pos = index[fd.ordinal()];
		int newPos = pos + offset;
		pos = newPos > 3 ? newPos - 4 : newPos < 0 ? newPos + 4 : newPos;
		return order[pos];
	}

	public static ForgeDirection rot(ForgeDirection fd)
	{
		return rotn(fd, 1);
	}
	
	/** Borrowed from BetterStorage **/
	public static MovingObjectPosition rayTrace(EntityPlayer player, float partialTicks) 
	{
		double range = ((player.worldObj.isRemote)
			? Minecraft.getMinecraft().playerController.getBlockReachDistance()
			: ((EntityPlayerMP)player).theItemInWorldManager.getBlockReachDistance());
		
		Vec3 start = Vec3.createVectorHelper(player.posX, player.posY + 1.62 - player.yOffset, player.posZ);
		Vec3 look = player.getLook(1.0F);
		Vec3 end = start.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range);
		
		MovingObjectPosition target = player.worldObj.rayTraceBlocks(start, end);
		return target;
	}
}
