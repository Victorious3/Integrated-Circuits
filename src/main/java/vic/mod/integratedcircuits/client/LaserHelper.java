package vic.mod.integratedcircuits.client;

import net.minecraft.nbt.NBTTagCompound;
import vic.mod.integratedcircuits.TileEntityAssembler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
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
		public float lastAY, lastAZ, aY, aZ, length;
		private float speed = 1F;
		private TileEntityAssembler te;
		public boolean isActive = true;
		
		private Laser(TileEntityAssembler te, int id)
		{
			this.te = te;
			this.id = id;
		}
		
		public void reload()
		{
			float w2 = 0, w3 = 0;
			lastAY = aY;
			lastAZ = aZ;
			
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
				length = 0;
				lastAY = aY;
				lastAZ = aZ;
			}
		}
		
		public void setAim(int x, int y)
		{
			this.x = x;
			this.y = y;
			reload();
		}
		
		public void update(float partialTicks)
		{
			
		}
	}
}
