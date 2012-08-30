/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common.world;

import java.io.Serializable;
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
    
    public WorldOctree parent;
    
    public Chunklet(int posX, int posY, int posZ, WorldOctree parent){
        this.posX=posX; this.posY=posY; this.posZ=posZ;
        this.parent=parent;
    }
    
    public static int getBlockIndex(float x, float y,float z) {
        int posX = (int)Math.floor(x/csl);
        int posY = (int)Math.floor(y/csl);
        int posZ = (int)Math.floor(z/csl);
        
        return posX + posY *csl + posZ*csl2;
    }
    public static int getBlockIndex(Vec3f pos) {
        return getBlockIndex(pos.x,pos.y,pos.z);
    }
    public static int getBlockIndex(Vec3d pos) {
        return getBlockIndex((float)pos.x,(float)pos.y,(float)pos.z);
    }
}
