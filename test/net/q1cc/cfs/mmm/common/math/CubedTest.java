/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common.math;


/**
 *
 * @author claus
 */
public class CubedTest {
    public static void main(String[] args) {
        Cubed cube = new Cubed(Vec3d.NULL,20);
        System.out.println(cube.intersects(new Vec3d(0.0f,0.5f,1.0f)));
        
        //System.out.println(Cubed.);
        
    }
    
}
