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
                if(levels>=7)
                    System.out.println("gen: l="+levels+" i="+i);
            }
        } else {
            //TODO implent funky sinewaves, iFFT's, imbapolynoms or other stuff somwhere around this line.
            pos=oc.position;
            if(oc.height>0 && oc.height+Chunklet.csl>0){
                oc.block=null;
                //blox[(int)pos.x][(int)pos.y][(int)pos.z]=null;
            }
            else {
                oc.block = new Chunklet((int)oc.position.x,(int)oc.position.y,(int)oc.position.z);
                generate(oc.block);
            }
        } 
        oc.isGenerated=true;
    }
    public void generate(Chunklet c) {
        for (int x = 0; x < Chunklet.csl; x++) {
            for (int y = 0; y < Chunklet.csl; y++) {
                for (int z = 0; z < Chunklet.csl; z++) {
                    if (c.posY + y == 0) {
                        c.blocks[x + Chunklet.csl * y + Chunklet.csl2 * z] = StaticBlock.GRASS;
                    } else if (c.posY + y < 0 && c.posY + y > -30) {
                        c.blocks[x + Chunklet.csl * y + Chunklet.csl2 * z] = StaticBlock.STONE;
                    }

                }
            }
        }
    }
}
