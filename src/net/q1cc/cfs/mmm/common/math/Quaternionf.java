/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common.math;

import static java.lang.Math.*;

/**
 * A Quaternion with float precision.
 * @author claus doesn't want to implement this
 */
public class Quaternionf {
    
    public float x;
    public float y;
    public float z;
    public float w;
    
    public Quaternionf(float x,float y,float z,float w){
        this.x=x;
        this.y=y;
        this.z=z;
        this.w=w;
    }
    
    /**
     * rotate a vector around a given axis by a given angle.
     * only ONE of the x,y,z can be true! otherwise, garbage is produced.
     * @param vector
     * @param angle
     * @param aroundX
     * @param aroundY
     * @param aroundZ
     * @return 
     */
    public static Vec3f rotate(Vec3f vector, float angle, boolean aroundX,boolean aroundY, boolean aroundZ){
        if(angle==0.0f)
            return new Vec3f(vector);
        
        float x=0,y=0,z=0;
        double rad=Math.toRadians(angle);
        
        if(aroundX){
            x=vector.x;
            y=(float) (cos(rad)*vector.y - sin(rad)*vector.z);
            z=(float) (sin(rad)*vector.y + cos(rad)*vector.z);
        } else if(aroundY) {
            x=(float) (cos(rad)*vector.x + sin(rad)*vector.z);
            y=vector.y;
            z=(float) (sin(rad)*vector.x + cos(rad)*vector.z);
        } else if(aroundZ) {
            x=(float) (cos(rad)*vector.x - sin(rad)*vector.y);
            y=(float) (sin(rad)*vector.x + cos(rad)*vector.y);
            z=vector.z;
        }
        
        return new Vec3f(x,y,z);
    }
    
    
    @Override
    public String toString() {
        return "Qx="+x+",y="+y+",z="+z;
    }
    
}
