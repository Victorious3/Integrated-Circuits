package vic.mod.integratedcircuits;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockAssembler extends BlockContainer
{
	private IIcon front_off, front_on, back, side, bottom, top;
	
	protected BlockAssembler() 
	{
		super(Material.iron);
		setBlockName(IntegratedCircuits.modID + ".assembler");
		setCreativeTab(CreativeTabs.tabMisc);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) 
	{
		return new TileEntityAssembler();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int s, int meta) 
	{
		switch(s) {
		case 0 : return bottom;
		case 1 : return top;
		case 2 : return front_on;
		case 3 : return back;
		default : return side;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir) 
	{
		front_off = ir.registerIcon(IntegratedCircuits.modID + ":assembler_front_off");	
		front_on = ir.registerIcon(IntegratedCircuits.modID + ":assembler_front_on");
		back = ir.registerIcon(IntegratedCircuits.modID + ":assembler_back");
		bottom = ir.registerIcon(IntegratedCircuits.modID + ":assembler_bottom");
		top = ir.registerIcon(IntegratedCircuits.modID + ":assembler_top");
		side = ir.registerIcon(IntegratedCircuits.modID + ":assembler_side");
	}
	
	@Override
	public boolean isOpaqueCube() 
	{
		return false;
	}
}
