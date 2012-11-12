/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common;

import net.q1cc.cfs.mmm.client.Client;
import net.q1cc.cfs.mmm.common.math.Quaternionf;
import net.q1cc.cfs.mmm.common.math.Vec3f;
import org.lwjgl.util.vector.*;

/**
 *
 * @author claus
 */
public class Player extends Entity{
    
    float walkingSpeed=20f;
    float runningSpeed=0.3f;
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
        Vec3f off = new Vec3f(0,0,0);
        if(forward)     off.z -= 1;
        if(backwards)   off.z += 1;
        if(left)        off.x -= 1;
        if(right)       off.x += 1;
        
        Matrix4f rot = new Matrix4f();
        rot.rotate((float)Math.toRadians(-rotation.x), new Vector3f(0,1,0));
        
        //Vector3f move = new Vector3f((float)Math.cos(-rotation.x)*offset.x+(float)Math.sin(-rotation.x)*offset.y,0,
        //        -(float)Math.sin(-rotation.x)*offset.x+(float)Math.cos(-rotation.x)*offset.y);        
        //rot.transpose();
        Vec3f move = new Vec3f(
                rot.m00*off.x+rot.m10*off.y+rot.m20*off.z,
                rot.m01*off.x+rot.m11*off.y+rot.m21*off.z,
                rot.m02*off.x+rot.m12*off.y+rot.m22*off.z);
        
        move.multToThis((run?runningSpeed:walkingSpeed)*Client.instance.renderer.deltaTime);
        
        return translate(move);
    }
    
    
    public enum Controller{
        MOUSE_AND_KEYBOARD, GAMEPAD1,GAMEPAD2,GAMEPAD3,GAMEPAD4,
        NETWORK
    }
}
