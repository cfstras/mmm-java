/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common.world;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import net.q1cc.cfs.mmm.client.Client;
import net.q1cc.cfs.mmm.client.render.GLChunklet;
import net.q1cc.cfs.mmm.client.render.MainGLRender;
import net.q1cc.cfs.mmm.client.render.WorkerTask;
import net.q1cc.cfs.mmm.common.MemUtil;
import net.q1cc.cfs.mmm.common.Player;
import net.q1cc.cfs.mmm.common.math.Vec3f;
import net.q1cc.cfs.mmm.common.world.Chunklet;
import net.q1cc.cfs.mmm.common.world.WorldOctree;

/**
 *
 * @author cfstras
 */
public class ChunkletManager implements WorkerTask {

    /*
     * initial viewDistance, in chunklets.
     * update with updateViewDistance()
     */
    private static int viewDist = 16;
    
    private static int viewDistSqM;
    private static int cslh;
    static float maxWalkDistanceSq;
    
    boolean working;
    //int[] needed;
    
    World world;
    Player player;
    Vec3f lastLoadPlayerPosition;
    
    public ChunkletManager(World world) {
        this.world = world;
        updateViewDistance(viewDist);
    }
    
    public static void updateViewDistance(int distance) {
        viewDist = distance;
        viewDistSqM = distance*Chunklet.csl2;
        maxWalkDistanceSq = Chunklet.csl2;
        cslh = Chunklet.csl/2;
    }

    @Override
    public int getPriority() {
        //only work when all chunklets waiting have been converted
        return WorkerTask.PRIORITY_MIN;
    }

    @Override
    public boolean doWork() {
        
        if(world.exiting){
            return false;
        }
        if(player == null) {
            player = world.player;
            if(player!=null){
                lastLoadPlayerPosition = new Vec3f(player.position);
                lastLoadPlayerPosition.x+=viewDistSqM; //hack to start right now
                //render.taskPool.add(this);
                //return true; //this prevents loading on first run
            } else {
                //render.taskPool.add(this);
                return true;
            }
        }
        if(Vec3f.subtract(lastLoadPlayerPosition, player.position)
                .lengthSquared() >= maxWalkDistanceSq) {
            //we have moved, reload!
            lastLoadPlayerPosition = new Vec3f(player.position);
            
            if (working == true) {
                System.out.println("chunklet manager double-run");
                return false;
            }
            working = true;
            
            //listNeeded();
            removeList(world.generateChunklets);
            //now add some
            addList(world.generateChunklets);
            //System.gc(); //TODO do some gc after removing chunks, or not.
            //debugChunks();
        }
        //TODO add this task to the taskpool whenever the player moves.
        working=false;
        return true;
    }

//    private void walkTree(WorldOctree oc) {
//        checkNode(oc);
//        if(oc.hasSubtrees){
//            for(WorldOctree s:oc.subtrees) {
//                if(s!=null){
//                    walkTree(s);
//                }
//            }
//        } //else { //TODO delete and form new trees
//            //if(oc.block==null){
//            //    oc.parent.subtrees[oc.getSPID()]=null;
//            //    oc.checkForSubtrees();
//            //}
//        //}
//    }
//    void checkNode(WorldOctree oc) {
//        Vec3f mid = new Vec3f((float)oc.position.x+cslh,(float)oc.position.y+cslh,(float)oc.position.z+cslh);
//        float distSq = Vec3f.subtract(lastLoadPlayerPosition,mid).lengthSquared();
//        if(distSq>viewDistSqM){
//            if(oc.block!=null) {
//                removeNode(oc);
//            }
//        } else {
//            loadNode(oc,mid);
//        }
//    }
    
    void checkChunklet(GLChunklet g) {
        Vec3f mid = new Vec3f((float)g.posX+cslh,(float)g.posY+cslh,(float)g.posZ+cslh);
        float distSq = Vec3f.subtract(lastLoadPlayerPosition,mid).lengthSquared();
        if(distSq>viewDistSqM){
            removeChunklet(g);
        } else {
            //loadChunklet(g); //TODO reload Chunklet if still in memory
        }
    }
    
