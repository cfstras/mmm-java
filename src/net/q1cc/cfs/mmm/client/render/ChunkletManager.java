/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.client.render;

import java.nio.ByteBuffer;
import net.q1cc.cfs.mmm.client.Client;
import net.q1cc.cfs.mmm.common.MemUtil;
import net.q1cc.cfs.mmm.common.Player;
import net.q1cc.cfs.mmm.common.math.Vec3f;
import net.q1cc.cfs.mmm.common.world.Chunklet;
import net.q1cc.cfs.mmm.common.world.WorldOctree;

/**
 *
 * @author cfstras
 */
class ChunkletManager implements WorkerTask {

    /*
     * initial viewDistance, in chunklets.
     * update with updateViewDistance()
     */
    private static int viewDist = 32;
    
    private static int viewDistSqM;
    private static int cslh;
    static float maxWalkDistanceSq;
    
    MainGLRender render;
    Player player;
    Vec3f lastLoadPlayerPosition;
    
    public ChunkletManager(MainGLRender render) {
        this.render = render;
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
        if(render.exiting){
            return false;
        }
        if(player == null) {
            player = render.player;
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
            walkTree(render.world.generateOctree);
            //System.gc(); //TODO do some gc after removing chunks, or not.
            //debugChunks();
        }
        //TODO add this task to the taskpool whenever the player moves.
        return true;
    }

    private void walkTree(WorldOctree oc) {
        checkNode(oc);
        if(oc.hasSubtrees){
            for(WorldOctree s:oc.subtrees) {
                if(s!=null){
                    walkTree(s);
                }
            }
        } //else { //TODO delete and form new trees
            //if(oc.block==null){
            //    oc.parent.subtrees[oc.getSPID()]=null;
            //    oc.checkForSubtrees();
            //}
        //}
    }
    void checkNode(WorldOctree oc) {
        Vec3f mid = new Vec3f((float)oc.position.x+cslh,(float)oc.position.y+cslh,(float)oc.position.z+cslh);
        float distSq = Vec3f.subtract(lastLoadPlayerPosition,mid).lengthSquared();
        if(distSq>viewDistSqM){
            if(oc.block!=null) {
                removeNode(oc);
            }
        } else {
            loadNode(oc,mid);
        }
    }
    
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
        if(g.parent!=null) {
            g.parent.block=null;
        }
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

    private void loadNode(WorldOctree oc,Vec3f mid) {
        GLChunklet glc;
        Chunklet c = oc.block;
        if(c==null){
            render.world.worldProvider.provideSubtree(oc, mid);
        } else if(c instanceof GLChunklet) {
            glc = (GLChunklet)c;
            glc.build();
        } else {
            glc = new GLChunklet(c);
            oc.block=glc;
            glc.build();
        }
    }

    private void debugChunks() {
        //list all chunklets buffered
        System.out.println("Chunklets buffered:");
        for(GLChunklet g:render.chunksBuffered) {
            System.out.println(g);
        }
        //list all chunklets in tree
        System.out.println("\nChunklets in tree:"); 
        soutTree(render.world.generateOctree, "");
    }

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
    
}
