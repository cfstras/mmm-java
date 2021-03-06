package net.q1cc.cfs.mmm.common.world;

import java.util.Random;
import net.q1cc.cfs.mmm.client.Client;
import net.q1cc.cfs.mmm.client.render.MainGLRender;
import net.q1cc.cfs.mmm.client.render.WorkerTask;
import net.q1cc.cfs.mmm.client.render.WorkerTaskPool;
import net.q1cc.cfs.mmm.common.blocks.BlockInfo;
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
abstract public class WorldGenerator extends WorldProvider {
    
    public WorldGenerator(World world,WorkerTaskPool taskPool){
        super(world,taskPool);
    }
    
//    @Override
//    public void provideSubtree(WorldOctree oc, Vec3f position){
//        new WorkerTaskImpl(oc, position).doWork();
//    }
    
    public Chunklet provideChunklet(int x, int y, int z) {
        return generate(x, y, z);
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
//    public abstract void generate(WorldOctree oc,int levels);
    public abstract Chunklet generate(int x, int y, int z);
    
    /**
     * generates the same way as the normal function does, but generates only the subtrees leading to the position and level specified.
     * @param oc
     * @param levels 
     */
//    public void generateInto(WorldOctree oc,Vec3f position, int toLevel) {
//        if(toLevel==oc.subtreeLvl) {
//            generate(oc,toLevel);
//        } else if(toLevel<oc.subtreeLvl) {
//            generateInto(WorldOctree.getOctreeAt(new Vec3d(position), oc.subtreeLvl-1, oc,true),position,toLevel);
//        }
//    }

//    private class WorkerTaskImpl implements WorkerTask {
//
//        private final WorldOctree oc;
//        private final Vec3f position;
//
//        public WorkerTaskImpl(WorldOctree oc, Vec3f position) {
//            this.oc = oc;
//            this.position = position;
//        }
//
//        @Override
//        public int getPriority() {
//            return WorkerTask.PRIORITY_NORM;
//        }
//
//        @Override
//        public boolean doWork() {
//            generateInto(oc,position,0);
//            Client.instance.renderer.recursePrepare(oc, false);
//            return true;
//        }
//    }
    
}
