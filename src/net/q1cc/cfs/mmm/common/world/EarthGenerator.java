/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common.world;

import net.q1cc.cfs.mmm.common.StaticBlock;
import net.q1cc.cfs.mmm.common.math.Vec3d;

/**
 *
 * @author claus
 */
public class EarthGenerator extends WorldGenerator{
    
    
    public EarthGenerator(World world) {
        super(world);
    }
    
    @Override
    public void generate(WorldOctree oc,Block[][][] blox, int levels) {
        //TODO get rid of that recursion, it's damn slow. stupid java.
        Vec3d pos=null;
        if(levels>0){
            for(int i=0;i<8;i++){
                generate(oc.getSubtree(i),blox,levels-1);
                if(levels>=5)
                    System.out.println("gen: l="+levels+" i="+i);
            }
        } else {
            //TODO implent funky sinewaves, iFFT's, imbapolynoms or other stuff somwhere around this line.
            pos=oc.position;
            if(oc.height>5 && oc.height+Chunklet.csl>5){
                oc.block=null;
                //blox[(int)pos.x][(int)pos.y][(int)pos.z]=null;
            }
            else {
                oc.block = new Chunklet((int)oc.position.x,(int)oc.position.y,(int)oc.position.z,oc);
                generate(oc.block);
            }
        } 
        oc.isGenerated=true;
    }
    public void generate(Chunklet c) {
        for (int x = 0; x < Chunklet.csl; x++) {
            for (int y = 0; y < Chunklet.csl; y++) {
                for (int z = 0; z < Chunklet.csl; z++) {
                    if (c.parent.height + y > -5 && c.parent.height + y < 5) {
                        c.blocks[x + Chunklet.csl * y + Chunklet.csl2 * z] = StaticBlock.GRASS;
                    } else if (c.parent.height + y < -5 && c.parent.height + y > -100) {
                        c.blocks[x + Chunklet.csl * y + Chunklet.csl2 * z] = StaticBlock.STONE;
                    }

                }
            }
        }
    }
}