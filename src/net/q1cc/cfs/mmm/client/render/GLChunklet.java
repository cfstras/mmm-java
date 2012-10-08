/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.client.render;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.q1cc.cfs.mmm.client.Client;
import net.q1cc.cfs.mmm.common.MemUtil;
import net.q1cc.cfs.mmm.common.blocks.BlockInfo;
import net.q1cc.cfs.mmm.common.world.Block;
import net.q1cc.cfs.mmm.common.world.Chunklet;
import net.q1cc.cfs.mmm.common.world.WorldOctree;
import org.lwjgl.BufferUtils;
import org.lwjgl.MemoryUtil;
import org.lwjgl.Sys;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 * The GL Extension of a chunklet, able to store VBO IDs and such.
 * Stores a pointer to the underlying chunklet.
 * Not to be serialized.
 * @author cfstras
 */
public class GLChunklet extends Chunklet implements WorkerTask {
    
    int priority = WorkerTask.PRIORITY_NORM;
    
    public static int texture_block_size = 16;
    public static int texture_block_rows = 8;
    public static int texture_block_cols = 8;
    
    /**
     * The VBO ID for this chunklet, if it is in VRAM.
     * if not, this is -1.
     */
    public int vboID=-1;
    public int iboID=-1;
    
    /**
     * The VAO ID for render setup of this chunklet.
     * if none exists, is -1.
     */
    public int vaoID=-1;
    
    /**
     * Resident client memory size of this chunklet, in bytes.
     */
    public int memorySize = 0;
    
    /**
     * number of indices in index buffer
     */
    public int indCount;
    
    public FloatBuffer vertexB;
    public IntBuffer vertexIB;
    public ByteBuffer vertexBB;
    public ByteBuffer indexBB;
    public IntBuffer indexB;
    
    //state flags
    boolean building=false;
    boolean built=false;
    boolean empty=false;
    boolean buffered=false;
    
    //waiting operations
    boolean awaitingBuild = false;
    boolean awaitingCacheCleanup = false;
    boolean awaitingBuffering = false;
    boolean awaitingUpdate=false;
    boolean awaitingVRAMCleanup=false;
    
    
    /**
     * creates a GLChunklet from a given common chunklet.
     * Does not create video memory representations yet.
     * @param from 
     */
    public GLChunklet(Chunklet from) {
        super(from.posX,from.posY,from.posZ, from.parent);
        this.blocks=from.blocks;
        if(from.parent==null){
            System.out.println("muh!");
        }
    }
    
    public GLChunklet(int x, int y, int z, WorldOctree parent){
        super(x,y,z, parent);
    }
    
    /**
     * Builds a chunks representation into video memory.
     * takes an existing ByteBuffer as source, if null or too small, requests
     * a new one.
     * side ids are:
     * 0 top
     * 1 bottom
     * 2 left
     * 3 right
     * 4 front
     * 5 back
     * 
     * @param targetV the target vertex buffer, if one is avaliable.
     * @param targetI same for index buffer
     */
    private boolean buildChunklet() {
        
        //build chunk
        int vertexSize = 8;
        int indexSize = 1;
        int vertices = 0;
        int indices = 0;
        int vertexPos=0; 
        Block b;
        blocksInside=Chunklet.csl2*Chunklet.csl;// asume a full chunk
        vertices = 4 * blocksInside*6;
        indices = 6 * blocksInside * 4;
        
        indexBB = MemUtil.getBuffer(indices * indexSize * 4);
        vertexBB = MemUtil.getBuffer(vertices * vertexSize * 4);
        
        if(indexBB==null || vertexBB==null) { // out of memory, delay generation
            priority = WorkerTask.PRIORITY_IDLE;
            doCleanupCache();
            return false;
        }
        
        indexBB.rewind(); // should not be needed
        vertexBB.rewind();
        vertexIB = vertexBB.asIntBuffer();
        vertexB = vertexBB.asFloatBuffer();
        indexB = indexBB.asIntBuffer();
        
        //start filling
        vertexPos=0;
        for (int ix=0; ix<Chunklet.csl; ix++) {
            for(int iy=0; iy<Chunklet.csl; iy++) {
                for(int iz=0; iz<Chunklet.csl; iz++) {
                    b = blocks[ix + iy*Chunklet.csl + iz*Chunklet.csl2];
                    if(b!=null){
                        vertexPos = block(b,ix,iy,iz,vertexPos,true);
                    }
                }// three
            }//     outer
        }//         forloops

        //done.
        indCount = indexB.position();
        //if(vertexPos!=0 || indCount!=0) {
        //    System.out.println(toString()+": verts: "+vertexPos+ " inds: "+indCount+" t:"+Thread.currentThread().getName());
        //}
        
        if(vertexPos==0 || indCount == 0) {
            empty=true;
            doCleanupCache();
        } else {
            //TODO use a smaller buffer if this one is way too big
            empty=false;
            vertexB.flip();
            indexB.flip();
            built = true;
        }
        return true;
    }
    
