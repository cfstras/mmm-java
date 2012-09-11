/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.client.render;

import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

/**
 *
 * @author cfstras
 */
public class GLGarbageCollector {
    
    public ConcurrentLinkedDeque<Integer> vbosToDelete = 
            new ConcurrentLinkedDeque<Integer>();
    public ConcurrentLinkedDeque<Integer> vaosToDelete = 
            new ConcurrentLinkedDeque<Integer>();
    public ConcurrentLinkedDeque<Integer> texToDelete = 
            new ConcurrentLinkedDeque<Integer>();
    
    private IntBuffer intB = BufferUtils.createIntBuffer(128);
    
    /**
     * deletes everything to be deleted.
     * has to be called in GL thread.
     */
    protected void collect() {
        if(fill(vbosToDelete)) {
            GL15.glDeleteBuffers(intB);
        }
        if(fill(vaosToDelete)) {
            GL30.glDeleteVertexArrays(intB);
        }
        if(fill(texToDelete)) {
            GL11.glDeleteTextures(intB);
        }
    }
    
    private boolean fill(ConcurrentLinkedDeque<Integer> list) {
        intB.rewind();
        intB.limit(128);
        boolean ret =false;
        if(!list.isEmpty()){
            ret=true;
            for(int i=0;i<128;i++){
                Integer d = list.pollFirst();
                if(d==null){
                    break;
                } else {
                    intB.put(d);
                }
            }
        }
        intB.flip();
        return ret;
    }
}
