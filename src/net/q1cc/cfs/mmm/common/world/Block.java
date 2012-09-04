/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common.world;

import java.io.Serializable;
import org.lwjgl.util.Color;

/**
 *
 * @author claus
 */
public class Block implements Cloneable, Serializable{
    private static final long serialVersionUID = 9166001L;

    boolean isSolid;
    
    /**
     * The breitness of the light this Block emits.
     * if 0, this block is not emitting any light.
     * normal Sunlight is 1.0 bright.
     */
    public float brightness;
    public Color color;
    public float lightLevel=1;
    
    public boolean isOpaque;
    public int blockID;
    
    public Block(boolean isSolid,
    float brightness,
    Color color,
    boolean isOpaque,
    int blockID){
        this.isSolid=isSolid;
        this.brightness=brightness;
        this.color=color;
        this.isOpaque=isOpaque;
        this.blockID=blockID;
    }
    
    @Override
    public Block clone() {
        Block newB=null;
        try {
            newB = (Block)super.clone();
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
        //reset unique values like IDs etc.
        return newB;
    }
    
    @Override
    public String toString() {
        return "B"+blockID+","+ (isSolid?"solid,":"")+"emits="+brightness+",light="+lightLevel+",c="+color.toString();
    }
}
