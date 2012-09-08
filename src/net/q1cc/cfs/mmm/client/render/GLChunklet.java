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
    public IntBuffer indexB;
    
    boolean built=false;
    boolean update=false;
    
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
    
    //public GLChunklet(int x, int y, int z, WorldOctree parent){
    //    super(x,y,z, parent);
    //}
    
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
    public synchronized void buildChunklet(ByteBuffer targetV, ByteBuffer targetI) {
        int sizeGivenI=0, sizeGivenV=0;
        //build chunk
        
        //first pass: check for hidden faces and count blocks
        int vertexSize = 8;
        int indexSize = 1;
        int vertices = 0;
        int indices = 0;
        int vertexPos=0;
        Block b;
        blocksInside=0;
        for (int ix=0; ix<Chunklet.csl; ix++) {
            for(int iy=0; iy<Chunklet.csl; iy++) {
                for(int iz=0; iz<Chunklet.csl; iz++) {
                    b = blocks[ix + iy*Chunklet.csl + iz*Chunklet.csl2];
                    if(b!=null){
                        blocksInside++;
                        vertexPos = block(b,ix,iy,iz,vertexPos,false);
                    }
                }
            }
        }
        vertices = 4 * vertexPos/4;
        indices = 6 * vertexPos/4;
        if(blocksInside==0 || indices == 0) {
            built=true;
            //System.out.println("empty/invisible chunklet");
            return;
        }
        
        //second pass: generate visible faces
        vertexB=null;
        if(targetV!=null) {
            vertexB = targetV.asFloatBuffer();
            sizeGivenV = vertexB.capacity();
        }
        indexB=null;
        if(targetI!=null) {
            indexB = targetI.asIntBuffer();
            sizeGivenI = indexB.capacity();
        }
        if( (sizeGivenI < indices * indexSize)){
            indexB = BufferUtils.createIntBuffer(indices * indexSize);
        }
        if( (sizeGivenV < vertices * vertexSize)){
            vertexBB = BufferUtils.createByteBuffer(vertices * vertexSize * 4);
        }
        vertexIB = vertexBB.asIntBuffer();
        vertexB = vertexBB.asFloatBuffer();
        indexB.rewind();
        vertexIB.rewind();
        vertexB.rewind();
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
        if(vertices != vertexPos){
            System.out.println("vertices: "+vertices+ " vertexPos:"+vertexPos+ " indices: "+indices);
        }
        indCount = indices;
        vertexB.flip();
        indexB.flip();
        built = true;
        Client.instance.renderer.chunksToBuffer.add(this);
    }

    @Override
    public synchronized boolean doWork() {
        if(!built){
            buildChunklet(null, null);
            return true;
        } else {
            System.out.println("I was called without reason, master.");
            return false;
        }
    }

    @Override
    public int getPriority() {
        return WorkerTask.PRIORITY_NORM;
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
        
        vertexB.put((float)(u)/texture_block_cols+(float)(side)/texture_block_cols)
                .put((float)(v)/texture_block_rows+(float)(bl.blockID)/texture_block_rows);
        vertexIB.position(vertexB.position());
        //Color
        ReadableColor c = bl.color;
        if(side!=0)c = Color.WHITE;
        int col = 0;
        col |= c.getAlpha() << (3 * 8); //a
        col |= c.getBlue() << (2 * 8); //b
        col |= c.getGreen() << (1 * 8); //g
        col |= c.getRed(); //r
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
    
}
