
package net.q1cc.cfs.mmm.client.render;


import java.nio.FloatBuffer;
import net.q1cc.cfs.mmm.common.world.Block;
import net.q1cc.cfs.mmm.common.math.Quaternionf;
import net.q1cc.cfs.mmm.common.math.Vec3d;
import net.q1cc.cfs.mmm.common.math.Vec3f;
import net.q1cc.cfs.mmm.common.world.WorldOctree;
import org.lwjgl.LWJGLException;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;
import org.lwjgl.util.glu.GLU;
/**
 *
 * @author claus
 */
public class Primitives {
    
    static int cubeList;
    
    /*
    static void renderBlock(WorldOctree b){
        // only render face if scalar product of face normal and camera is < 0
        if(b.block.color.getAlpha()!=0){
            //renderCube(b.position,Vec3f.ONE.mult((float)WorldOctree.getSidelength(b.subtreeLvl)),b.block.color,Vec3f.NULL,0.0f);
            renderCubeBackFaceCulling(b.position,Vec3f.ONE.mult((float)WorldOctree.getSidelength(b.subtreeLvl)),b.block.color,Vec3f.NULL,0.0f);
            blocksRendered++;
        } else if(false) { //check for textures here
            
        }
        
    }/* //disabled, copied the method into renderOctree to improve performance
    
    /**
     * renders an octree recursively.
     * @param b
     * @param deepness determines how many levels to go deep. 0 means "just this one", -1 means "as far as you can go"
     */
    static FloatBuffer bufferOctree(WorldOctree b, int deepness, FloatBuffer buf){
        if(b.block!=null){
            if(true){//b.block.shouldBeRendered){
                //check if i am completely surrounded
                boolean render=false;
                if(b.parent.hasSubtrees){
                    for(int i=0;i<8;i++){
                        if(b.parent.subtrees[i]==null) //TODO: bullshit, use getSixSurrounders() or something else.
                            render=true;
                            break;
                    }
                }
                
                if(true){//render) {
                    //renderCube(b.position,Vec3f.ONE.mult((float)WorldOctree.getSidelength(b.subtreeLvl)),b.block.color,Vec3f.NULL,0.0f);
                    renderCubeList(b.position,Vec3f.ONE.mult((float)WorldOctree.getSidelength(b.subtreeLvl)),b.block.color,Vec3f.NULL,0.0f);
                }
            }
        }
        if(!b.hasSubtrees||deepness==0){
            return;
        }
        for(int i=0;i<8;i++){
            if(b.subtrees[i]!=null) {
                renderOctree(b.subtrees[i],deepness-1);
            }
        }
    }
    
    static void renderPyramid(Vec3d position,Vec3f size,Color color,Vec3f rotationAxis,float angle) {
        glPushMatrix();                          // Reset The Current Modelview Matrix

        glTranslated(position.x,position.y, position.z);                // Move
        glRotatef(angle, rotationAxis.x,rotationAxis.y, rotationAxis.z);    // Rotate The Triangle
        glColor4f(color.getRed()/255f,color.getGreen()/255f,color.getBlue()/255f,color.getAlpha()/255f);
        glBegin(GL_TRIANGLES);                    // Drawing Using Triangles
            glVertex3f( 0.5f, 1.0f, 0.5f);         // Top Of Triangle (Front)
            glVertex3f( 0.0f, 0.0f, 1.0f);         // Left Of Triangle (Front)
            glVertex3f( 1.0f, 0.0f, 1.0f);         // Right Of Triangle (Front)

            glVertex3f( 0.5f, 1.0f, 0.5f);         // Top Of Triangle (Right)
            glVertex3f( 1.0f, 0.0f, 1.0f);         // Left Of Triangle (Right)
            glVertex3f( 1.0f, 0.0f, 0.0f);        // Right Of Triangle (Right)

            glVertex3f( 0.5f, 1.0f, 0.5f);         // Top Of Triangle (Back)
            glVertex3f( 1.0f, 0.0f, 0.0f);            // Left Of Triangle (Back)
            glVertex3f( 0.0f, 0.0f, 0.0f);            // Right Of Triangle (Back)

            glVertex3f( 0.5f, 1.0f, 0.5f);         // Top Of Triangle (Left)
            glVertex3f( 0.0f, 0.0f, 0.0f);         // Left Of Triangle (Left)
            glVertex3f( 0.0f, 0.0f, 1.0f);         // Right Of Triangle (Left)
        glEnd(); 
        glBegin(GL_QUADS); 
            glVertex3f( 0.0f, 0.0f, 0.0f);        
            glVertex3f( 0.0f, 0.0f, 1.0f);         
            glVertex3f( 1.0f, 0.0f, 0.0f);         
            glVertex3f( 1.0f, 0.0f, 1.0f);         
        glEnd();
        glPopMatrix();
    }
    
    
    static void renderCubeAlternative(Vec3d position,Vec3f size,ReadableColor color,Vec3f rotationAxis,float angle) {
        //glPushMatrix();                          // Reset The Current Modelview Matrix
        glTranslated(position.x,position.y, position.z);              // Move
        glRotatef(angle, rotationAxis.x,rotationAxis.y, rotationAxis.z);               // Rotate The Quad On The X axis
        glColor4f(color.getRed()/255f,color.getGreen()/255f,color.getBlue()/255f, color.getAlpha()/255f);
        
        glBegin(GL_QUADS);                        // Draw A Quad
            //use vertex buffer object here // dump whole chunk in VBO
        glEnd();
        
        glTranslated(-position.x,-position.y, -position.z);
        //glPopMatrix();
    }
    
