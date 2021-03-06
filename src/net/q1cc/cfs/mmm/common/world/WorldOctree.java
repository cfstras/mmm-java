/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.common.world;

import java.io.Serializable;
import java.util.Vector;
import net.q1cc.cfs.mmm.common.math.Cubed;
import net.q1cc.cfs.mmm.common.math.Vec3d;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * This class represents an Octree that handles blocks.
 * It could hold the map or the mapchanges.
 * it has 8 children, going so:
 * 0: -x -y -z  1: x -y -z  2: -x y -z  3: x y -z,
 * 4: -x -y z   5: x -y z   6: -x y z   7: x y z
___________
|\ 2  \  3 \
| \----\----\
|\|\_6__\_7__\
| || 6  | 7  |
\0_|____|____| <--1
 \ |  4 | 5  |
  \|____|____|

 * 
 * @author claus
 **/
public class WorldOctree implements Serializable{

    /**
     * the 8 subtrees of this node, from 0 to 7
     * if one of them is null, there is no subtree in that area.
     **/
    public WorldOctree[] subtrees;
    public boolean hasSubtrees = false;
    /**
     * This determines the level of this subtree.
     * subtree level 0 means this subtree has a side length of 1 chunklet.
     * subtree level 1 => length 2 chunklets
     * subtree level 10 => length 1024 chunklets
     * subtree level -1 => length chunklet (don't use this)
     **/
    public int subtreeLvl;
    
    /**
     * The main Chunklet of this subtree.
     * if null and this is the changed-tree, this block is unchanged (if subtrees are unchanged, too)
     * if null and this is the world-tree, this block either has subtrees or is of no use and can be removed.
     **/
    public Chunklet block;
    
    public WorldOctree parent;
    
    /**
     * States the height of the lower side of this subtree
     * over (average, if we ever implement tides) sea-level
     **/
    public double height;
    
    /**
     * coordinates for the corner of subtree 0 (-x -y -z)
     */
    public Vec3d position;
    
    /**
     * Defines the max level of an octree.
     * the last octree subnode has a 16x16x16 chunklet,
     * so level 4 gives us 256 sidelength, 1M blocks
     * could be in ram without problems.
     * additionally, jit-generating and interval-trees/run-length-encoding
     * gives us more blocks.
     * 
     * 1024x1024x1024 = 1,073 mio blocks.
     * 1GB RAM gives us ~4 mio blocks.
     * 
     */
    static final int highestSubtreeLvl = 4;
    
    /**
     * This shows if this subtree has already been generated.
     * If it has not, you should do so before using it
     * (or it may have neither children nor a Block)
     * 
     * This can be used to have the WorldGenerator only generate more detail
     * when you move close to the subtree.
     */
    boolean isGenerated;
    
    /**
     * WorldOctrees should not be created from other classes. Only the main Octree can be initialized (whith xyz offsets)
     * @param subtreeLvl
     * @param height
     * @param x
     * @param y
     * @param z 
     */
    private WorldOctree(WorldOctree parent, int subtreeLvl, double height, Vec3d position) {
        this.subtreeLvl = subtreeLvl;
        this.height = height;
        this.position=position;
        this.parent=parent;
        //cube=new Cubed(position, subtreeLvl);
        blocksCreated++;
        if(blocksCreated%1000==0){
                System.out.println("Tree nodes: "+blocksCreated);
        }
    }

    /**
     * 
     */
    public WorldOctree(Vec3d position) {
        this(null, highestSubtreeLvl,
                -getSidelength(highestSubtreeLvl)/2, //the center of the block is sea-level.
                position);
    }

