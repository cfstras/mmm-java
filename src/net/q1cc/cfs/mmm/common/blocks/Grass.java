/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common.blocks;

import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author cfstras
 */
public class Grass extends BlockInfo {
    Grass(int id, String name, float height,
            boolean isOpaque, boolean isVisible, boolean isSolid,
            float lightEmitted, float viscosity,
            int texTop, int texBot, int texLeft, int texRight, int texFront, int texBack)
    {
        super(id,name,height,isOpaque,isVisible,isSolid,lightEmitted,viscosity,texTop,texBot,texLeft,texRight,texFront,texBack);
    }
   
    @Override
    public ReadableColor getColor() {
        //TODO do biome color calculations here
        return new Color(140, 255, 100, 255);
    }
}