    @Override
    public boolean doWork() {
        synchronized(this) {
            if(building) {
                System.out.println("chunklet already working");
                return false;
            }
            building=true;
        }
        //check flags
        if(awaitingVRAMCleanup) {
            buffered=false;
            awaitingBuffering=false;
            doCleanupVRAM();
            awaitingVRAMCleanup=false;
        }
        if(awaitingCacheCleanup) {
            built=false;
            awaitingBuffering=false;
            doCleanupCache();
            awaitingCacheCleanup=false;
        }
        if(awaitingBuild) {
            if( !built || (built&&awaitingUpdate)) {
                if(buildChunklet()) {
                    awaitingBuild=false;
                    awaitingUpdate=false;
                    awaitingBuffering=true;
                    if(!empty) {
                        Client.instance.renderer.chunksToBuffer.add(this);
                    }
                } else {
                    //didn't get memory, replace on build queue.
                    Client.instance.taskPool.add(this);
                }
            } else if(built&& !awaitingUpdate) {
                System.out.println(toString()+" double build invocation");
            }
        }
        
        //if(awaitingUpdate) {
        //TODO implement single block updates (how?)
        //}
        
        building=false;
        return true;
    }
    
    /**
     * schedules a cache cleanup.
     * also cancels scheduling for buffering.
     */
    void cleanupCache() {
        synchronized(this) {
            if(built) {
                if(!awaitingBuffering && !awaitingVRAMCleanup && !awaitingBuild
                        && !awaitingUpdate && !awaitingCacheCleanup) {
                    //we are most likely not in queue, put us in
                    Client.instance.taskPool.add(this);
                }
                awaitingCacheCleanup=true;
            }
            if(awaitingBuffering) {
                awaitingBuffering=false;
                Client.instance.renderer.chunksToBuffer.remove(this);
            }
        }
    }
    
    /**
     * schedules a VRAM garbage collect.
     */
    void cleanupVRAMCache() {
        synchronized(this) {
            if(buffered) {
                if(!awaitingBuffering && !awaitingVRAMCleanup && !awaitingBuild
                        && !awaitingUpdate && !awaitingCacheCleanup) {
                    //we are most likely not in queue, put us in
                    Client.instance.taskPool.add(this);
                }
                awaitingVRAMCleanup = true;
            }
        }
    }
    
    /**
     * schedules this GLChunklet to build.
     * also cancels scheduling for buffering.
     */
    void build() {
        synchronized(this) {
            if(!built) {
                if(!awaitingBuffering && !awaitingVRAMCleanup && !awaitingBuild
                        && !awaitingUpdate && !awaitingCacheCleanup) {
                    //we are most likely not in queue, put us in
                    Client.instance.taskPool.add(this);
                }
                awaitingBuild = true;
                if(awaitingBuffering) {
                    awaitingBuffering=false;
                    Client.instance.renderer.chunksToBuffer.remove(this);
                }
            }
        }
    }

    @Override
    public int getPriority() {
        return priority;
    }

