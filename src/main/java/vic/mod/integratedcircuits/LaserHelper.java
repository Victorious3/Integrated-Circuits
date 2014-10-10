package vic.mod.integratedcircuits;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;
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
	private boolean isRunning;
	
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
		isRunning = false;
		for(Laser laser : lasers)
		{
			if(laser != null) 
			{
				laser.isRunning = false;
				laser.reset();
			}
		}
	}
	
	public void start()
	{
		te.matrix = new int[te.size][te.size];
		isRunning = true;
		for(Laser laser : lasers)
			if(laser != null) laser.start();		
	}
	
	public void update()
	{
		if(isRunning)
		{
			boolean b = false;
			for(int i = 0; i < 4; i++)
			{
				Laser laser = getLaser(i);
				if(laser == null) continue;
				laser.update(0);
				if(laser.isRunning) b = true; 
				if(laser.canUpdate()) laser.findNext();
			}
			if(!b)
			{
				isRunning = false;
				te.onCircuitFinished();
			}
		}
	}
	
	public static class Laser
	{
		public int x, y, id;
		public float iY, iZ, length;
		private float rotSpeed = 0.4F, laserSpeed = 5F; //75F
		private float lastAY, lastAZ, aY, aZ, rotTimeAZ, rotTimeAY;
		private TileEntityAssembler te;
		public boolean isActive = true, isRunning = false;
		private int lastModified;
		
		private Laser(TileEntityAssembler te, int id)
		{
			this.te = te;
			this.id = id;
			if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) 
				lastModified = ClientProxy.clientTicks;
			else lastModified = CommonProxy.serverTicks;
			
			reset();
		}
		
		private void reload()
		{
			float w2 = 0;
			lastAY = aY;
			lastAZ = aZ;
			iY = aY;
			iZ = aZ;
			
			if(te.refMatrix != null && isRunning)
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
		
		private ForgeDirection direction;
		private int step, max, turn;
		
		public void findNext()
		{
			while(isRunning)
			{	
				if(te.matrix[x][y] == 0)
				{
					te.matrix[x][y] = 1;
					if(te.refMatrix[x][y] != 0)
					{
						setAim(x, y);
						return;
					}	
				}
				
				x += direction.offsetX;
				y += direction.offsetZ;
				
				step--;
				if(step == 1)
				{
					turn++;
					if(turn == 2)
					{
						max--;
						turn = 0;
						if(max == 1) 
						{
							isRunning = false;
							reset();
						}
					}
					step = max;
					direction = MiscUtils.rot(direction);
				}
			}
		}
		
		public void start()
		{
			isRunning = true;
			reset();	
		}
		
		public void setAim(int x, int y)
		{
			if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
				IntegratedCircuits.networkWrapper.sendToAll(new PacketAssemblerUpdate(isRunning, x, y, id, te.xCoord, te.yCoord, te.zCoord));
			
			this.x = x;
			this.y = y;
			this.isActive = false;
			
			if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
				lastModified = ClientProxy.clientTicks;
			else lastModified = CommonProxy.serverTicks;
			
			reload();
			rotTimeAZ = Math.abs(lastAZ - aZ) * rotSpeed;
			rotTimeAY = Math.abs(lastAY - aY) * rotSpeed;
		}
		
		public void reset()
		{
			step = te.size;
			max = te.size;
			turn = 0;
			
			x = 0;
			y = 0;
			
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
			
			if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
				lastModified = ClientProxy.clientTicks;
			else lastModified = CommonProxy.serverTicks;
			
			reload();
			IntegratedCircuits.networkWrapper.sendToAll(new PacketAssemblerUpdate(isRunning, x, y, id, te.xCoord, te.yCoord, te.zCoord));
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
