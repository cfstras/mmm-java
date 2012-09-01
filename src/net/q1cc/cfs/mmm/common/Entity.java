/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common;

import net.q1cc.cfs.mmm.common.math.Quaternionf;
import net.q1cc.cfs.mmm.common.math.Vec3d;
import net.q1cc.cfs.mmm.common.math.Vec3f;
import org.lwjgl.util.vector.Vector3f;

/**
 * Describes a Generic Entity.
 * Entities are things that can (or can be) moved, unlike blocks, or aren't blocks.
 * Examples are Players, NPC's, Weapon Projectiles, etc, etc.
 * @author claus
 */
abstract public class Entity {
    /**
     * The position of an Entity.
     * Must be always at the bottom middle of the entity. (For players, the feet. for arrows, the tip.)
     */
    public Vec3f position;
    
    /**
     * The size of the collider on this entity.
     * is calculated from the tip (position)
     * upwards (+rotation)
     */
    Vec3f colliderSize;
    //public Vec3f rotationNormal;
    public Vec3f rotation;
    EntityType type;
    String name;
    
    
    boolean hasGravity=false;
    
    public Entity() {
        position = new Vec3f(0f,0f,0f);
        //rotationNormal=new Vec3f(1f, 0f, 0f);
        rotation = new Vec3f(0f,0f,0f);
        type=EntityType.NOTCONTROLLED;
        
    }
    
    /**
     * Translates to a point relatively to my old position.
     * does physics.
     * @param xyz
     * @return if the translation completed without any physics collisions
     */
    public boolean translate(Vec3f xyz) {
        Vec3f newPos=position.add(xyz);
        
        //doPhysics()
        //TODO implement physics
        position=newPos;
        return true;
    }
    
    /**
     * takes my rotation into account and moves forward
     * @param xyz
     * @return 
     */
    public boolean move(Vec3f xyz) {
        //TODO rotation matrix
        return translate(xyz);
    }
            
   public enum EntityType {
       SCRIPTED, PLAYERCONTROLLED, NOTCONTROLLED
   }
}
