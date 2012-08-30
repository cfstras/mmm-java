/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.client.render;

/**
 *
 * @author cfstras
 */
public abstract class WorkerTask {
    static final int PRIORITY_IDLE = 0;
    static final int PRIORITY_MIN = 1;
    static final int PRIORITY_NORM = 2;
    static final int PRIORITY_MAX = 3;
    
    boolean done=false;
    int priority = PRIORITY_NORM;
    
    public WorkerTask(int prio){
        this.priority = prio;
    }
    
    abstract boolean doWork();
}
