/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common;

import net.q1cc.cfs.mmm.client.Client;
import net.q1cc.cfs.mmm.common.math.Vec3d;
import net.q1cc.cfs.mmm.common.math.Vec3f;
import net.q1cc.cfs.mmm.common.world.WorldOctree;

/**
 *
 * @author cfstras
 */
class Physics {

    /**
     * checks for the first collision that occurs if an entity moves by a given
     * vector.
     * @param ent the entity to check.
     * @param translation the desired movement
     * @return the collision point if a collision occurs, null if no collision happens
     */
    static Vec3f getCollision(Entity ent, Vec3f translation) {
        WorldOctree oc = ent.myLastOctree;
        if(oc==null) {
            //oc = Client.instance.world.generateOctree;
        }
        oc = WorldOctree.getOctreeAt(new Vec3d(ent.position), 0, oc, false);
        //TODO
        
        ent.myLastOctree = oc;
        return null;
    }
    
}