    static void renderCubeList(Vec3d position,Vec3f size,ReadableColor color,Vec3f rotationAxis,float angle) {
        glPushMatrix();                          // Reset The Current Modelview Matrix
        glTranslated(position.x,position.y, position.z);              // Move
        glRotatef(angle, rotationAxis.x,rotationAxis.y, rotationAxis.z);               // Rotate The Quad On The X axis
        glColor4f(color.getRed()/255f,color.getGreen()/255f,color.getBlue()/255f, color.getAlpha()/255f);
        glScalef(size.x, size.y, size.z);
        glCallList(cubeList);
        glPopMatrix();
    }
    
    /**static void renderCubeBackFaceCulling(Vec3d position,Vec3f size,ReadableColor color,Vec3f rotationAxis,float angle) {
        glPushMatrix();                          // Reset The Current Modelview Matrix
        glTranslated(position.x,position.y, position.z);              // Move
        glRotatef(angle, rotationAxis.x,rotationAxis.y, rotationAxis.z);               // Rotate The Quad On The X axis
        glColor4f(color.getRed()/255f,color.getGreen()/255f,color.getBlue()/255f, color.getAlpha()/255f);
        glScalef(size.x, size.y, size.z);
        
        glBegin(GL_QUADS);                        // Draw A Quad
        //top face
        if (Vec3f.UP.scalProd(MainGLRender.cameraNormal) < 0.3f) {
            facesDrawn++;
            glNormal3i(0, 1, 0);
            glVertex3i(1, 1, 1);         // Bottom Right Of The Quad (Top)
            glVertex3i(0, 1, 1);         // Bottom Left Of The Quad (Top)
            glVertex3i(0, 1, 0);         // Top Left Of The Quad (Top)
            glVertex3i(1, 1, 0);         // Top Right Of The Quad (Top)
        }
        if (Vec3f.DOWN.scalProd(MainGLRender.cameraNormal) < 0.3f) {
            facesDrawn++;
            glNormal3i(0,-1, 0);
            glVertex3i(1, 0, 0);         // Bottom Right Of The Quad (Bottom)
            glVertex3i(0, 0, 0);         // Bottom Left Of The Quad (Bottom)
            glVertex3i(0, 0, 1);         // Top Left Of The Quad (Bottom)
            glVertex3i(1, 0, 1);         // Top Right Of The Quad (Bottom)
        }
        if (Vec3f.FRONT.scalProd(MainGLRender.cameraNormal) < 0.3f) {
            facesDrawn++;
            glNormal3f(0, 1, 1);

            glVertex3i(1, 0, 1);         // Bottom Right Of The Quad (Front)
            glVertex3i(0, 0, 1);         // Bottom Left Of The Quad (Front)
            glVertex3i(0, 1, 1);         // Top Left Of The Quad (Front)
            glVertex3i(1, 1, 1);         // Top Right Of The Quad (Front)
        }
        if (Vec3f.BACK.scalProd(MainGLRender.cameraNormal) < 0.3f) {
            facesDrawn++;
            glNormal3f(0, 1,-1);
            glVertex3i(1, 1, 0);         // Top Left Of The Quad (Back)
            glVertex3i(0, 1, 0);         // Top Right Of The Quad (Back)
            glVertex3i(0, 0, 0);         // Bottom Right Of The Quad (Back)
            glVertex3i(1, 0, 0);         // Bottom Left Of The Quad (Back)
        }
        if (Vec3f.LEFT.scalProd(MainGLRender.cameraNormal) < 0.3f) {
            facesDrawn++;
            glNormal3f(-1, 0, 0);
            glVertex3i(0, 0, 1);         // Bottom Right Of The Quad (Left)
            glVertex3i(0, 0, 0);         // Bottom Left Of The Quad (Left)
            glVertex3i(0, 1, 0);         // Top Left Of The Quad (Left)
            glVertex3i(0, 1, 1);         // Top Right Of The Quad (Left)
        }

        if (Vec3f.RIGHT.scalProd(MainGLRender.cameraNormal) < 0.3f) {
            facesDrawn++;
            glNormal3f(1, 1, 0);
            glVertex3i(1, 0, 0);         // Bottom Right Of The Quad (Right)
            glVertex3i(1, 0, 1);         // Bottom Left Of The Quad (Right)
            glVertex3i(1, 1, 1);         // Top Left Of The Quad (Right)
            glVertex3i(1, 1, 0);         // Top Right Of The Quad (Right)
        }
        glEnd();
        
        //glTranslated(-position.x,-position.y, -position.z);
        glPopMatrix();
    }*/
    
