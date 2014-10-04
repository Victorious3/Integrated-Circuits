package vic.mod.integratedcircuits.client;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.TileEntityAssembler;
import vic.mod.integratedcircuits.net.PacketAssemblerUpdate;
import vic.mod.integratedcircuits.proxy.ClientProxy;
import vic.mod.integratedcircuits.proxy.CommonProxy;
import vic.mod.integratedcircuits.util.MiscUtils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class LaserHelper 
{
	private Laser[] lasers = new Laser[4];
	private TileEntityAssembler te;
	
	public LaserHelper(TileEntityAssembler te)
	{
		this.te = te;
		createLaser(0);
		createLaser(1);
		createLaser(2);
		createLaser(3);
	}
	
	public Laser getLaser(int id)
	{
		return lasers[id];
	}
	
	public void createLaser(int id)
	{
		lasers[id] = new Laser(te, id);
	}
	
	public void removeLaser(int id)
	{
		lasers[id] = null;
	} 
	
	public NBTTagCompound writeToNBT(NBTTagCompound tag)
	{
		return tag;
	}
	
	public void readFromNBT(NBTTagCompound tag)
	{
		
	}
	
	public void reload()
	{
		for(Laser laser : lasers)
			if(laser != null) laser.reload();
	}
	
	public void reset()
	{
		for(Laser laser : lasers)
			if(laser != null) laser.reset();
	}
	
	public static class Laser
	{
		public int x, y, id;
		public float iY, iZ, length;
		private float rotSpeed = 0.4F, laserSpeed = 5F; //75F
		private float lastAY, lastAZ, aY, aZ, rotTimeAZ, rotTimeAY;
		private TileEntityAssembler te;
		public boolean isActive = true, isDone = false;
		private int lastModified;
		private ForgeDirection direction;
		
		private Laser(TileEntityAssembler te, int id)
		{
			this.te = te;
			this.id = id;
			if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) 
				lastModified = ClientProxy.clientTicks;
			else lastModified = CommonProxy.serverTicks;
			
			switch (id) {
			case 0:
				direction = ForgeDirection.WEST; break;
			case 1:
				direction = ForgeDirection.SOUTH; break;
			case 2:
				direction = ForgeDirection.EAST; break;
			default:
				direction = ForgeDirection.NORTH; break;
			}
		}
		
		private void reload()
		{
			float w2 = 0;
			lastAY = aY;
			lastAZ = aZ;
			iY = aY;
			iZ = aZ;
			
			if(te.refMatrix != null)
			{
				float x2 = x + 0.5F;
				float y2 = y + 0.5F;
				
				if(id == 3 || id == 1) 
				{
					x2 = y + 0.5F;
					y2 = x + 0.5F;
				}
				
				if(id == 3 || id == 0) x2 = te.size - x2;
				if(id == 1 || id == 0) y2 = te.size - y2;
				
				float w1 = (10 / 16F * (x2 / (float)te.size)) + (3 / 16F - (0.5F - (float)Math.sin(Math.PI / 4D) * 0.5F));
				float h1 = (10 / 16F * (y2 / (float)te.size)) + (3 / 16F - (0.5F - (float)Math.sin(Math.PI / 4D) * 0.5F));
				aZ = (float)Math.atan(w1 / h1);
				w2 = (float)(w1 / Math.sin(aZ));
				aZ = (float)Math.toDegrees(aZ) - 45F;
				aY = 90F - (float)Math.toDegrees(Math.atan(w2 / (6 / 16F - 2 / 80F)));
				length = w2 / (float)Math.sin(Math.toRadians(90F - aY));
			}
			else
			{
				aY = 0;
				aZ = 0;
			}		
		}
		
		public float getInterpolated(float partialTicks, float f1, float f2, float speed)
		{
			float dif = f2 - f1;
			float dif2 = MathHelper.clamp_float((ClientProxy.clientTicks + partialTicks - lastModified) / speed, 0F, 1F);
			if(dif2 >= 1F) return f2;
			return f1 + dif * dif2;
		}
		
		public void reset()
		{
			if(te.refMatrix != null)
			{
				switch (id) {
				case 0:
					x = te.size - 1;
					y = te.size - 1;
					break;
				case 1:
					x = te.size - 1;
					break;
				case 3:
					y = te.size - 1;
					break;
				}
			}
			IntegratedCircuits.networkWrapper.sendToAll(new PacketAssemblerUpdate(x, y, id, te.xCoord, te.yCoord, te.zCoord));
		}
		
		public void findNext()
		{
			while(!isDone)
			{
				boolean b1 = x + 1 >= te.size || te.matrix[x + 1][y] != 0;
				boolean b2 = y + 1 >= te.size || te.matrix[x][y + 1] != 0;
				boolean b3 = x - 1 < 0 || te.matrix[x - 1][y] != 0;
				boolean b4 = y - 1 < 0 || te.matrix[x][y - 1] != 0;	
				isDone = b1 && b2 && b3 && b4;
				if(isDone) return;
				
				int nX = x;
				int nY = y;
				nX += direction.offsetX;
				nY += direction.offsetZ;
				
				if(nX < 0 || nY < 0 || nX >= te.size || nY >= te.size || te.matrix[nX][nY] != 0)
					direction = MiscUtils.rot(direction);
				else
				{
					te.matrix[x][y] = 1;
					x = nX;
					y = nY;
					
					if(te.refMatrix[x][y] != 0 && te.matrix[x][y] == 0)
					{
						setAim(x, y);
						return;
					}
				}
			}
		}
		
		public void setAim(int x, int y)
		{
			if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
				IntegratedCircuits.networkWrapper.sendToAll(new PacketAssemblerUpdate(x, y, id, te.xCoord, te.yCoord, te.zCoord));
			
			this.x = x;
			this.y = y;
			this.isActive = false;
			
			if(te.getWorldObj().isRemote)
				lastModified = ClientProxy.clientTicks;
			else lastModified = CommonProxy.serverTicks;
			
			reload();
			rotTimeAZ = Math.abs(lastAZ - aZ) * rotSpeed;
			rotTimeAY = Math.abs(lastAY - aY) * rotSpeed;
		}
		
		public boolean canUpdate()
		{
			return isActive && CommonProxy.serverTicks >= lastModified + laserSpeed;
		}
		
		public void update(float partialTicks)
		{
			if(!isActive)
			{
				if(te.getWorldObj().isRemote)
				{
					iZ = getInterpolated(partialTicks, lastAZ, aZ, rotTimeAZ);
					iY = getInterpolated(partialTicks, lastAY, aY, rotTimeAY);
					if(iZ == aZ && iY == aY) 
					{
						isActive = true;
						lastModified = ClientProxy.clientTicks;
					}
				}
				else
				{
					if(CommonProxy.serverTicks - lastModified > rotTimeAY && CommonProxy.serverTicks - lastModified > rotTimeAZ)
					{
						isActive = true;
						lastModified = CommonProxy.serverTicks;
					}
				}
			}
		}
	}
}
