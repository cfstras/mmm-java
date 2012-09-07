/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common.world;

import java.util.Random;
import net.q1cc.cfs.mmm.common.StaticBlock;
import net.q1cc.cfs.mmm.common.math.Vec3d;
import net.q1cc.cfs.mmm.common.math.Vec3f;

/**
 * This is a WorldGenerator.
 * It generates cubes of map, able to generate small ranges of levels 
 * in order to decrease data at points never traveled to but seen at a distance.
 * 
 * The class is abstract, its subclasses implement the specific behavior of various
 * world types.
 * 
 * A WorldGenerator hast to be absolutely deterministic, e.g. create exactly the
 * same world it created when being run a week ago,
 * including Spawnpoint (and Entities/Mobs). This is due to the fact that in a
 * server-client mode, the client doesn't get the generated world sent,
 * he just gets the seed and the changes to the world.
 * 
 * @author claus
 */
abstract public class WorldGenerator {
    
    Random r;
    long seed;
    World world;
    
    public WorldGenerator(World world){
        this.world=world;
        seed=world.worldSeed;
        r=new Random(seed);
    }
    
    /**
     * This method generates blocks for this octree and all children,
     * going as many levels deep as specified.
     * 
     * It has to be noted that this method may be later called with a child of this tree
     * to generate them with further detail.
     * All subtrees (including oc) have to set the isGenerated flag to tell the
     * renderer it can safely render this far.
     * 
     * If the isGenerated flag is set, all children must be checked
     * whether they might need to be generated.
     */
    public abstract void generate(WorldOctree oc,Block[][][] blox,int levels);
    
    /**
     * generates the same way as the normal function does, but generates only the subtrees leading to the position and level specified.
     * @param oc
     * @param levels 
     */
    public void generateInto(WorldOctree oc,Block[][][] blox,Vec3d position, int toLevel) {
        generate(oc,blox,0);
        if(toLevel<oc.subtreeLvl) {
            generateInto(WorldOctree.getOctreeAt(position, oc.subtreeLvl-1, oc),blox,position,toLevel);
        }
    }
    
    public Vec3d spawnPoint() {
        r.setSeed(seed ^ 0x4746353579aa2bL); //some random.org value
        double maxXZ= (WorldOctree.getSidelength(WorldOctree.highestSubtreeLvl-1));
        
        Vec3d sp=new Vec3d( r.nextDouble()*maxXZ, 5 ,r.nextDouble()*maxXZ );
        //TODO move as high as needed to spawn properly
        //and select another point if this takes too long
        
        //just for show, spawn a torch there.
        WorldOctree w= WorldOctree.getOctreeAt(sp, 0, world.generateOctree);
        if(w.block==null){
            w.block = new Chunklet((int)w.position.x, (int)w.position.y, (int)w.position.z, w);
        }
        w.block.blocks[Chunklet.getBlockIndex(sp)] = StaticBlock.TORCH;
        System.out.println("spawnpoint: "+sp);
        System.out.println("spawnTree: "+w);
        
        return sp;
    }
    
}
