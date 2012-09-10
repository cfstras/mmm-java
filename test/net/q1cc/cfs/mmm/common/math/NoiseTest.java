/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common.math;

/**
 *
 * @author cfstras
 */
public class NoiseTest {
    public static void main(String[] args) {
        int num = 1000*1000*100;
        test1(num);
        test2(num);
        test1(num);
        test1(num);
        test2(num);
        test2(num);
        System.out.println("finis");
    }
    
    public static void test1(int num){
        long start = System.nanoTime();
        testSimplex1(num);
        long end = System.nanoTime();
        System.out.println("noise1: "+(end-start)/num +"ns mean, "+((end-start)/1000000l)+"ms total");
    }
    public static void test2(int num){
        long start = System.nanoTime();
        testSimplex2(num);
        long end = System.nanoTime();
        System.out.println("noise2: "+(end-start)/num +"ns mean, "+((end-start)/1000000l)+"ms total");
    }
    public static void testSimplex1(int num){
        //for(int i=0;i<num;i++) {
        //    SimplexNoise.noise(i+i, i*i);
        //}
        for(int i=0;i<num;i++) {
            SimplexNoise.noise(i+i, i*i, i*i+i);
        }
        //for(int i=0;i<num;i++) {
        //    SimplexNoise.noise(i+i, (double)i*i, i*i+i,i);
        //}
    }
    public static void testSimplex2(int num){
        SimplexNoise2 sn = new SimplexNoise2();
        //for(int i=0;i<num;i++) {
        //    sn.noise(i+i, i*i);
        //}
        for(int i=0;i<num;i++) {
            sn.noise(i+i, i*i, i*i+i);
        }
        //for(int i=0;i<num;i++) {
        //    sn.noise(i+i, (double)i*i, i*i+i,i);
        //}
    }
}
