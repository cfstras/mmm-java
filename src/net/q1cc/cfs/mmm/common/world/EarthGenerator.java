/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common.world;

import java.util.Random;
import net.q1cc.cfs.mmm.client.render.WorkerTaskPool;
import net.q1cc.cfs.mmm.common.blocks.BlockInfo;
import net.q1cc.cfs.mmm.common.math.SimplexNoise;
import net.q1cc.cfs.mmm.common.math.Vec3d;

/**
 *
 * @author claus
 */
public class EarthGenerator extends WorldGenerator {
    
    
    public EarthGenerator(World world,WorkerTaskPool taskPool) {
        super(world,taskPool);
    }
    
    @Override
    public void generate(WorldOctree oc, int levels) {
        //TODO get rid of that recursion, it's damn slow. stupid java.
        Vec3d pos=null;
        if(levels>0){
            for(int i=0;i<8;i++){
                generate(oc.getSubtree(i,true),levels-1);
                if(levels>=WorldOctree.highestSubtreeLvl)
                    System.out.println("gen: l="+levels+" i="+i);
            }
        } else {
            pos=oc.position;
            if(oc.height>5 && oc.height+Chunklet.csl>5){
                oc.block=null;
            }
            else {
                oc.block = new Chunklet((int)oc.position.x,(int)oc.position.y,(int)oc.position.z,oc);
                generate(oc.block);
            }
        } 
        oc.isGenerated=true;
    }
    
    public void generate(Chunklet c) {
        SimplexNoise sn = new SimplexNoise(new Random(world.worldSeed));
        for (int x = 0; x < Chunklet.csl; x++) {
            for (int y = 0; y < Chunklet.csl; y++) {
                for (int z = 0; z < Chunklet.csl; z++) {
                    c.blocks[x + Chunklet.csl * y + Chunklet.csl2 * z]
                    = generateBlock(x+c.posX,y+c.posY,z+c.posZ,c.parent.height+c.posY,sn);

                }
            }
        }
    }
    Block generateBlock(int x, int y, int z, double height, SimplexNoise sn) {
        //TODO do biomes here
        
        double h = sn.noise(x/80.0,y/80.0,z/80.0)-(y/70.0)-1;
        if(h>0) return BlockInfo.get(BlockInfo.GRASS);
        
        return null;
        //return generateHills(x,y,z,height,sn);
    }
    
    Block generateHills(int x, int y, int z, double height, SimplexNoise sn) {
        //double dd = noise(x/1000.0, y/1000.0, z/1000.0)*20;
        double d = sn.noise(x/100.0, z/100.0)*1 - sn.noise(x/400.0+800, y/100.0, z/400.0+100)*3 - (y/10.0+1);
        if (d>0 && d<0.4) {
            return BlockInfo.get(BlockInfo.GRASS);
        } else if (d>=0.4 && d<1.0) {
            return BlockInfo.get(BlockInfo.DIRT);
        } else if (d>=1.0 && d<10) {
            return BlockInfo.get(BlockInfo.ROCK);
        } else if(d>=10) {
            return BlockInfo.get(BlockInfo.SAND);
        }
        
        return null;
    }
}