    static void renderCube(Vec3d position,Vec3f size,ReadableColor color,Vec3f rotationAxis,float angle) {
        //glPushMatrix();                          // Reset The Current Modelview Matrix
        glTranslated(position.x,position.y, position.z);              // Move
        glRotatef(angle, rotationAxis.x,rotationAxis.y, rotationAxis.z);               // Rotate The Quad On The X axis
        glColor4f(color.getRed()/255f,color.getGreen()/255f,color.getBlue()/255f, color.getAlpha()/255f);
        
        glBegin(GL_QUADS);                        // Draw A Quad
        facesDrawn++;
            glNormal3f(0.0f,1.0f,0.0f);
            glVertex3f( size.x, size.y, 0.0f);         // Top Right Of The Quad (Top)
            glVertex3f( 0.0f,   size.y, 0.0f);         // Top Left Of The Quad (Top)
            glVertex3f( 0.0f,   size.y, size.z);         // Bottom Left Of The Quad (Top)
            glVertex3f( size.x, size.y, size.z);         // Bottom Right Of The Quad (Top)
            
            facesDrawn++;
            glNormal3f(0.0f,-1.0f,0.0f);
            glVertex3f( size.x, 0.0f, size.z);         // Top Right Of The Quad (Bottom)
            glVertex3f( 0.0f,   0.0f, size.z);         // Top Left Of The Quad (Bottom)
            glVertex3f( 0.0f,   0.0f, 0.0f);         // Bottom Left Of The Quad (Bottom)
            glVertex3f( size.x, 0.0f, 0.0f);         // Bottom Right Of The Quad (Bottom)
            
            facesDrawn++;
            glNormal3f(0.0f,1.0f,1.0f);
            glVertex3f( size.x, size.y, size.z);         // Top Right Of The Quad (Front)
            glVertex3f( 0.0f,   size.y, size.z);         // Top Left Of The Quad (Front)
            glVertex3f( 0.0f,   0.0f,   size.z);         // Bottom Left Of The Quad (Front)
            glVertex3f( size.x, 0.0f,   size.z);         // Bottom Right Of The Quad (Front)
            
            facesDrawn++;
            glNormal3f(0.0f,1.0f,-1.0f);
            glVertex3f( size.x, 0.0f,   0.0f);         // Bottom Left Of The Quad (Back)
            glVertex3f( 0.0f,   0.0f,   0.0f);         // Bottom Right Of The Quad (Back)
            glVertex3f( 0.0f,   size.y, 0.0f);         // Top Right Of The Quad (Back)
            glVertex3f( size.x, size.y, 0.0f);         // Top Left Of The Quad (Back)
            
            facesDrawn++;
            glNormal3f(-1.0f,0.0f,0.0f);
            glVertex3f(0.0f,    size.y, size.z);         // Top Right Of The Quad (Left)
            glVertex3f(0.0f,    size.y, 0.0f);         // Top Left Of The Quad (Left)
            glVertex3f(0.0f,    0.0f,   0.0f);         // Bottom Left Of The Quad (Left)
            glVertex3f(0.0f,    0.0f,   size.z);         // Bottom Right Of The Quad (Left)
            
            facesDrawn++;
            glNormal3f(1.0f,1.0f,0.0f);
            glVertex3f( size.x, size.y, 0.0f);         // Top Right Of The Quad (Right)
            glVertex3f( size.x, size.y, size.z);         // Top Left Of The Quad (Right)
            glVertex3f( size.x, 0.0f,   size.z);         // Bottom Left Of The Quad (Right)
            glVertex3f( size.x, 0.0f,   0.0f);         // Bottom Right Of The Quad (Right)
        glEnd();
        
        glTranslated(-position.x,-position.y, -position.z);
        //glPopMatrix();
    }
    
