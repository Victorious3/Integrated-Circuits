package vic.mod.integratedcircuits.ic.part;

import java.util.ArrayList;

import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.ic.CircuitPart;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.CraftingAmount;
import vic.mod.integratedcircuits.misc.ItemAmount;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.misc.PropertyStitcher.IntProperty;
import vic.mod.integratedcircuits.misc.Vec2;

import com.google.common.collect.Lists;

/** Rotateable Part **/
public abstract class PartCPGate extends CircuitPart
{
	public final IntProperty PROP_ROTATION = new IntProperty(stitcher, 3);
	
	public final int getRotation(Vec2 pos, ICircuit parent)
	{
		return getProperty(pos, parent, PROP_ROTATION);
	}
	
	public final void setRotation(Vec2 pos, ICircuit parent, int rotation)
	{
		setProperty(pos, parent, PROP_ROTATION, rotation);
		notifyNeighbours(pos, parent);
	}
	
	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) 
	{
		if(button == 0 && !ctrl)
			cycleProperty(pos, parent, PROP_ROTATION);
		notifyNeighbours(pos, parent);
	}
	
	public ForgeDirection toInternal(Vec2 pos, ICircuit parent, ForgeDirection dir)
	{
		return MiscUtils.rotn(dir, -getRotation(pos, parent));
	}
	
	public ForgeDirection toExternal(Vec2 pos, ICircuit parent, ForgeDirection dir)
	{
		return MiscUtils.rotn(dir, getRotation(pos, parent));
	}

	@Override
	public ArrayList<String> getInformation(Vec2 pos, ICircuit parent, boolean edit, boolean ctrlDown) 
	{
		ArrayList<String> text = Lists.newArrayList();
		ForgeDirection rot = MiscUtils.getDirection(getRotation(pos, parent));
		text.add(EnumChatFormatting.DARK_GRAY + "" + EnumChatFormatting.ITALIC + MiscUtils.getLocalizedDirection(rot));
		if(edit && !ctrlDown) text.add(I18n.format("gui.integratedcircuits.cad.rotate"));
		return text;
	}

	@Override
	public void onScheduledTick(Vec2 pos, ICircuit parent) 
	{
		notifyNeighbours(pos, parent);
	}

	@Override
	public void getCraftingCost(CraftingAmount cost) 
	{
		cost.add(new ItemAmount(Items.redstone, 0.048));
		cost.add(new ItemAmount(IntegratedCircuits.itemSiliconDrop, 0.1));
	}
}