/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.client.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Holds all the Worker Tasks to be done.
 * Is not fair.
 * @author cfstras
 */
public class WorkerTaskPool {
    
    Worker[] workers;
    boolean workersIdle=true;
    boolean workerIdle=true;
    private boolean shutdown=false;
    
    public static int maxThreads = 1;
        
    private final PriorityBlockingQueue<WorkerTask> tasks;
    
    public WorkerTaskPool() {
        tasks = new PriorityBlockingQueue<WorkerTask>(500, new Comparator<WorkerTask>() {
            @Override
            public int compare(WorkerTask o1, WorkerTask o2) {
                return o2.getPriority()-o1.getPriority();
            }
        });
    }
    
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
        return true;
    }
    
    public WorkerTask pop(){
        if(tasks.isEmpty()) 
            return null;
        return tasks.remove();
    }
}
