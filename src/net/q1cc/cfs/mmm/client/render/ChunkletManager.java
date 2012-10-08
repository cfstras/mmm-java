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
    
    public static int viewDist; // in chunklets
    private static int viewDistSqM;
    private static int cslh;
    static float maxWalkDistanceSq;
    
    MainGLRender render;
    Player player;
    Vec3f lastLoadPlayerPosition;
    
    public ChunkletManager(MainGLRender render) {
        this.render = render;
        updateViewDistance(32);
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
        }
        //TODO add this task to the taskpool whenever the player moves.
        return true;
    }

    private void walkTree(WorldOctree oc) {
        Vec3f mid = new Vec3f((float)oc.position.x+cslh,(float)oc.position.y+cslh,(float)oc.position.z+cslh);
        float distSq = Vec3f.subtract(lastLoadPlayerPosition,mid).lengthSquared();
        if(distSq>viewDistSqM){
            removeNode(oc);
        } else {
            loadNode(oc,mid);
        }
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

    private void removeNode(WorldOctree oc) {
        GLChunklet glc;
        Chunklet c = oc.block;
        if(c==null) {
            return;
        }
        if(c instanceof GLChunklet) {
            glc = (GLChunklet)c;
            glc.cleanupVRAMCache();
            glc.cleanupCache();
        } else {
            c = null;
        }
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
    
}
