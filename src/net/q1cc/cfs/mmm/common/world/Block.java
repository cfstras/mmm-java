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

    boolean isSolid;
    
    /**
     * The breitness of the light this Block emits.
     * if 0, this block is not emitting any light.
     * normal Sunlight is 1.0 bright.
     */
    public float brightness;
    public boolean isSizeLimited;
    public int minOctreeLvl;
    public int maxOctreeLvl;
    public String texture_filename;
    public Color color;
    public float lightLevel;
    
    public boolean isOpaque;
    
    public Block(boolean isSolid,
    float brightness,
    boolean isSizeLimited,
    int minOctreeLvl,
    int maxOctreeLvl,
    String texture_filename,
    Color color,boolean isOpaque){
        this.isSolid=isSolid;
        this.brightness=brightness;
        this.isSizeLimited=isSizeLimited;
        this.minOctreeLvl=minOctreeLvl;
        this.maxOctreeLvl=maxOctreeLvl;
        this.texture_filename=texture_filename;
        this.color=color;
        this.isOpaque=isOpaque;
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
        return "B,"+ (isSolid?"solid,":"")+"lumen="+brightness+",light="+lightLevel+",c="+color.toString();
    }
}
