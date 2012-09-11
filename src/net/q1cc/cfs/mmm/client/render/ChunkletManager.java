/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.client.render;

import net.q1cc.cfs.mmm.client.Client;
import net.q1cc.cfs.mmm.common.Player;
import net.q1cc.cfs.mmm.common.math.Vec3f;
import net.q1cc.cfs.mmm.common.world.Chunklet;
import net.q1cc.cfs.mmm.common.world.WorldOctree;

/**
 *
 * @author cfstras
 */
class ChunkletManager implements WorkerTask {
    
    public static int viewDistance; // in chunklets
    private static int viewDistanceM;
    private static int cslh;
    
    MainGLRender render;
    Player player;
    float maxWalkDistance = Chunklet.csl2; //squared
    Vec3f lastLoadPlayerPosition;
    
    public ChunkletManager(MainGLRender render) {
        this.render = render;
        this.player = render.player;
        if(player==null){
            //we are still loading
        } else {
            lastLoadPlayerPosition = new Vec3f(player.position);
        }
        updateViewDistance(16);
    }
    
    public static void updateViewDistance(int distance) {
        viewDistance = distance;
        viewDistanceM = distance*Chunklet.csl2;
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
                //render.taskPool.add(this);
                return true;
            } else {
                //render.taskPool.add(this);
                return true;
            }
        }
        if(Vec3f.subtract(lastLoadPlayerPosition, player.position)
                .lengthSquared() >= maxWalkDistance) {
            //we have moved more than a chunklet, reload!
            lastLoadPlayerPosition = new Vec3f(player.position);
            walkTree(render.world.generateOctree);
            System.gc();
        }
        //render.taskPool.add(this);
        //TODO add this task to the taskpool whenever the player moves.
        return true;
    }

    private void walkTree(WorldOctree oc) {
        Vec3f mid = new Vec3f((float)oc.position.x+cslh,(float)oc.position.y+cslh,(float)oc.position.z+cslh);
        float distance2 = Vec3f.subtract(lastLoadPlayerPosition,mid).lengthSquared();
        if(distance2>viewDistanceM){
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
        } //else {
            //if(oc.block==null){
            //    oc.parent.subtrees[oc.getSPID()]=null;
            //    oc.checkForSubtrees();
            //    //TODO delete up
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
            boolean loaded = glc.loaded;
            boolean built = glc.built;
            synchronized(oc){
                glc.loaded=false;
                glc.built=false;
                oc.block=null;
            }
            if(loaded){
                render.chunksBuffered.remove(glc);
                render.garbageCollector.vbosToDelete.add(glc.vboID);
                glc.vboID=-1;
                render.garbageCollector.vbosToDelete.add(glc.iboID);
                glc.iboID=-1;
                render.garbageCollector.vaosToDelete.add(glc.vaoID);
                glc.vaoID=-1;
            }
            if(built) {
                glc.vertexB=null;
                glc.vertexBB=null;
                glc.vertexIB=null;
                glc.indexB=null;
            }
        }
    }

    private void loadNode(WorldOctree oc,Vec3f mid) {
        GLChunklet glc;
        Chunklet c = oc.block;
        if(c==null){
            render.world.worldProvider.provideSubtree(oc, mid);
            return;
        }
        if(c instanceof GLChunklet) {
            glc = (GLChunklet)c;
            if(glc.loaded) return;
            if(glc.built) {
                render.chunksToBuffer.add(glc);
                return;
            }
            // should not be needed to put me on the build stack,
            //who loads a chunklet without requesting build?
            //TODO check this is on build taskpool (or not)
            render.taskPool.add(glc);
            return;
        }
        glc = new GLChunklet(c);
        oc.block=glc;
        render.taskPool.add(glc);
        
    }
    
}
