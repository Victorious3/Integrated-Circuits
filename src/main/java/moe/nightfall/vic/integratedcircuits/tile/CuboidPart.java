package moe.nightfall.vic.integratedcircuits.tile;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.TMultiPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderBlocks;
/*
copy of JCuboidPart used to load up the mod normally, probably breaks FMPSupport though
required because TCuboidPart doesnt use the SideOnly annotation
 */
public abstract class CuboidPart extends TMultiPart implements ICuboidPart {
    public Iterable<IndexedCuboid6> getSubParts() {
        return super.getSubParts();
    }

    public Iterable<Cuboid6> getCollisionBoxes() {
        return super.getCollisionBoxes();
    }

    @SideOnly(Side.CLIENT)
    public void drawBreaking(RenderBlocks renderBlocks) {
        super.drawBreaking(renderBlocks);
    }

    public CuboidPart() {
        super();
    }
}
