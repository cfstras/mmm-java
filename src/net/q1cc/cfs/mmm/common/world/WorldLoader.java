/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common.world;

import java.io.File;
import net.q1cc.cfs.mmm.common.math.Vec3d;

/**
 *
 * @author claus
 */
public class WorldLoader {

    public static World load(File folder) {
        //TODO load world from file using WorldLoader
        
        //generate/load seed. 0L is a fairly random value i generated with a dice.
        long worldSeed=0L;
        //for now, just generate a new World
        World w=new World(folder, new WorldOctree(Vec3d.NULL),
                WorldGeneratorType.EarthGenerator, worldSeed);
        
        return w;
    }
    
    public static String save(World world, File folder){
        
        return "";
    }
    
}
