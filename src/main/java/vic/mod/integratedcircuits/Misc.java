package vic.mod.integratedcircuits;

import static net.minecraftforge.common.util.ForgeDirection.EAST;
import static net.minecraftforge.common.util.ForgeDirection.NORTH;
import static net.minecraftforge.common.util.ForgeDirection.SOUTH;
import static net.minecraftforge.common.util.ForgeDirection.WEST;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.ForgeDirection;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.TMultiPart;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Part Factory, abused as Util class */
public class Misc implements IPartFactory
{
	@Override
	public TMultiPart createPart(String arg0, boolean arg1) 
	{
		if(arg0.equals(IntegratedCircuits.partCircuit)) return new PartCircuit();
		return null;
	}
	
	//TODO Move it, it does not fit in here!
	
	public static ForgeDirection[] order = new ForgeDirection[]{NORTH, EAST, SOUTH, WEST};
	public static int[] index = new int[]{-1, -1, 0, 2, 3, 1, -1};
	
	public static ForgeDirection rot(ForgeDirection fd)
	{
		return rotn(fd, 1);
	}
	
	public static ForgeDirection rotn(ForgeDirection fd, int offset)
	{
		int pos = index[fd.ordinal()];
		int newPos = pos + offset;
		pos = newPos > 3 ? newPos - 4 : newPos < 0 ? newPos + 4 : newPos;
		return order[pos];
	}
	
	public static int[][][] readPCBMatrix(NBTTagCompound compound)
	{
		NBTTagList idlist = compound.getTagList("id", NBT.TAG_INT_ARRAY);
		int[][] id = new int[idlist.tagCount()][];
		for(int i = 0; i < idlist.tagCount(); i++)
		{
			id[i] = idlist.func_150306_c(i);
		}
		
		NBTTagList metalist = compound.getTagList("meta", NBT.TAG_INT_ARRAY);
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
		
		compound.setTag("id", idlist);
		compound.setTag("meta", metalist);
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
}
