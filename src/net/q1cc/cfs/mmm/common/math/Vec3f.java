/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common.math;

/**
 *  Float 3-Dimensional Vector
 * @author claus
 */
public class Vec3f implements Cloneable {
    
    public float x;
    public float y;
    public float z;
    
    public static final Vec3f NULL =    new Vec3f(0, 0, 0);
    public static final Vec3f UP =      new Vec3f(0, 1, 0);
    public static final Vec3f DOWN =    new Vec3f(0,-1, 0);
    public static final Vec3f LEFT =    new Vec3f(-1,0, 0);
    public static final Vec3f RIGHT =   new Vec3f(1, 0, 0);
    public static final Vec3f FRONT =   new Vec3f(0, 0, 1);
    public static final Vec3f BACK =    new Vec3f(0, 0,-1);
    public static final Vec3f ONE =     new Vec3f(1, 1, 1);
    
    public Vec3f(float x,float y,float z){
        this.x=x;
        this.y=y;
        this.z=z;
    }
    public Vec3f(Vec3f cloneFrom){
        this.x=cloneFrom.x;
        this.y=cloneFrom.y;
        this.z=cloneFrom.z;
    }
    
    /**
     * clones a new float-Vector from a double one. Might reduce accuracy.
     * @param cloneFrom 
     */
    public Vec3f(Vec3d cloneFrom){
        this.x=(float)cloneFrom.x;
        this.y=(float)cloneFrom.y;
        this.z=(float)cloneFrom.z;
    }
    
    public static Vec3f add(Vec3f a, Vec3f b){
        return new Vec3f(a.x+b.x,a.y+b.y,a.z+b.z);
    }
    
    
    /**
     * Adds another Vector to this one and stores the result in this Vector.
     * @return this object.
     */
    public Vec3f addToThis(Vec3f other){
        this.x+=other.x;
        this.y+=other.y;
        this.z+=other.z;
        return this;
    }
    
    /**
     * Adds another vector to this one and stores the result in a new vector.
     * @return a new vector
     */
    public Vec3f add(Vec3f other){
        return add(this,other);
    }
    
    /**
     * subtracts another vector and stores the result in this vector.
     * @return this vector
     */
    public Vec3f subtractToThis(Vec3f other){
        x-=other.x;
        y-=other.y;
        z-=other.z;
        return this;
    }
    
    /**
     * subtracts vector b from vector a and stores the result in a new vector.
     * @param a
     * @param b
     * @return a new vector containing the result of a-b
     */
    public static Vec3f subtract(Vec3f a, Vec3f b){
        return new Vec3f(a.x-b.x,a.y-b.y,a.z-b.z);
    }
    
    /**
     * Calculates the square of the length of this vector.
     * @return a big green elefant
     */
    public float lengthSquared() {
        return x*x+y*y+z*z;
    }
    
    public static float scalProd(Vec3f a, Vec3f b){
        return a.x*b.x+a.y*b.y+a.z*b.z;
    }
    
    public float scalProd(Vec3f other){
        return scalProd(this,other);
    }
    
    public static Vec3f mult(Vec3f a,Vec3f b){
        return new Vec3f(
                a.y*b.z-a.z*b.y,
                a.z*b.x-a.x*b.z,
                a.x*b.y-a.y*b.x);
    }
    public Vec3f multToThis(float factor){
        this.x *=factor;
        this.y*= factor;
        this.z*=factor;
        return this;
    }
    
    /**
     * Multiplies another Vector to this one and stores the result in this Vector.
     * @return this object.
     */
    public Vec3f multToThis(Vec3f other) {
        float oldX=x;
        float oldY=y;
        x= y*other.z - z*other.y;
        y= z*other.x - oldX*other.z;
        z= oldX*other.y - oldY*other.x;
        return this;
    }
    
    /**
     * Multiplies another Vector to this one and stores the result in a new Vector.
     * @return a new Vector.
     */
    public Vec3f mult(Vec3f other){
        return mult(this,other);
    }
    
    /**
     * Multiplies all values of this Vector with a value and stores the result in a new Vector.
     * @return a new Vector.
     */
    public Vec3f mult(float factor){
        return new Vec3f(x*factor,y*factor,z*factor);
    }
    
    
    @Override
    public String toString(){
        return "V3f["+x+","+y+","+z+"]";
    }
}
