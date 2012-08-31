/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common.math;

/**
 * Double-precision Cube.
 * @author claus
 */
public class Cubed {
    
    /**
     * Position of Cube's middle
     */
    public Vec3d position;
    
    public double sidelength;
    
    /**
     * if this cube has something to do with octrees,
     * it's level, so we can determine an exact sidelength.
     * otherwise Int.MIN_VALUE
     */
    public int level=Integer.MIN_VALUE;
    
    /**
     * ignored for now. aaaaaaaaah.
     */
    public Quaternionf rotation;
    
    public Cubed(Vec3d position, double sidelength){
        this.position=position;
        this.sidelength=sidelength;
        
        double l=Math.log(sidelength);
        if((int)l==l){
            level=(int)l;
        }
    }
    
    /**
     * Note that this constructor interprets the position to be the lower left of this Cube!
     * it converts it to middle position.
     * @param position
     * @param level 
     */
    public Cubed(Vec3d position, int level){
        
        this.level=level;
        
        this.sidelength=Math.pow(2, level);
        double slh=sidelength/2;
        Vec3d pos=new Vec3d(position.x+slh,position.y+slh,position.z+slh);
        this.position = pos;
        
    }
    
    public boolean intersects(Cubed other){
        //TODO implement cube intersects cube
        
        
        return false;
    }
    
    /**
     * checks if a point lies within a cube
     * 
     * ignores rotation
     * @param point
     * @return 
     */
    public boolean intersects(Vec3d point)
    {
        double slh=sidelength/2;
        if(point.x<position.x-slh) return false;
        if(point.x>position.x+slh) return false;
        
        if(point.y>position.y+slh) return false;
        if(point.y>position.y+slh) return false;
        
        if(point.z>position.z+slh) return false;
        if(point.z>position.z+slh) return false;
        
        return true;
    }
    /**
     * static function for intersection.
     * interprets x,y,z as the corner values
     * @param point the point to check for intersection
     * @param x x of the cube corner
     * @param y y of the cube corner
     * @param z z of the cube corner
     * @param sidelength sidelength of the cube
     * @return 
     */
    public static boolean intersectsCorner(Vec3d point,double x,double y,double z,double sidelength){
        double slh= sidelength/2d;
        x+=slh; y+=slh; z+=slh;
        if(point.x<x-slh) return false;
        if(point.x>=x+slh) return false;
        
        if(point.y<y-slh) return false;
        if(point.y>=y+slh) return false;
        
        if(point.z<z-slh) return false;
        if(point.z>=z+slh) return false;
        
        //System.out.println(point+" intersects with cube mid x="+x+",y="+y+",z="+z+",radius="+slh);
        return true;
    }
    /**
     * static function for intersection.
     * interprets x,y,z as the middle values
     * @param point the point to check for intersection
     * @param x x of the cube middle
     * @param y y of the cube middle
     * @param z z of the cube middle
     * @param sidelength sidelength of the cube
     * @return 
     */
    public static boolean intersects(Vec3d point,double x,double y,double z,double sidelength){
        double slh= sidelength/2;
        if(point.x<x-slh) return false;
        if(point.x>x+slh) return false;
        
        if(point.y>y+slh) return false;
        if(point.y>y+slh) return false;
        
        if(point.z>z+slh) return false;
        if(point.z>z+slh) return false;
        
        return true;
    }

}