    /**
     * Finds a node below this one. If no node is present at the given location,
     * a new one is created.
     * @param x the x parameter of the subnode bitmask
     * @param y the x parameter of the subnode bitmask
     * @param z the x parameter of the subnode bitmask
     * @param createNew whether to create a new subnode if it doesn't exist yet.
     * @return 
     */
    synchronized WorldOctree getSubtree(boolean x,boolean y, boolean z, boolean createNew) {
        WorldOctree subtree=null;
        int index=getIndex(x,y,z);
        if(hasSubtrees){
            subtree = subtrees[index];
        }
        if (subtree == null && createNew) {//if it doesn't exist, create new
            double newheight = height;
            double slh=getSidelength(subtreeLvl - 1);
            Vec3d newpos=new Vec3d(position);
            //determine height for new Block
                if(x){
                    newpos.x+=slh;
                }
                if(y) {
                    newpos.y+=slh;
                    newheight+=slh; // increase height, too, if y=true
                }
                if(z){
                    newpos.z+=slh;
                }
            int tries=3;
            while (tries>=0&&subtree==null){
                tries--;
                try {
                    //System.out.println(Runtime.getRuntime().freeMemory()+"/"+Runtime.getRuntime().totalMemory());
                    subtree = new WorldOctree(this, subtreeLvl - 1, newheight, newpos);
                } catch (OutOfMemoryError e) {
                    System.gc();
                    System.out.println("OOM error, trying to fix...");
                    System.out.println("Tree nodes: "+blocksCreated);
                }
            }
            if(!hasSubtrees){
                subtrees=new WorldOctree[8];
                hasSubtrees=true;
            }
            subtrees[index]=subtree;
        }
        //else eventually check for validity

        return subtree;
    }
    
    public WorldOctree getSubtree(int index, boolean createNew) {
        boolean x=false,y=false,z=false;
        //it's a bitmask!
        if((index&1)==1)
            x=true;
        if((index&2)==2)
            y=true;
        if((index&4)==4)
            z=true;
        
        return getSubtree(x, y, z,createNew);
    }
    
    static long blocksCreated=0;
    
    public int getIndex(boolean x,boolean y, boolean z){
        int index=0;
        /*if(!z) //-z
            if(!y) //-y
                if(!x)//-x
                    index=0;
                 else //x
                    index=1;
             else //y
                if(!x)//-x
                    index=2;
                 else //x
                    index=3;
         else  //z
            if(!y) //-y
                if(!x)//-x
                    index=4;
                 else //x
                    index=5;
             else //y
                if(!x)//-x
                    index=6;
                 else //x
                    index=7;*/
        //it's a bitmask!
        if(x)
            index |= 1;
        if(y)
            index |= 2;
        if(z)
            index |= 4;
        return index;
    } 
    
    /**
     * Finds a subtree at a specific position, on a specific level,
     * creating it, if it doesn't exist.
     * 
     * Needs a subtree to start from, if the requested subtree is not available from that one,
     * null is returned.
     * if the search goes outside the main subtree, null is returned.
     * @param position
     * @param level
     * @param subtree
     * @return 
     */
    public static WorldOctree getOctreeAt(Vec3d position, int level, WorldOctree subtree, boolean createNew) {
        boolean search = true;
        if (subtree == null) {
            System.err.println("ERROR: null given for finding subtree @" + position + ",l=" + level);
        }

        int iterations = 0;
        do {
            if (subtree == null) { //subtree is null when we left the root subtree.
                //TODO tiled main trees should be checked for here once they are implemented
                System.err.println("ERROR: could not find octree, is out of main subtree. @" + position + ",l=" + level);
                return null;
            }
            if (Cubed.intersectsCorner(position, subtree.position.x, subtree.position.y, subtree.position.z, getSidelength(subtree.subtreeLvl))) { //check if the position is within given subtree
                if (level == subtree.subtreeLvl) {
                    return subtree;
                } else if (level > subtree.subtreeLvl) { //level is higher than given subtree
                    //check parents
                    subtree = subtree.parent;
                    search = true;
                    continue;
                } else { //level is deeper than given subtree, check children
                    boolean x = true, y = true, z = true;
                    double slh = getSidelength(subtree.subtreeLvl - 1);
                    if (subtree.position.x + slh > position.x) {
                        x = false;
                    }
                    if (subtree.position.y + slh > position.y) {
                        y = false;
                    }
                    if (subtree.position.z + slh > position.z) {
                        z = false;
                    }
                    subtree = subtree.getSubtree(x, y, z,createNew);
                    if (subtree == null) {
                        System.out.println("getSubtree returned null! wtf?");
                    }
                }
            } else {//position is not within subtree, check parents
                WorldOctree oldSub=subtree;
                subtree = subtree.parent;
                if (subtree == null) {
                    //System.out.println("block search outside main tree, was "+oldSub+" before, search for "+position+" level "+level);
                    return null;
                }
                search = true;
                continue;
            }
            iterations++;
        } while (search && iterations < 21); //asuming we have max. 10 levels, everything above 21 iterations is a crash
        if (search) {
            System.err.println("ERROR: could not find that damn octree. " + subtree + " @" + position + ",l=" + level);
            return null;
        }
        return subtree;
    }

