/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.client.render;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Holds all the Worker Tasks to be done.
 * Is not fair.
 * @author cfstras
 */
public class WorkerTaskPool {
    
    //TODO replace with priority queue
    ArrayList<WorkerTask> tasks = new ArrayList<WorkerTask>();
    
    Worker[] workers;
    boolean workersIdle=true;
    boolean workerIdle=true;
    private boolean shutdown=false;
    
    public static int maxThreads = 1;
    
    private final Object lock = new Object();
    
    public void initWorkers() {
        int num = Runtime.getRuntime().availableProcessors();
        num = Math.min(num, maxThreads);
        workers = new Worker[num];
        for(int i=0;i<num;i++){
            workers[i]=new Worker(this);
            workers[i].setPriority((Thread.MIN_PRIORITY+Thread.MAX_PRIORITY)/2);
            workers[i].setDaemon(true);
            workers[i].setName("Worker "+i);
            workers[i].start();
        }
    }
    
    /**
     * Waits for all jobs to be finished.
     * could take forever.
     */
    public boolean finishWorkers() {
        shutdown=true;
        while(!tasks.isEmpty()){
            synchronized(this){
                try {
                    wait(1000);
                } catch (InterruptedException ex) {}
            }
        }
        for(Worker w:workers){
            w.live=false;
            synchronized(w){
                w.notifyAll();
            }
        }
        for(Worker w:workers){
            try {
                w.join();
            } catch (InterruptedException ex) {}
        }
        return false;
    }
    
    public boolean add(WorkerTask task){
        if(shutdown){
            return false;
        }
        synchronized(lock){
            tasks.add(task);
            /// wake them up
            workersIdle = false;
            if(workerIdle){
                workerIdle=false;
                for (int i = 0; i < workers.length; i++) {
                    synchronized (workers[i]) {
                        workers[i].notifyAll();
                        workers[i].wasIdle=false;
                    }
                }
            }
        }
        return true;
    }
    
    public WorkerTask pop(){
        WorkerTask ret;
        synchronized(lock){
            if(tasks.size()<1) {
                workersIdle=true;
                workerIdle=true;
                if(shutdown){
                    synchronized(this){
                        this.notifyAll();
                    }
                }
                return null;
            }
            ret=tasks.get(0);
        
            for(WorkerTask t:tasks){
                if(t.getPriority()>ret.getPriority()){
                    ret=t;
                }
                if(ret.getPriority()>=WorkerTask.PRIORITY_MAX){
                    break;
                }
            }
            tasks.remove(ret);
        }
        return ret;
    }
}
