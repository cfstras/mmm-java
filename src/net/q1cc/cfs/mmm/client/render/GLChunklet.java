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

/**
 * The GL Extension of a chunklet, able to store VBO IDs and such.
 * Stores a pointer to the underlying chunklet.
 * Not to be serialized.
 * @author cfstras
 */
public class GLChunklet extends Chunklet implements WorkerTask {
    
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
     * Vertices are stored as such:
     * 
     * struct vertex {
     *      in vec3  inPos;
     *      in float inLight;
     *      in int   inOrient;
     *      in int   inColor;
     *      in int   inBlock;
     *      in float padding;
     * }
     * 
     * @param targetV the target vertex buffer, if one is avaliable.
     * @param targetI same for index buffer
     */
    public synchronized void buildChunklet(ByteBuffer targetV, ByteBuffer targetI) {
        int sizeGivenI=0, sizeGivenV=0;
        //build chunk
        //TODO compress surfaces
        // needed:    float perVertex
        int vertexSize = 8;
        int indexSize = 1;
        int vertices = 0;
        int indices = 0;
        
        for(Block b:blocks){
            if(b!=null){
                //TODO check for special block
                vertices += 6*4;
                indices += 6*2*3;
            }
        }
        blocksInside = vertices / (6*4);
        if(blocksInside==0) {
            built=true;
            //System.out.println("height "+parent.height+" discarded. x="+posX+" y="+posY+" z="+posZ);
            return;
        } else {
            //System.out.println("height "+parent.height+" has "+blocksInside+" blocks. x="+posX+" y="+posY+" z="+posZ);
        }
        
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
        int vertexPos=0;
        Block b;
        for (int ix=0; ix<Chunklet.csl; ix++) {
            for(int iy=0; iy<Chunklet.csl; iy++) {
                for(int iz=0; iz<Chunklet.csl; iz++) {
                    b = blocks[ix + iy*Chunklet.csl + iz*Chunklet.csl2];
                    if(b!=null){
                        // the indices
                        
                        //front
                        vertexPos = side(b,ix,iy,iz,true,false,false,true,vertexPos);
                        indexB.put(vertexPos-3).put(vertexPos-4).put(vertexPos-2);
                        indexB.put(vertexPos-1).put(vertexPos-3).put(vertexPos-2);
                        //right
                        vertexPos = side(b,ix+1,iy,iz,false,true,false,false,vertexPos);
                        indexB.put(vertexPos-4).put(vertexPos-3).put(vertexPos-2);
                        indexB.put(vertexPos-3).put(vertexPos-1).put(vertexPos-2);
                        //left
                        vertexPos = side(b,ix,iy,iz,true,true,false,false,vertexPos);
                        indexB.put(vertexPos-3).put(vertexPos-4).put(vertexPos-2);
                        indexB.put(vertexPos-1).put(vertexPos-3).put(vertexPos-2);
                        //top
                        vertexPos = side(b,ix,iy+1,iz,false,false,true,false,vertexPos);
                        indexB.put(vertexPos-3).put(vertexPos-4).put(vertexPos-2);
                        indexB.put(vertexPos-1).put(vertexPos-3).put(vertexPos-2);
                        //back
                        vertexPos = side(b,ix,iy,iz-1,false,false,false,true,vertexPos);
                        indexB.put(vertexPos-4).put(vertexPos-3).put(vertexPos-2);
                        indexB.put(vertexPos-3).put(vertexPos-1).put(vertexPos-2);
                        //bottom
                        vertexPos = side(b,ix,iy,iz,true,false,true,false,vertexPos);
                        indexB.put(vertexPos-4).put(vertexPos-3).put(vertexPos-2);
                        indexB.put(vertexPos-3).put(vertexPos-1).put(vertexPos-2);
                        
                    } //if b!=null
                    
                }// three
            }//     outer
        }//         forloops
        //System.out.println("Chunklet " + blocksInside + " blocks, "
        //    + vertexPos + "=" + vertexB.position() / vertexSize + "=" + vertices + " verts, "
        //    + indices + "=" + indexB.position() + " inds");
        //done.
        indCount = indices;
        vertexB.flip();
        indexB.flip();
        //vertexB = vertexB;
        //indexB = indexB;
        built = true;
        Client.instance.renderer.chunksToBuffer.add(this);
        //System.out.print(".");

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

    private int side(Block bl, int ix, int iy, int iz,
            boolean bm, boolean bx, boolean by, boolean bz, int vertexPos) {
        int dx = 0, dy = 0, dz = 0;
        if (!bx) {
            dx = 1;
        }
        if (!by) {
            dy = 1;
        }
        if (!bz) {
            dz = -1;
        }
        if (bm) {
            //dx *= -1;
            //dy *= -1;
            //dz *= -1;
        }
        for(int a=0;a<2;a++){
            for(int b=0;b<2;b++){
                if(dz==0){ 
                    vertexB.put(ix + a*dx);
                    vertexB.put(iy + b*dy);
                    vertexB.put(iz);
                } else if(dy==0){
                    vertexB.put(ix + a*dx);
                    vertexB.put(iy);
                    vertexB.put(iz + b*dz);
                } else if(dx==0) {
                    vertexB.put(ix);
                    vertexB.put(iy + a*dy);
                    vertexB.put(iz + b*dz);
                } else {
                    System.out.println("wtf");
                }
                
                vertexB.put(bl.lightLevel);
                //orientation
                int orientation = 0;
                orientation += (bx ? 1 : 0) * 1;
                orientation += (by ? 1 : 0) * 2;
                orientation += (bz ? 1 : 0) * 4;
                orientation += (bm ? 1 : 0) * 8;
                //texture coordinates
                orientation += (a==1? 1:0) * 16;
                orientation += (b==1? 1:0) * 32;
                vertexIB.position(vertexB.position());
                vertexIB.put(orientation);
                //Color
                int col=0;
                col |= bl.color.getRed()   << 3*8;
                col |= bl.color.getGreen() << 2*8;
                col |= bl.color.getBlue()  << 1*8;
                col |= bl.color.getAlpha();
                vertexIB.put(col);
                vertexIB.put(bl.blockID);
                vertexB.position(vertexIB.position());
                vertexB.put(1337);
                vertexPos++;
            }
        }
        
        
        return vertexPos;
    }
    
    
}
