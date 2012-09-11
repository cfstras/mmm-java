/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common.world;

import java.io.Serializable;
import net.q1cc.cfs.mmm.common.blocks.BlockInfo;
import net.q1cc.cfs.mmm.common.math.Vec3d;
import net.q1cc.cfs.mmm.common.math.Vec3f;

/**
 * A Chunklet is a 16x16x16 part of the map. It has a specified position (-x,-y,-z)
 * and an array of underneath blocks.
 * Future: run-length encoding.
 * @author cfstras
 */
public class Chunklet implements Serializable {
    
    /**
     * the side length of a chunklet. use this instead of 16, if it ever changes.
     */
    public static final int csl = 16;
    /**
     * the side length to the power of 2.
     */
    public static final int csl2 = csl*csl;
    
    /**
     * x, y and z position of this chunklets corner.
     */
    public int posX, posY, posZ;
    
    /**
     * the number of blocks inside this chunklet which are not air.
     * if not determined, is -1.
     */
    public int blocksInside = -1;
    
    /**
     * 16x16x16 blocks in this chunklet.
     * access: blocks[x + y*csl + z*csl2] = blah.
     */
    public Block[] blocks = new Block[csl*csl*csl];
    
    public final WorldOctree parent;
    
    public Chunklet(int posX, int posY, int posZ, WorldOctree parent){
        this.posX=posX; this.posY=posY; this.posZ=posZ;
        this.parent=parent;
    }
    
    public static int getBlockIndex(float x, float y,float z) {
        int pX = (int)(x+csl)%csl;
        int pY = (int)(y+csl)%csl;
        int pZ = (int)(z+csl)%csl;
        
        return pX + pY *csl + pZ*csl2;
    }
    public static int getBlockIndex(Vec3f pos) {
        return getBlockIndex(pos.x,pos.y,pos.z);
    }
    public static int getBlockIndex(Vec3d pos) {
        return getBlockIndex((float)pos.x,(float)pos.y,(float)pos.z);
    }

    protected boolean faceIsHidden(Block b, int ix, int iy, int iz, int side, boolean uptodate) {
        if (b.adjecentOpaquesCalculated) {
            return b.adjecentOpaques[side];
        }
        boolean h = false;
        if (side == 0) {
            //top
            h = hasOpaqueBlock(ix, iy + 1, iz, true);
        } else if (side == 1) {
            //bot
            h = hasOpaqueBlock(ix, iy - 1, iz, true);
        } else if (side == 2) {
            //left
            h = hasOpaqueBlock(ix - 1, iy, iz, true);
        } else if (side == 3) {
            //right
            h = hasOpaqueBlock(ix + 1, iy, iz, true);
        } else if (side == 4) {
            //front
            h = hasOpaqueBlock(ix, iy, iz + 1, true);
        } else if (side == 5) {
            //back
            h = hasOpaqueBlock(ix, iy, iz - 1, true);
        } else {
            System.out.println("wtf");
        }
        b.adjecentOpaques[side] = h;
        return h;
    }

    protected boolean hasOpaqueBlock(int ix, int iy, int iz, boolean couldBeOutside) {
        if (couldBeOutside && (ix >= csl || ix < 0 || iy >= csl || iy < 0 || iz >= csl || iz < 0)) {
            //check adjecent octree nodes
            int adjSide = -1;
            if (iy >= csl) {
                //top
                adjSide = 0;
                iy %= csl;
            } else if (iy < 0) {
                //bot
                adjSide = 1;
                iy += csl;
            } else if (ix < 0) {
                //left
                adjSide = 2;
                ix += csl;
            } else if (ix >= csl) {
                //right
                adjSide = 3;
                ix %= csl;
            } else if (iz >= csl) {
                //front
                adjSide = 4;
                iz %= csl;
            } else if (iz < 0) {
                //back
                adjSide = 5;
                iz += csl;
            } else {
                System.out.println("wtf");
            }
            WorldOctree otheroc = parent.getAdjecent(adjSide);
            if (otheroc == null) {
                return false;
            }
            if (otheroc.block == null) {
                return false;
            }
            return otheroc.block.hasOpaqueBlock(ix, iy, iz, false);
        }
        Block b = blocks[ix + iy * Chunklet.csl + iz * Chunklet.csl2];
        if (b == null) {
            return false;
        }
        if (BlockInfo.blocks[b.blockID].isOpaque) {
            return true;
        }
        return false;
    }
}
