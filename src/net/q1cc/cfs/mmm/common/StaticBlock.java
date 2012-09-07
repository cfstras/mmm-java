/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common;

import net.q1cc.cfs.mmm.common.world.Block;
import org.lwjgl.util.Color;

/**
 *
 * @author claus
 */
public class StaticBlock {
    
    /**
     * Might better not use that one - just use NULL.
     */
    //public static final Block AIR= new Block(false,0.0f,new Color(0, 0, 0, 0),false);
    
    public static final Block TORCH= new Block(false,0.7f, new Color(240, 220, 0, 100),false,0);
    
    public static final Block DIRT= new Block(true,0.0f, new Color(255, 255, 255, 255),true,1);
    public static final Block GRASS= new Block(true,0.0f, new Color(140, 255, 100, 255),true,2);
    public static final Block STONE= new Block(true,0.0f, new Color(255, 255, 255, 255),true,0);
    
}
