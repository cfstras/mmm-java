/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common.world;

import java.util.Random;
import net.q1cc.cfs.mmm.client.render.WorkerTaskPool;
import net.q1cc.cfs.mmm.common.blocks.BlockInfo;
import net.q1cc.cfs.mmm.common.math.Vec3d;
import net.q1cc.cfs.mmm.common.math.Vec3f;

/**
 *
 * @author cfstras
 */
public abstract class WorldProvider {
    Random r;
    long seed;
    WorkerTaskPool taskPool;
    World world;
    
    public WorldProvider(World world,WorkerTaskPool taskPool) {
        this.taskPool = taskPool;
        this.world=world;
        seed = world.worldSeed;
        r=new Random(seed);
    }
    
//    public abstract void provideSubtree(WorldOctree oc, Vec3f position);
    
    public Vec3d spawnPoint() {
        r.setSeed(seed ^ 20343817869306411L); //some random.org value
        double maxXZ = WorldOctree.getSidelength(WorldOctree.highestSubtreeLvl - 1);
        Vec3d sp = new Vec3d(r.nextDouble() * maxXZ, -15, r.nextDouble() * maxXZ);
        sp.x = Math.round(sp.x); sp.z = Math.round(sp.z);
        //TODO move down/up to ground
        //and select another point if this takes too long
        //just for show, spawn a lightstone there.
        //WorldOctree w = WorldOctree.getOctreeAt(sp, 0, world.generateOctree, true);
        //if (w.block == null) {
        //    w.block = new Chunklet((int) w.position.x, (int) w.position.y, (int) w.position.z, w);
        //}
        //w.block.blocks[Chunklet.getBlockIndex(sp)] = BlockInfo.create(BlockInfo.LIGHTSTONE);
        System.out.println("spawnpoint: " + sp);
        //System.out.println("spawnTree: " + w);
        return sp;
    }

    public abstract Chunklet provideChunklet(int x, int y, int z);
    
}
