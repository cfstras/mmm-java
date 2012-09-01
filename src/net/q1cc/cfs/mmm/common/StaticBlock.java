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
    public static final Block AIR= new Block(false,0.0f,false,0,0,null, new Color(0, 0, 0, 0),false);
    
    public static final Block TORCH= new Block(false,0.7f,false,0,0,null, new Color(240, 220, 0, 100),true);
    
    public static final Block DIRT= new Block(true,0.0f,false,0,0,null, new Color(115, 127, 70, 255),true);
    public static final Block GRASS= new Block(true,0.0f,false,0,0,null, new Color(20, 230, 0, 255),true);
    public static final Block STONE= new Block(true,0.0f,false,0,0,null, new Color(150, 150, 150, 255),true);
    
}