    private int side(Block bl, int ix, int iy, int iz, int side, int vertexPos) {
        if(side==0) {//top
            vertexB.put(ix).put(iy+1).put(iz+1);
            vert(0,1,side,bl);
            vertexB.put(ix+1).put(iy+1).put(iz+1);
            vert(1,1,side,bl);
            vertexB.put(ix+1).put(iy+1).put(iz);
            vert(1,0,side,bl);
            vertexB.put(ix).put(iy+1).put(iz);
            vert(0,0,side,bl);
            vertexPos += 4;
        } else if(side==1) {//bot
            vertexB.put(ix).put(iy).put(iz);
            vert(0,1,side,bl);
            vertexB.put(ix+1).put(iy).put(iz);
            vert(1,1,side,bl);
            vertexB.put(ix+1).put(iy).put(iz+1);
            vert(1,0,side,bl);
            vertexB.put(ix).put(iy).put(iz+1);
            vert(0,0,side,bl);
            vertexPos += 4;
        } else if(side==2) {//left
            vertexB.put(ix).put(iy).put(iz);
            vert(0,1,side,bl);
            vertexB.put(ix).put(iy).put(iz+1);
            vert(1,1,side,bl);
            vertexB.put(ix).put(iy+1).put(iz+1);
            vert(1,0,side,bl);
            vertexB.put(ix).put(iy+1).put(iz);
            vert(0,0,side,bl);
            vertexPos += 4;
        } else if(side==3) {//right
            vertexB.put(ix+1).put(iy).put(iz+1);
            vert(0,1,side,bl);
            vertexB.put(ix+1).put(iy).put(iz);
            vert(1,1,side,bl);
            vertexB.put(ix+1).put(iy+1).put(iz);
            vert(1,0,side,bl);
            vertexB.put(ix+1).put(iy+1).put(iz+1);
            vert(0,0,side,bl);
            vertexPos += 4;
        } else if(side==4) {//front
            vertexB.put(ix).put(iy).put(iz+1);
            vert(0,1,side,bl);
            vertexB.put(ix+1).put(iy).put(iz+1);
            vert(1,1,side,bl);
            vertexB.put(ix+1).put(iy+1).put(iz+1);
            vert(1,0,side,bl);
            vertexB.put(ix).put(iy+1).put(iz+1);
            vert(0,0,side,bl);
            vertexPos += 4;
        } else if(side==5) {//back
            vertexB.put(ix+1).put(iy).put(iz);
            vert(0,1,side,bl);
            vertexB.put(ix).put(iy).put(iz);
            vert(1,1,side,bl);
            vertexB.put(ix).put(iy+1).put(iz);
            vert(1,0,side,bl);
            vertexB.put(ix+1).put(iy+1).put(iz);
            vert(0,0,side,bl);
            vertexPos += 4;
        }
        
        return vertexPos;
    }
    
    private void vert(int u, int v, int side, Block bl){
        int texID = BlockInfo.blocks[bl.blockID].getTexID(side);
        int row = texID/texture_block_cols;
        int column = texID%texture_block_cols;
        vertexB.put((float)(u)/texture_block_cols+(float)(column)/texture_block_cols)
                .put((float)(v)/texture_block_rows+(float)(row)/texture_block_rows);
        vertexIB.position(vertexB.position());
        //Color
        ReadableColor c = BlockInfo.blocks[bl.blockID].getColor();
        if(side!=0)c = Color.WHITE;
        int col = 0;
        //TODO use the byte buffer here, makes more sense.
        col |= c.getRed();              //r
        col |= c.getGreen() << (1 * 8); //g
        col |= c.getBlue() << (2 * 8);  //b
        col |= c.getAlpha() << (3 * 8); //a
        vertexIB.put(col);
        vertexB.position(vertexIB.position());
        vertexB.put(1337).put(1338);
    }

    private int block(Block b, int ix, int iy, int iz, int vertexPos, boolean draw) {
        /*
         * 0 top    y+1
         * 1 bottom y-1
         * 2 left   x-1
         * 3 right  x+1
         * 4 front  z-1
         * 5 back   z+1
         */
        boolean uptodate = b.adjecentOpaquesCalculated;
        if (!draw) {
            uptodate = false;
        }
        for(int i=0;i<6;i++) {
            if(!faceIsHidden(b,ix,iy,iz,i, uptodate)){
                if(draw){
                    indexB.put(vertexPos + 0).put(vertexPos + 1).put(vertexPos + 2);
                    indexB.put(vertexPos + 0).put(vertexPos + 2).put(vertexPos + 3);
                    vertexPos = side(b, ix, iy, iz, i, vertexPos);
                } else {
                    vertexPos+=4; //reserve space
                }
            }
        }
        if (!uptodate) {
            b.adjecentOpaquesCalculated = true;
        }
        return vertexPos;
    }
    
    private boolean doCleanupCache() {
        ByteBuffer ib = indexBB;
        ByteBuffer vb = vertexBB;
        indexBB = null;
        indexB=null;
        vertexB = null;
        vertexIB = null;
        vertexBB = null;
        if(ib!=null) {
            MemUtil.returnBuffer(ib);
        }
        if(vb!=null) {
            MemUtil.returnBuffer(vb);
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "GLC@x:"+posX+",y:"+posY+",z:"+posZ
                +"["+(built?"b":" ")+"]"
                +"["+(buffered?"l":" ")+"]"
                +"["+(building?"w":" ")+"]"
                +"["+(awaitingBuffering?"ab":" ")+"]"; //TODO update flags
    }

    private void doCleanupVRAM() {
        Client.instance.renderer.chunksBuffered.remove(this);
        int vao = vaoID;
        int vbo = vboID;
        int ibo = iboID;
        vaoID = iboID = vboID = -1;
        Client.instance.renderer.garbageCollector.vaosToDelete.add(vao);
        Client.instance.renderer.garbageCollector.vbosToDelete.add(vbo);
        Client.instance.renderer.garbageCollector.vbosToDelete.add(ibo);
    }
}
