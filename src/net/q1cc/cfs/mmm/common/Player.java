/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common;

import net.q1cc.cfs.mmm.client.Client;
import net.q1cc.cfs.mmm.common.math.Quaternionf;
import net.q1cc.cfs.mmm.common.math.Vec3f;

/**
 *
 * @author claus
 */
public class Player extends Entity{
    
    float walkingSpeed=0.5f;
    float runningSpeed=0.8f;
    Controller controller;
    
    public Player() {
        super();
        hasGravity=true;
        // 0.5m shoulders, lol.
        colliderSize=new Vec3f(0.5f,1.75f,0.3f);
        type=EntityType.PLAYERCONTROLLED;
        controller=Controller.MOUSE_AND_KEYBOARD;
    }
    
    public boolean move(boolean forward,boolean left, boolean right, boolean backwards,boolean run){
        //TODO this rotation sucks.
        if(left&&right){
            left=false;right=false;
        }
        if(forward&&backwards){
            forward=false;
            backwards=false;
        }
        if(!forward&&!backwards&&!left&&!right){
            return true;
        }
        float x=0,y=0,z=0;
        if(forward) {
            z=-1;
        } else if(backwards){
            z=1;
        }
        if(left){
            if(forward){
                x=z=-0.707107f; //sqrt(0.5)
            } else if (backwards) {
                x-= z=0.707107f;
            } else {
                x=-1f;
            }
        } else if(right) {
            if(forward){
                x-=z=-0.707107f; //sqrt(0.5)
            } else if (backwards) {
                x= z=0.707107f;
            } else {
                x=1f;
            }
        }
        
        Vec3f offset=new Vec3f(x,y,z);
        offset=Quaternionf.rotate(offset, -rotation.x, false, true, false);
        
        offset.mult((run?runningSpeed:walkingSpeed)*Client.instance.renderer.deltaTime);
        
        return translate(offset);
    }
    
    
    public enum Controller{
        MOUSE_AND_KEYBOARD, GAMEPAD1,GAMEPAD2,GAMEPAD3,GAMEPAD4,
        NETWORK
    }
}