    public static int generateCubeList(){
        int box = GL11.glGenLists(1);                                   // Generate 2 Different Lists
        GL11.glNewList(box,GL11.GL_COMPILE);                            // Start With The Box List
            GL11.glBegin(GL11.GL_QUADS);
                //top face
                glNormal3i(0, 1, 0);
                glVertex3i(1, 1, 1);         // Bottom Right Of The Quad (Top)
                glVertex3i(1, 1, 0);         // Bottom Left Of The Quad (Top)
                glVertex3i(0, 1, 0);         // Top Left Of The Quad (Top)
                glVertex3i(0, 1, 1);         // Top Right Of The Quad (Top)

                glNormal3i(0,-1, 0);
                glVertex3i(1, 0, 0);         // Bottom Right Of The Quad (Bottom)
                glVertex3i(0, 0, 0);         // Bottom Left Of The Quad (Bottom)
                glVertex3i(0, 0, 1);         // Top Left Of The Quad (Bottom)
                glVertex3i(1, 0, 1);         // Top Right Of The Quad (Bottom)

                glNormal3f(0, 0, 1);
                glVertex3i(1, 1, 1);         // Bottom Right Of The Quad (Front)
                glVertex3i(0, 1, 1);         // Bottom Left Of The Quad (Front)
                glVertex3i(0, 0, 1);         // Top Left Of The Quad (Front)
                glVertex3i(1, 0, 1);         // Top Right Of The Quad (Front)

                glNormal3f(0, 0,-1);
                glVertex3i(1, 1, 0);         // Top Left Of The Quad (Back)
                glVertex3i(1, 0, 0);         // Top Right Of The Quad (Back)
                glVertex3i(0, 0, 0);         // Bottom Right Of The Quad (Back)
                glVertex3i(0, 1, 0);         // Bottom Left Of The Quad (Back)

                glNormal3f(-1, 0, 0);
                glVertex3i(0, 1, 1);         // Bottom Right Of The Quad (Left)
                glVertex3i(0, 1, 0);         // Bottom Left Of The Quad (Left)
                glVertex3i(0, 0, 0);         // Top Left Of The Quad (Left)
                glVertex3i(0, 0, 1);         // Top Right Of The Quad (Left)

                glNormal3f(1, 0, 0);
                glVertex3i(1, 1, 1);         // Bottom Right Of The Quad (Right)
                glVertex3i(1, 0, 1);         // Bottom Left Of The Quad (Right)
                glVertex3i(1, 0, 0);         // Top Left Of The Quad (Right)
                glVertex3i(1, 1, 0);         
            GL11.glEnd();
        GL11.glEndList();
        
        return box;
    }
    
}
