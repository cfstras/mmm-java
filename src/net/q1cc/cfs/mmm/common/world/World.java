/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common.world;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import net.q1cc.cfs.mmm.client.Client;
import net.q1cc.cfs.mmm.client.render.WorkerTaskPool;
import net.q1cc.cfs.mmm.common.Player;
import net.q1cc.cfs.mmm.common.math.Cubed;
import net.q1cc.cfs.mmm.common.math.Vec3d;
import net.q1cc.cfs.mmm.common.math.Vec3f;

/**
 * This class represents a World.
 * @author claus
 */
public class World implements Serializable{
    
    public boolean exiting=false;    
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
    //public WorldOctree generateOctree;
    //screw you.
    public ArrayList<Chunklet> generateChunklets;
    
    
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
    
    public WorldProvider worldProvider;
    
    protected long worldSeed;
    
    public Vec3d spawnPoint;
    
    public Player player;
    
    public ChunkletManager chunkletManager;
    
    public World(File folder, WorldOctree changedOctree, WorldGeneratorType type, long worldSeed){
        this.folder=folder;
        //this.changedOctree=changedOctree;
        this.worldType=type;
        this.worldSeed=worldSeed;
        
        double m = -WorldOctree.getSidelength(WorldOctree.highestSubtreeLvl)/2;
        //generateOctree=new WorldOctree(new Vec3d(m,m,m));
        generateChunklets = new ArrayList<Chunklet>(128);
        
        this.worldProvider=getGenerator();
        
        this.spawnPoint=worldProvider.spawnPoint(); //call now, because spawnPoint sets a torch
        player=new Player();
        
        player.position=new Vec3f(spawnPoint);
        player.position.y+=1+1.72f; //1m for the block, 1.72m player height
        player.position.x+=0.5f;
        player.position.z+=0.5f;
        player.rotation.y+=30; //look slightly down
        if(changedOctree==null){
            initChangedOctree();
        }
    }
    
    /**
     * gets the chunklet which a given point resides in.
     * 
     * @param point
     * @return null, if that one is not in memory
     */
    public Chunklet getChunkletAt(Vec3d point) {
        //TODO make this smarter
        
        for(Chunklet c: generateChunklets) {
            if(Cubed.intersectsCorner(point, c.posX, c.posY,c.posZ, Chunklet.csl)) {
                return c;
            }
        }
        
        return null;
    }
    
    private WorldGenerator getGenerator(){
        WorldGenerator wg=null;
        if(worldType == WorldGeneratorType.EarthGenerator){
            wg = new EarthGenerator(this, Client.instance.taskPool);
        }
        
        return wg;
    }

    private void initChangedOctree() {
        //here is where the second world could go
    }

    public void unload() {
        exiting=true;
        chunkletManager.unload();
        //TODO save any nonsaved chunklets
    }
    
}
