/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.client.render;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
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
    
    
    public static final int FLOAT_BYTES = 4;
    public static final int SHORT_BYTES = 2;
    public static final int INT_BYTES = 4;
    
    public static final int VERTEX_SIZE_FLOATS = 8;
    public static final int VERTEX_SIZE_BYTES = VERTEX_SIZE_FLOATS * FLOAT_BYTES;
    
    /** Vertex Layout:
     * float posX, posY, posZ;
     * float texU, texV;
     * byte colR, colG, colB, colA;
     * float padding,padding2;
     * => 8 * 4 bytes
     */
    
    /** Proposed Vertex Layout:
     * byte posX, posY, posZ; //4 bit block position, 4 bit for detailed meshes
     * byte colR, colG, colB;
     * short texID; //determines texture z
     * byte texU, texV;
     * byte padding,padding;
     * byte padding,padding,padding,padding;
     * => 8 * 2 bytes
     */
    
    /**
     * The VBO ID for this chunklet, if it is in VRAM.
     * if not, this is -1.
     */
    public int vboID=-1;
    //public int iboID=-1;
    
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
    //public int indCount;
    
    public int vertCount;
    
    public FloatBuffer vertexB;
    public IntBuffer vertexIB;
    public ByteBuffer vertexBB;
    public ShortBuffer vertexSB;
    //public ByteBuffer indexBB;
    //public IntBuffer indexB;
    
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
        //int vertexSize = 8;
        //int indexSize = 1;
        int vertices = 0;
        //int indices = 0;
        int vertexPos=0; 
        Block b;
        blocksInside=Chunklet.csl2*Chunklet.csl;// assume a full chunk
        vertices = 4 * blocksInside * 6;
        //indices = 6 * blocksInside * 4;
        
        //indexBB = MemUtil.getBuffer(indices * indexSize * 4);
        vertexBB = MemUtil.getBuffer(vertices * VERTEX_SIZE_BYTES);
        
        if(/*indexBB==null ||*/ vertexBB==null) { // out of memory, delay generation
            priority = WorkerTask.PRIORITY_IDLE;
            doCleanupCache();
            return false;
        }
        
        //indexBB.rewind(); // should not be needed
        vertexBB.clear();
        vertexIB = vertexBB.asIntBuffer();
        vertexB = vertexBB.asFloatBuffer();
        vertexSB = vertexBB.asShortBuffer();
        //indexB = indexBB.asIntBuffer();
        
        //start filling
        vertexPos=0;
        for (byte ix=0; ix<Chunklet.csl; ix++) {
            for(byte iy=0; iy<Chunklet.csl; iy++) {
                for(byte iz=0; iz<Chunklet.csl; iz++) {
                    b = blocks[ix + iy*Chunklet.csl + iz*Chunklet.csl2];
                    if(b!=null){
                        vertexPos = block(b,ix,iy,iz,vertexPos,true);
                    }
                }// three
            }//     outer
        }//         forloops

        //done.
        //indCount = indexB.position();
        //if(vertexPos!=0 || indCount!=0) {
        //    System.out.println(toString()+": verts: "+vertexPos+ " inds: "+indCount+" t:"+Thread.currentThread().getName());
        //}
        vertCount = vertexBB.position()/VERTEX_SIZE_BYTES;
        
        memorySize = VERTEX_SIZE_BYTES * vertCount;// + indCount * INT_BYTES;
        
        if(vertCount==0 /*|| indCount == 0*/) {
            empty=true;
            doCleanupCache();
        } else {
            //TODO use a smaller buffer if this one is way too big
            empty=false;
            vertexB.flip();
            //indexB.flip();
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
                //Client.instance.renderer.chunksToBuffer.remove(this);
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
                    //Client.instance.renderer.chunksToBuffer.remove(this);
                }
            }
        }
    }

    @Override
    public int getPriority() {
        return priority;
    }

    private int side(Block bl, byte ix, byte iy, byte iz, int side, int vertexPos) {
        if(side==0) {//top
            vert(0,ix,0,iy,1,iz,1,side,bl);
            vert(1,ix,1,iy,1,iz,1,side,bl);
            vert(2,ix,0,iy,1,iz,0,side,bl);
            vert(1,ix,1,iy,1,iz,1,side,bl);
            vert(3,ix,1,iy,1,iz,0,side,bl);
            vert(2,ix,0,iy,1,iz,0,side,bl);
        } else if(side==1) {//bot
            vert(0,ix,0,iy,0,iz,0,side,bl);
            vert(1,ix,1,iy,0,iz,0,side,bl);
            vert(2,ix,0,iy,0,iz,1,side,bl);
            vert(1,ix,1,iy,0,iz,0,side,bl);
            vert(3,ix,1,iy,0,iz,1,side,bl);
            vert(2,ix,0,iy,0,iz,1,side,bl);
        } else if(side==2) {//left
            vert(0,ix,0,iy,0,iz,0,side,bl);
            vert(1,ix,0,iy,0,iz,1,side,bl);
            vert(2,ix,0,iy,1,iz,0,side,bl);
            vert(1,ix,0,iy,0,iz,1,side,bl);
            vert(3,ix,0,iy,1,iz,1,side,bl);
            vert(2,ix,0,iy,1,iz,0,side,bl);
        } else if(side==3) {//right
            vert(0,ix,1,iy,0,iz,1,side,bl);
            vert(1,ix,1,iy,0,iz,0,side,bl);
            vert(2,ix,1,iy,1,iz,1,side,bl);
            vert(1,ix,1,iy,0,iz,0,side,bl);
            vert(3,ix,1,iy,1,iz,0,side,bl);
            vert(2,ix,1,iy,1,iz,1,side,bl);
        } else if(side==4) {//front
            vert(0,ix,0,iy,0,iz,1,side,bl);
            vert(1,ix,1,iy,0,iz,1,side,bl);
            vert(2,ix,0,iy,1,iz,1,side,bl);
            vert(1,ix,1,iy,0,iz,1,side,bl);
            vert(3,ix,1,iy,1,iz,1,side,bl);
            vert(2,ix,0,iy,1,iz,1,side,bl);
        } else if(side==5) {//back
            vert(0,ix,1,iy,0,iz,0,side,bl);
            vert(1,ix,0,iy,0,iz,0,side,bl);
            vert(2,ix,1,iy,1,iz,0,side,bl);
            vert(1,ix,0,iy,0,iz,0,side,bl);
            vert(3,ix,0,iy,1,iz,0,side,bl);
            vert(2,ix,1,iy,1,iz,0,side,bl);
        }
        vertexPos += 6;
        return vertexPos;
    }
    
    private void vert(int id,byte x, int ox, byte y,int oy, byte z, int oz, int side, Block bl){
        /* *
         * id is the id of the vertex.
         *  __  id |u |v
         * 2  3  0 |0  1
         * |\ |  1 |1  1
         * | \|  2 |0  0
         * 0__1  3 |1  0
         */
        
        vertexBB.put((byte)(x+ox)).put((byte)(y+oy)).put((byte)(x+oz));
        
        ReadableColor c = BlockInfo.blocks[bl.blockID].getColor();
        if(side!=0)c = Color.WHITE;
        vertexBB.put(c.getRedByte());
        vertexBB.put(c.getGreenByte());
        vertexBB.put(c.getBlueByte());
        
        short texID = (short)BlockInfo.blocks[bl.blockID].getTexID(side);
        vertexSB.position(vertexBB.position()/2);
        vertexSB.put(texID);
        vertexBB.position(vertexSB.position()*2);
        byte u,v;
        switch(id) {
            case 0:
                u=0; v=1; break;
            case 1:
                u=1; v=1; break;
            case 2:
                u=2; v=0; break;
            case 3:
                u=3; v=1; break;
            default:
                System.out.println("something went terribly wrong");
                return;
        }
        
        int row = texID/texture_block_cols;
        int column = texID%texture_block_cols;
        vertexBB.put((byte)(u/texture_block_cols+column/texture_block_cols))
                .put((byte)(v/texture_block_rows+row/texture_block_rows));
        
        vertexBB.put((byte)0xca).put((byte)0xfe); //padding for fast memory fetches
        vertexBB.put((byte)0xba).put((byte)0xbe).put((byte)0x10).put((byte)0x01);
    }

    private int block(Block b, byte ix, byte iy, byte iz, int vertexPos, boolean draw) {
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
                    //indexB.put(vertexPos + 0).put(vertexPos + 1).put(vertexPos + 2);
                    //indexB.put(vertexPos + 0).put(vertexPos + 2).put(vertexPos + 3);
                    vertexPos = side(b, ix, iy, iz, i, vertexPos);
                } else {
                    vertexPos+=6; //reserve space
                }
            }
        }
        if (!uptodate) {
            b.adjecentOpaquesCalculated = true;
        }
        return vertexPos;
    }
    
    private boolean doCleanupCache() {
        //ByteBuffer ib = indexBB;
        ByteBuffer vb = vertexBB;
        //indexBB = null;
        //indexB=null;
        vertexB = null;
        vertexIB = null;
        vertexBB = null;
        vertexSB = null;
        //if(ib!=null) {
        //    MemUtil.returnBuffer(ib);
        //}
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
                +"["+(awaitingBuffering?"ab":" ")+"]"
                +","+vertCount+"v,"+(memorySize)+"kb"
                +";"; //TODO update flags
    }

    private void doCleanupVRAM() {
        Client.instance.renderer.chunksBuffered.remove(this);
        int vao = vaoID;
        int vbo = vboID;
        //int ibo = iboID;
        vaoID /*= iboID*/ = vboID = -1;
        Client.instance.renderer.garbageCollector.vaosToDelete.add(vao);
        Client.instance.renderer.garbageCollector.vbosToDelete.add(vbo);
        //Client.instance.renderer.garbageCollector.vbosToDelete.add(ibo);
    }
}