    private void removeChunklet(GLChunklet g) {
        //if(g.parent!=null) {
        //    g.parent.block=null;
        //}
        g.cleanupCache();
        g.cleanupVRAMCache();
    }
    
    private void removeNode(WorldOctree oc) {
        GLChunklet glc;
        Chunklet c = oc.block;
        if(c instanceof GLChunklet) {
            glc = (GLChunklet)c;
            glc.cleanupCache();
            glc.cleanupVRAMCache();
        } else {
            //do something with non-gl chunklets?
        }
        oc.block=null; //TODO sync this call
    }

//    private void loadNode(WorldOctree oc,Vec3f mid) {
//        GLChunklet glc;
//        Chunklet c = oc.block;
//        if(c==null){
//            world.worldProvider.provideSubtree(oc, mid);
//        } else if(c instanceof GLChunklet) {
//            glc = (GLChunklet)c;
//            glc.build();
//        } else {
//            glc = new GLChunklet(c);
//            oc.block=glc;
//            glc.build();
//        }
//    }

    private void soutTree(WorldOctree tree, String tab) {
        if(tree.block!=null) {
            System.out.println(tab+tree.block);
        }
        if(tree.hasSubtrees) {
            //System.out.println(tab+"\\.");
            for (WorldOctree c:tree.subtrees) {
                if(c!=null) {
                    soutTree(c, tab+" |");
                }
            }
            //System.out.println(tab+"/");
        }
    }

    private void removeList(ArrayList<Chunklet> generateChunklets) {
        Vec3f mid;
        Chunklet c;
        Iterator<Chunklet> it = generateChunklets.iterator();
        while(it.hasNext()) {
            c = it.next();
            mid = new Vec3f((float) c.posX + cslh, (float) c.posY + cslh, (float) c.posZ + cslh);
            float distSq = Vec3f.subtract(lastLoadPlayerPosition, mid).lengthSquared();
            if (distSq > viewDistSqM) {
                GLChunklet glc;
                if(c instanceof GLChunklet) {
                    glc = (GLChunklet)c;
                    glc.cleanupCache();
                    glc.cleanupVRAMCache();
                } else {
                    //do something with non-gl chunklets?
                }
                it.remove(); // should we really do that before knowing the chunklet is out of vram and bufferedChunks?
            }
        }
    }

    private void addList(ArrayList<Chunklet> generateChunklets) {
        Vec3f mid;
        int x, y,z;
        GLChunklet glc;
        Chunklet c;
        for(int ix=-viewDist;ix<=viewDist;ix++) {
            for(int iy=-viewDist;iy<=viewDist;iy++) {
                for(int iz=-viewDist;iz<=viewDist;iz++) {
                    x = (ix+(int)lastLoadPlayerPosition.x/16)*16;
                    y = (iy+(int)lastLoadPlayerPosition.y/16)*16;
                    z = (iz+(int)lastLoadPlayerPosition.z/16)*16;
                    mid = new Vec3f((float) ix*Chunklet.csl+cslh+lastLoadPlayerPosition.x,
                            (float) iy*Chunklet.csl+cslh+lastLoadPlayerPosition.y,
                            (float) iz*Chunklet.csl+cslh+lastLoadPlayerPosition.z);
                    
                    float distSq = Vec3f.subtract(lastLoadPlayerPosition, mid).lengthSquared();
                    if (distSq <= viewDistSqM) {
                        c=null;
                        for(Chunklet a:generateChunklets) {
                            if(a.posX == x && a.posY == y && a.posZ == z) {
                                c = a;
                                break;
                            }
                        }
                        if(c==null){
                            c = world.worldProvider.provideChunklet(x,y,z);
                            generateChunklets.add(c);
                        }
                        if(c instanceof GLChunklet) {
                            glc = (GLChunklet)c;
                            if(!glc.awaitingBuild && !glc.built) {
                                glc.build();
                            }
                        } else {
                            System.out.println("Error: wrong object type in mem.\n"
                                    +"maybe your client is outdated?");
                        }
                    } else {
                        //nothing
                    }
                }
            }
        }
    }

    Chunklet getAdjecent(int adjSide) {
        return null; //TODO implement getAdjecent
    }
    
}
