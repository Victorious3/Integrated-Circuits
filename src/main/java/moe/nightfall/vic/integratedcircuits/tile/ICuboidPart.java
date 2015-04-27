package moe.nightfall.vic.integratedcircuits.tile;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.vec.Cuboid6;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderBlocks;
/*
copy of TCuboidPart used to load up the mod normally, probably breaks FMPSupport though
required because TCuboidPart doesnt use the SideOnly annotation
 */
public interface ICuboidPart {
    Cuboid6 getBounds();

    Iterable<IndexedCuboid6> getSubParts();

    Iterable<Cuboid6> getCollisionBoxes();

    @SideOnly(Side.CLIENT)
    void drawBreaking(RenderBlocks var1);
}
