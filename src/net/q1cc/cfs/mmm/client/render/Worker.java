/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.client.render;

/**
 * A worker works.
 * @author cfstras
 */
public class Worker extends Thread {
    
    WorkerTask currentTask;
    WorkerTaskPool pool;
    
    boolean live=true;
    boolean waiting=true;
    boolean wasIdle=false;
    
    public Worker(WorkerTaskPool pool){
        this.pool = pool;
    }
    
    @Override public void run() {
        while(live){
            currentTask = pool.pop();
            if(currentTask!=null){
                if(wasIdle){
                    System.out.println("strange, i woke up from a timeout and a task was waiting.");
                }
                waiting=false;
                currentTask.doWork();
                currentTask=null;
                waiting=true;
            } else {
                pool.workerIdle=true;
                wasIdle=true;
                synchronized(this){
                    try {
                        wait(5000);
                    } catch (InterruptedException ex) {wasIdle=false;}
                }
            }
            
        }
    }
}
