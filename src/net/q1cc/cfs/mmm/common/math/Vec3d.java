
package net.q1cc.cfs.mmm.common.math;

/**
 * double 3-Dimensional Vector
 * @author claus
 */
public class Vec3d implements Cloneable {
    
    public double x;
    public double y;
    public double z;
    
    public static final Vec3d NULL =    new Vec3d(0, 0, 0);
    public static final Vec3d UP =      new Vec3d(0, 1, 0);
    public static final Vec3d DOWN =    new Vec3d(0,-1, 0);
    public static final Vec3d LEFT =    new Vec3d(-1,0, 0);
    public static final Vec3d RIGHT =   new Vec3d(1, 0, 0);
    public static final Vec3d FRONT =   new Vec3d(0, 0, 1);
    public static final Vec3d BACK =    new Vec3d(0, 0,-1);
    
    public Vec3d(double x,double y,double z){
        this.x=x;
        this.y=y;
        this.z=z;
    }
    
    public Vec3d(Vec3d cloneFrom){
        this.x=cloneFrom.x;
        this.y=cloneFrom.y;
        this.z=cloneFrom.z;
    }
    
    public Vec3d(Vec3f cloneFrom){
        this.x=cloneFrom.x;
        this.y=cloneFrom.y;
        this.z=cloneFrom.z;
    }
    
    public static Vec3d add(Vec3d a, Vec3d b){
        return new Vec3d(a.x+b.x,a.y+b.y,a.z+b.z);
    }
    
    /**
     * Adds another Vector to this one and stores the result in this Vector.
     * @return this object.
     */
    public Vec3d addToThis(Vec3d other){
        this.x+=other.x;
        this.y+=other.y;
        this.z+=other.z;
        return this;
    }
    
    /**
     * Adds another Vector to this one and stores the result in a new Vector.
     * @return a new Vector
     */
    public Vec3d add(Vec3d other){
        return add(this,other);
    }
    
    public static double scalProd(Vec3d a, Vec3d b){
        return a.x*b.x+a.y*b.y+a.z*b.z;
    }
    
    public double scalProd(Vec3d other){
        return scalProd(this,other);
    }
    
    public static Vec3d mult(Vec3d a,Vec3d b){
        return new Vec3d(
                a.y*b.z-a.z*b.y,
                a.z*b.x-a.x*b.z,
                a.x*b.y-a.y*b.x);
    }
    
    /**
     * Multiplies another Vector to this one and stores the result in this Vector.
     * @return this object.
     */
    public Vec3d multToThis(Vec3d other) {
        double oldX=x;
        double oldY=y;
        x= y*other.z - z*other.y;
        y= z*other.x - oldX*other.z;
        z= oldX*other.y - oldY*other.x;
        return this;
    }
    
    /**
     * Multiplies another Vector to this one and stores the result in a new Vector.
     * @return a new Vector.
     */
    public Vec3d mult(Vec3d other){
        return mult(this,other);
    }
    
    /**
     * Multiplies all values of this Vector with a value and stores the result in a new Vector.
     * @return a new Vector.
     */
    public Vec3d mult(double factor){
        return new Vec3d(x*factor,y*factor,z*factor);
    }
    
    @Override
    public String toString(){
        return "V3d["+x+","+y+","+z+"]";
    }

}