    public static double getSidelength(int subtree_lvl) {
        return Math.pow(2, subtree_lvl)*Chunklet.csl;
    }
    
    /**
     * returns an array of the six octrees adjecent to the faces of this octree.
     * might contain "null" elements, guaranteed to be 6 in length.
     * @return 
     */
    public WorldOctree[] getSixSourrounders() {
        throw new RuntimeException("not implemented.");
//        WorldOctree[] surr=new WorldOctree[6];
//        int spID=getSPID();
//        /*if((index&1)==1)
//            x=true;
//        if((index&2)==2)
//            y=true;
//        if((index&4)==4)
//            z=true;*/
//        
//        //-x
//        if((spID&1)==0){ //-x, get +x of parent parent
//            int sppID=parent.getSPID();
//            surr[0]=parent.parent.subtrees[sppID].subtrees[spID|1];
//        }
//        
//        return surr;
    }
    
    /**
     * finds and retiurns the node next to this octree on the same level.
     * return null if there is no node.
     * sides:
     * 0 top
     * 1 bottom
     * 2 left
     * 3 right
     * 4 front
     * 5 back
     * @param side
     * @return 
     */
    public WorldOctree getAdjecent(int side){
        //TODO make a more efficient version of getAdjecent
        Vec3d nPos = new Vec3d(position);
        if (side == 0) { //top
            nPos.y+=getSidelength(subtreeLvl);
        } else if (side == 1) { //bot
            nPos.y-=getSidelength(subtreeLvl);
        } else if (side == 2) { //left
            nPos.x-=getSidelength(subtreeLvl);
        } else if (side == 3) { //right
            nPos.x+=getSidelength(subtreeLvl);
        } else if (side == 4) { //front
            nPos.z+=getSidelength(subtreeLvl);
        } else if (side == 5) { //back
            nPos.z-=getSidelength(subtreeLvl);
        } else {
            System.out.println("wtf");
        }
        return getOctreeAt(nPos, subtreeLvl, this, false);
    }
    
    /**
     * gets the index of this subtree seen from its parent.
     * -1 if there is no parent.
     * -2 if parent-children connection is broken.
     * @return 
     */
    public int getSPID() {
        if(parent==null){
            return -1;
        }
        for(int i=0;i<8;i++){
            if(parent.subtrees[i]==this){
                return i;
            }
        }
        return -2;
    }
    
    @Override
    public String toString(){
        return "(8T@"+position+",l="+subtreeLvl+",h="+height+",b=("+block+"));";
    }
    
    /**
     * this one prints subtrees recursively, prefixing recurseLvl*"\\t" to the string. 
     * @param recurseLvl
     * @return 
     */
    public String toString(int recurseLvl){
        StringBuilder sb=new StringBuilder();
        for(int rl=0;rl<recurseLvl;rl++)
                        sb.append('\t');
        sb.append(toString());
        if(hasSubtrees){
            sb.append('\n');
            for(int i=0;i<8;i++){
                if(subtrees[i]!=null){
                    
                    sb.append(subtrees[i].toString(recurseLvl+1));
                    sb.append('\n');
                }
            }
        }
        return sb.toString();
    }

    public void checkForSubtrees() {
        if(subtrees==null){
            hasSubtrees=false;
            return;
        }
        for(int i=0;i<8;i++){
            if(subtrees[i]!=null){
                hasSubtrees=true;
                return;
            }
        }
        hasSubtrees=false;
    }
}
