/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common.world;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import net.q1cc.cfs.mmm.client.Client;
import net.q1cc.cfs.mmm.common.Player;
import net.q1cc.cfs.mmm.common.math.Vec3d;
import net.q1cc.cfs.mmm.common.math.Vec3f;

/**
 * This class represents a World.
 * @author claus
 */
public class World implements Serializable{
    
    /**
     * This is the root generated Octree, level 10
     * it's 2^10m on each side
     * and will be replaced by some sort of array of trees in the future.
     * 
     * It contains solely generated data and is unique for every given seed.
     * 
     * it's coordinates are 0,0,0 and it's most far coordinate is 1024,1024,1024,
     * although the outer block on tree level 0 has the coordinates 1023,1023,1023,
     * but outerSubtree(outerSubtree([...]outerSubtree.coords)[...]) == 1024,1024,1024
     */
    public WorldOctree generateOctree;
   
    /**
     * This is the changed root Octree.
     * other than the generated Octree, it only contains subtrees/blocks
     * where players or other things have made changes to the original world.
     * on an unchanged world, this has no children and no block.
     */
    //public WorldOctree changedOctree;
    
    /**
     * This folder holds the world data.
     * if we are in client mode, it will most likely be null or hold cache data.
     */
    public File folder;
    
    /**
     * This determines the world type.
     * Like Earth, Heaven, Hell or Candyland
     */
    protected WorldGeneratorType worldType;
    
    protected WorldGenerator worldGenerator;
    
    protected long worldSeed;
    
    public Vec3d spawnPoint;
    
    public Player player;
    
    public World(File folder, WorldOctree changedOctree, WorldGeneratorType type, long worldSeed){
        this.folder=folder;
        //this.changedOctree=changedOctree;
        this.worldType=type;
        this.worldSeed=worldSeed;
        
        double m = -WorldOctree.getSidelength(WorldOctree.highestSubtreeLvl)/2;
        generateOctree=new WorldOctree(new Vec3d(m,m,m));
        
        this.worldGenerator=getGenerator();
        
        //blox=new Block[1024][1024][1024];
        //fill it with flat mountains.
        //TODO convert this to workers
        worldGenerator.generate(generateOctree,null, WorldOctree.highestSubtreeLvl); //just some sublevels for now, to be inaccurate.
        
        this.spawnPoint=worldGenerator.spawnPoint(); //call now, because spawnPoint sets a torch
        player=new Player();
        
        player.position=new Vec3f(spawnPoint);
        
        if(changedOctree==null){
            initChangedOctree();
        }
    }
    
    private WorldGenerator getGenerator(){
        Class<?> wgclass=null;
        try {
            wgclass=(Class<?>)Class.forName("net.q1cc.cfs.mmm.common.world."+worldType.name());
        } catch (Exception ex){
            System.err.println("Error: could not find World Generator for worldType "+worldType.name()+". Using Earth.");
            ex.printStackTrace();
            wgclass=EarthGenerator.class;
        }
        WorldGenerator wg = null;
        try {
            wg = (WorldGenerator)wgclass.getConstructor(World.class).newInstance(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return wg;
    }

    private void initChangedOctree() {
        WorldOctree wo= new WorldOctree(Vec3d.NULL);
        //changedOctree=wo;
    }
    
}
