/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common.world;

import java.io.Serializable;
import net.q1cc.cfs.mmm.common.blocks.BlockInfo;
import org.lwjgl.util.Color;

/**
 *
 * @author claus
 */
public class Block implements Cloneable, Serializable{
    private static final long serialVersionUID = 9166001L;
    
    /**
     * whether this block is solid, affecting physics calculations.
     * calculated from block ID, not to be serialized.
     */
    //boolean isSolid;
    
    /**
     * The breitness of the light this Block emits.
     * if 0, this block is not emitting any light.
     * normal Sunlight is 1.0 bright.
     */
    //public float brightness;
    
    /**
     * the color of this block, multiplied to the texture.
     * //TODO replace this with additionalData values, it saves memory.
     */
    //public Color color;
    
    /**
     * the computed light level for this block, measured at its center.
     * not to be serialized.
     */
    public float lightLevel=1;
    
    /**
     * whether the block is opaque and blocks adjecent blocks from sight.
     * calculated from block ID, not to be serialized
     */
    //public boolean isOpaque;
    
    /**
     * the block ID, deciding texture and block behaviour
     */
    public int blockID;
    
    /**
     * an array which stores whether the blocks adjecent to this one are opaque.
     * gets calculated whenever GLChunklet reloads.
     * 0: top
     * 1: bottom
     * 2: left
     * 3: right
     * 4: front
     * 5: back
     */
    public boolean[] adjecentOpaques;
    
    /**
     * tells if the adjecentOpaques array is up-to-date
     */
    public boolean adjecentOpaquesCalculated;

    public Block(//boolean isSolid,
    //float brightness,
    //Color color,
    //boolean isOpaque,
    int blockID){
        //this.isSolid=isSolid;
        //this.brightness=brightness;
        //this.color=color;
        //this.isOpaque=isOpaque;
        this.blockID=blockID;
        adjecentOpaques=new boolean[6];
    }
    
    @Override
    public Block clone() {
        Block newB = new Block(blockID);
        for(int i=0;i<6;i++){
            newB.adjecentOpaques[i]=adjecentOpaques[i];
        }
        newB.adjecentOpaquesCalculated=adjecentOpaquesCalculated;
        //reset unique values like IDs etc.
        return newB;
    }
    
    @Override
    public String toString() {
        //return "B"+blockID+","+ (isSolid?"solid,":"")+"emits="+brightness+",light="+lightLevel+",c="+color.toString();
        return "B"+BlockInfo.blocks[blockID].toString();
    }
}
