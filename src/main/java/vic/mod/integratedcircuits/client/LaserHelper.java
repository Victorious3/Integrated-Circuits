package vic.mod.integratedcircuits.client;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.TileEntityAssembler;
import vic.mod.integratedcircuits.net.PacketAssemblerUpdate;
import vic.mod.integratedcircuits.proxy.ClientProxy;
import vic.mod.integratedcircuits.proxy.CommonProxy;

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
	
	public static class Laser
	{
		private int x, y, id;
		public float iY, iZ, length;
		private float rotSpeed = 0.4F, laserSpeed = 75F;
		private float lastAY, lastAZ, aY, aZ, rotTimeAZ, rotTimeAY;
		private TileEntityAssembler te;
		public boolean isActive = true;
		private int lastModified;
		
		private Laser(TileEntityAssembler te, int id)
		{
			this.te = te;
			this.id = id;
			if(te.getWorldObj().isRemote) lastModified = ClientProxy.clientTicks;
			else lastModified = CommonProxy.serverTicks;
		}
		
		private void reload()
		{
			float w2 = 0;
			lastAY = aY;
			lastAZ = aZ;
			iY = aY;
			iZ = aZ;
			
			if(te.matrix != null)
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
		
		public void setAim(int x, int y)
		{
			if(!te.getWorldObj().isRemote)
			{
				if(!isActive || CommonProxy.serverTicks < lastModified + laserSpeed) return;
				IntegratedCircuits.networkWrapper.sendToAll(new PacketAssemblerUpdate(x, y, id, te.xCoord, te.yCoord, te.zCoord));
			}
			
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
