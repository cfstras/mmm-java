/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.lwjgl.BufferUtils;

/**
 *
 * @author claus
 */
public class MemUtil {
    
    final static LinkedList<ByteBuffer> buffers = new LinkedList<ByteBuffer>();
    
    static boolean outOfMemory = false;
    
    /**
     * returns a recycled ByteBuffer with at least the requested size.
     * if null is returned, we have no memory left.
     * @param minSize minimum size in bytes
     * @return a ByteBuffer or null if memory is low
     */
    public static ByteBuffer getBuffer(int minSize) {
        //TODO optimize performance with a binary tree or similiar
        ByteBuffer buffer=null;
        synchronized(buffers) {
            Iterator<ByteBuffer> it = buffers.iterator();
            while(it.hasNext()) {
                ByteBuffer b = it.next();
                //synchronized(b) {
                    int nextCap = b.capacity();
                    if(buffer == null && nextCap>=minSize) {
                        buffer = b;
                        it.remove();
                    } else if(buffer !=null && nextCap<buffer.capacity() && nextCap>=minSize) {
                        it.remove();
                        buffers.addFirst(buffer);
                        buffer = b;
                    }
                //}
                if(buffer !=null && buffer.capacity() == minSize) {
                    break;
                }
            }
        }
        //TODO check if found buffer is way too big
        if(buffer==null && !outOfMemory) {
            try {
                buffer = BufferUtils.createByteBuffer(minSize);
                buffer.order(ByteOrder.nativeOrder());
            } catch (OutOfMemoryError e) {
                //well, let's leave him with nothing.
                outOfMemory=true;
                //TODO when out of memory, schedule a freeing of cached buffers.
                System.out.println("oom error.");
                System.gc(); //TODO schedule this to a thread
            }
        }
        return buffer;
    }
    
    public static void returnBuffer(ByteBuffer buffer) {
        buffer.clear();
        synchronized(buffers) {
            buffers.add(buffer);
        }
    }
}
