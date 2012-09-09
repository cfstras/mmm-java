/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common.blocks;

import net.q1cc.cfs.mmm.common.world.Block;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author claus
 */
public class BlockInfo {
    //deprecated
    //public static final Block TORCH= new Block(false,0.7f, new Color(240, 220, 0, 100),false,0);
    //public static final Block DIRT= new Block(true,0.0f, new Color(255, 255, 255, 255),true,1);
    //public static final Block GRASS= new Block(true,0.0f, new Color(140, 255, 100, 255),true,2);
    //public static final Block STONE= new Block(true,0.0f, new Color(255, 255, 255, 255),true,0);
    
    //use these.
    public static final BlockInfo[] blocks = {
        ///            ID   Name       Height opaque      solid    viscosity   TEX   right back
        ///                                         visible     light     top bot left front
        new BlockInfo(  0, "Rock"      , 1.0f, true, true, true, 0.0f, 1.0f, 0, 0, 0, 0, 0, 0),
        new BlockInfo(  1, "Dirt"      , 1.0f, true, true, true, 0.0f, 1.0f, 1, 1, 1, 1, 1, 1),
        new Grass    (  2, "Grass"     , 1.0f, true, true, true, 0.0f, 1.0f, 2, 1, 3, 3, 3, 3),
        new BlockInfo(  3, "Lightstone", 1.0f,false, true, true,16.0f, 1.0f,12,12,12,12,12,12),
        new BlockInfo(  4, "Sand"      , 1.0f, true, true, true, 0.0f, 1.0f, 6, 6, 6, 6, 6, 6),
        new BlockInfo(  5, "Wood"      , 1.0f, true, true, true, 0.0f, 1.0f, 8, 8, 7, 7, 7, 7),
        new BlockInfo(  6, "Stone"     , 1.0f, true, true, true, 0.0f, 1.0f, 4, 4, 4, 4, 4, 4),
        new BlockInfo(  7, "Leaves"    , 1.0f, true, true, true, 0.0f, 1.0f, 9, 9, 9, 9, 9, 9),
        new BlockInfo(  8, "Stone"     , 1.0f, true, true, true, 0.0f, 1.0f, 0, 0, 0, 0, 0, 0),
        new BlockInfo(  9, "Stone"     , 1.0f, true, true, true, 0.0f, 1.0f, 0, 0, 0, 0, 0, 0),
        new BlockInfo( 10, "Stone"     , 1.0f, true, true, true, 0.0f, 1.0f, 0, 0, 0, 0, 0, 0),
    };
    
    /* quick accessors */
    public final static int ROCK = 0;
    public final static int DIRT = 1;
    public final static int GRASS = 2;
    public final static int LIGHTSTONE = 3;
    public final static int SAND = 4;
    public final static int WOOD = 5;
    public final static int STONE = 6;
    public final static int LEAVES = 7;    
    
    /* block attributes */
    
    public final String name;
    public final int id;
    public final float height;
    public final boolean isOpaque;
    public final boolean isVisible;
    public final boolean isSolid;
    public final float lightEmitted;
    public final float viscosity;
    public final int texTop;
    public final int texBot;
    public final int texLeft;
    public final int texRight;
    public final int texFront;
    public final int texBack;
    
    /** info methods **/
    public int getTexID(int side){
        if(side==0) return texTop;
        else if(side==1) return texBot;
        else if(side==2) return texLeft;
        else if(side==3) return texRight;
        else if(side==4) return texFront;
        else if(side==5) return texBack;
        else return -1;
    }
    
    /**
     * returns the color to multiply to the texture.
     * should be overridden by Blocks which have different colors
     * @return color
     */
    public ReadableColor getColor() {
        return new Color(255,255,255,255);
    }
    
    /**
     * returns a fresh instance of the given block id.
     * @param id
     * @return 
     */
    public static Block get(int id) {
        //BlockInfo i = blocks[id];
        return new Block(id);
    }
    
    /** internal constructor **/
    protected BlockInfo(int id, String name, float height,
            boolean isOpaque, boolean isVisible, boolean isSolid,
            float lightEmitted, float viscosity,
            int texTop, int texBot, int texLeft, int texRight, int texFront, int texBack)
    {
        this.id = id; this.name=name; this.height=height;
        this.isOpaque=isOpaque;this.isVisible=isVisible;this.isSolid=isSolid;
        this.lightEmitted=lightEmitted; this.viscosity=viscosity;
        this.texTop=texTop; this.texBot=texBot;
        this.texLeft=texLeft; this.texRight=texRight;
        this.texFront=texFront; this.texBack=texBack;
    }
}
