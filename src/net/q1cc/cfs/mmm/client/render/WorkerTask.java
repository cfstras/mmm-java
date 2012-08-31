/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.client.render;

/**
 *
 * @author cfstras
 */
public abstract interface WorkerTask {
    public static final int PRIORITY_IDLE = 0;
    public static final int PRIORITY_MIN = 1;
    public static final int PRIORITY_NORM = 2;
    public static final int PRIORITY_MAX = 3;
    
    public int getPriority();
    
    public boolean doWork();
}
